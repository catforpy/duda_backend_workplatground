package com.duda.user.service.miniprogram.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.redis.RedisUtils;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.miniprogram.MiniProgramCertificationDTO;
import com.duda.user.entity.miniprogram.MiniProgramCertification;
import com.duda.user.mapper.miniprogram.MiniProgramCertificationMapper;
import com.duda.user.service.miniprogram.MiniProgramCertificationService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 小程序认证服务实现
 *
 * 技术要点：
 * 1. CacheAside缓存模式：单条30分钟，列表5分钟
 * 2. RedisDistributedLock：创建操作使用分布式锁防止重复
 * 3. RocketMQ消息：同步发送生命周期事件消息
 * 4. 租户隔离：所有查询必须过滤tenant_id
 * 5. 乐观锁：使用version字段防止并发更新
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class MiniProgramCertificationServiceImpl implements MiniProgramCertificationService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramCertificationServiceImpl.class);

    private static final int CACHE_EXPIRE_SINGLE = 1800;  // 30分钟
    private static final int CACHE_EXPIRE_LIST = 300;     // 5分钟

    private static final String TOPIC_CERTIFICATION = "MINIPROGRAM_CERTIFICATION_LIFECYCLE";

    @Resource
    private MiniProgramCertificationMapper certificationMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private RedisDistributedLock distributedLock;

    @Resource
    private RocketMQUtils rocketMQUtils;

    @DubboReference(
        group = "INFRA_GROUP",
        version = "1.0.0",
        registry = "infraRegistry"
    )
    private IdGeneratorRpc idGeneratorRpc;

    @Override
    public MiniProgramCertificationDTO getCertificationById(Long id) {
        log.info("⭐【Service】查询认证信息，id={}", id);

        String cacheKey = buildCacheKey("certification", "id", String.valueOf(id));
        MiniProgramCertificationDTO cached = redisUtils.get(cacheKey, MiniProgramCertificationDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】认证信息，id={}", id);
            return cached;
        }

        MiniProgramCertification certification = certificationMapper.selectById(id);
        if (certification == null) {
            throw new BizException("认证信息不存在，id=" + id);
        }

        MiniProgramCertificationDTO dto = BeanCopyUtils.copy(certification, MiniProgramCertificationDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】认证信息，id={}", id);

        return dto;
    }

    @Override
    public List<MiniProgramCertificationDTO> listCertificationsByTenantId(Long tenantId) {
        log.info("⭐【Service】查询租户认证列表，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("certification", "tenant", String.valueOf(tenantId));
        List<MiniProgramCertificationDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】租户认证列表，tenantId={}", tenantId);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramCertification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramCertification::getTenantId, tenantId);
        List<MiniProgramCertification> list = certificationMapper.selectList(wrapper);
        List<MiniProgramCertificationDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramCertificationDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】租户认证列表，tenantId={}, count={}", tenantId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public MiniProgramCertificationDTO getCertificationByMiniProgramId(Long miniProgramId) {
        log.info("⭐【Service】根据小程序ID查询认证，miniProgramId={}", miniProgramId);

        String cacheKey = buildCacheKey("certification", "miniprogram", String.valueOf(miniProgramId));
        MiniProgramCertificationDTO cached = redisUtils.get(cacheKey, MiniProgramCertificationDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】小程序认证，miniProgramId={}", miniProgramId);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramCertification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramCertification::getMiniProgramId, miniProgramId);
        MiniProgramCertification certification = certificationMapper.selectOne(wrapper);

        if (certification != null) {
            MiniProgramCertificationDTO dto = BeanCopyUtils.copy(certification, MiniProgramCertificationDTO.class);
            redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
            log.info("💾【缓存已存】小程序认证，miniProgramId={}", miniProgramId);
            return dto;
        }

        return null;
    }

    @Override
    public List<MiniProgramCertificationDTO> listCertificationsByStatus(Long tenantId, String certificationStatus) {
        log.info("⭐【Service】根据状态查询认证列表，tenantId={}, status={}", tenantId, certificationStatus);

        String cacheKey = buildCacheKey("certification", "status", tenantId + ":" + certificationStatus);
        List<MiniProgramCertificationDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】状态认证列表，tenantId={}, status={}", tenantId, certificationStatus);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramCertification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramCertification::getTenantId, tenantId);
        wrapper.eq(MiniProgramCertification::getCertificationStatus, certificationStatus);
        List<MiniProgramCertification> list = certificationMapper.selectList(wrapper);
        List<MiniProgramCertificationDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramCertificationDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】状态认证列表，tenantId={}, status={}, count={}", tenantId, certificationStatus, dtoList.size());
        }

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniProgramCertificationDTO createCertification(MiniProgramCertificationDTO certificationDTO) {
        log.info("⭐【Service】创建认证信息，miniProgramId={}", certificationDTO.getMiniProgramId());

        String lockKey = "lock:certification:create:" + certificationDTO.getMiniProgramId();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            locked = distributedLock.tryLock(lockKey, lockValue, 10);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            LambdaQueryWrapper<MiniProgramCertification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MiniProgramCertification::getMiniProgramId, certificationDTO.getMiniProgramId());
            if (certificationMapper.selectCount(wrapper) > 0) {
                throw new BizException("该小程序已存在认证记录");
            }

            MiniProgramCertification certification = BeanCopyUtils.copy(certificationDTO, MiniProgramCertification.class);
            Long id = idGeneratorRpc.generateUserId();
            certification.setId(id);
            certification.setCreateTime(LocalDateTime.now());
            certification.setUpdateTime(LocalDateTime.now());

            int result = certificationMapper.insert(certification);
            if (result <= 0) {
                throw new BizException("创建认证信息失败");
            }

            MiniProgramCertificationDTO createdDTO = BeanCopyUtils.copy(certification, MiniProgramCertificationDTO.class);

            String messageKey = "certification-create-" + id;
            rocketMQUtils.syncSendWithKey(TOPIC_CERTIFICATION, createdDTO, messageKey);
            log.info("📤【MQ已发送】认证创建消息，id={}", id);

            clearTenantCache(certificationDTO.getTenantId());

            log.info("✅【创建成功】认证信息，id={}", id);
            return createdDTO;

        } finally {
            if (locked) {
                distributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCertification(MiniProgramCertificationDTO certificationDTO) {
        log.info("⭐【Service】更新认证信息，id={}", certificationDTO.getId());

        MiniProgramCertification existing = certificationMapper.selectById(certificationDTO.getId());
        if (existing == null) {
            throw new BizException("认证信息不存在，id=" + certificationDTO.getId());
        }

        if (!existing.getVersion().equals(certificationDTO.getVersion())) {
            throw new BizException("数据已被其他用户修改，请刷新后重试");
        }

        MiniProgramCertification certification = BeanCopyUtils.copy(certificationDTO, MiniProgramCertification.class);
        certification.setUpdateTime(LocalDateTime.now());
        certification.setVersion(existing.getVersion() + 1);

        int result = certificationMapper.updateById(certification);
        if (result <= 0) {
            throw new BizException("更新认证信息失败");
        }

        clearAllCache(certificationDTO.getId(), certificationDTO.getMiniProgramId(), certificationDTO.getTenantId());

        String messageKey = "certification-update-" + certificationDTO.getId();
        rocketMQUtils.syncSendWithKey(TOPIC_CERTIFICATION, certificationDTO, messageKey);
        log.info("📤【MQ已发送】认证更新消息，id={}", certificationDTO.getId());

        log.info("✅【更新成功】认证信息，id={}", certificationDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCertification(Long id) {
        log.info("⭐【Service】删除认证信息，id={}", id);

        MiniProgramCertification existing = certificationMapper.selectById(id);
        if (existing == null) {
            throw new BizException("认证信息不存在，id=" + id);
        }

        int result = certificationMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除认证信息失败");
        }

        clearAllCache(id, existing.getMiniProgramId(), existing.getTenantId());

        MiniProgramCertificationDTO deletedDTO = BeanCopyUtils.copy(existing, MiniProgramCertificationDTO.class);
        String messageKey = "certification-delete-" + id;
        rocketMQUtils.syncSendWithKey(TOPIC_CERTIFICATION, deletedDTO, messageKey);
        log.info("📤【MQ已发送】认证删除消息，id={}", id);

        log.info("✅【删除成功】认证信息，id={}", id);
    }

    private String buildCacheKey(String module, String type, String identifier) {
        return String.format("duda:user:%s:%s:%s", module, type, identifier);
    }

    private void clearTenantCache(Long tenantId) {
        if (tenantId != null) {
            String tenantCacheKey = buildCacheKey("certification", "tenant", String.valueOf(tenantId));
            redisUtils.delete(tenantCacheKey);
            log.info("🗑️【缓存已清除】租户认证列表，tenantId={}", tenantId);
        }
    }

    private void clearAllCache(Long id, Long miniProgramId, Long tenantId) {
        String idCacheKey = buildCacheKey("certification", "id", String.valueOf(id));
        redisUtils.delete(idCacheKey);

        if (miniProgramId != null) {
            String miniProgramCacheKey = buildCacheKey("certification", "miniprogram", String.valueOf(miniProgramId));
            redisUtils.delete(miniProgramCacheKey);
        }

        if (tenantId != null) {
            clearTenantCache(tenantId);
        }

        log.info("🗑️【缓存已清除】认证所有相关缓存，id={}", id);
    }
}
