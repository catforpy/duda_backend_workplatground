package com.duda.user.service.miniprogram.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.redis.RedisUtils;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.miniprogram.MiniProgramFilingDTO;
import com.duda.user.entity.miniprogram.MiniProgramFiling;
import com.duda.user.mapper.miniprogram.MiniProgramFilingMapper;
import com.duda.user.service.miniprogram.MiniProgramFilingService;
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
 * 小程序备案服务实现
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
public class MiniProgramFilingServiceImpl implements MiniProgramFilingService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramFilingServiceImpl.class);

    private static final int CACHE_EXPIRE_SINGLE = 1800;  // 30分钟
    private static final int CACHE_EXPIRE_LIST = 300;     // 5分钟

    private static final String TOPIC_FILING = "MINIPROGRAM_FILING_LIFECYCLE";

    @Resource
    private MiniProgramFilingMapper filingMapper;

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
    public MiniProgramFilingDTO getFilingById(Long id) {
        log.info("⭐【Service】查询备案信息，id={}", id);

        String cacheKey = buildCacheKey("filing", "id", String.valueOf(id));
        MiniProgramFilingDTO cached = redisUtils.get(cacheKey, MiniProgramFilingDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】备案信息，id={}", id);
            return cached;
        }

        MiniProgramFiling filing = filingMapper.selectById(id);
        if (filing == null) {
            throw new BizException("备案信息不存在，id=" + id);
        }

        MiniProgramFilingDTO dto = BeanCopyUtils.copy(filing, MiniProgramFilingDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】备案信息，id={}", id);

        return dto;
    }

    @Override
    public List<MiniProgramFilingDTO> listFilingsByTenantId(Long tenantId) {
        log.info("⭐【Service】查询租户备案列表，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("filing", "tenant", String.valueOf(tenantId));
        List<MiniProgramFilingDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】租户备案列表，tenantId={}", tenantId);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramFiling> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramFiling::getTenantId, tenantId);
        List<MiniProgramFiling> list = filingMapper.selectList(wrapper);
        List<MiniProgramFilingDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramFilingDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】租户备案列表，tenantId={}, count={}", tenantId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public MiniProgramFilingDTO getFilingByMiniProgramId(Long miniProgramId) {
        log.info("⭐【Service】根据小程序ID查询备案，miniProgramId={}", miniProgramId);

        String cacheKey = buildCacheKey("filing", "miniprogram", String.valueOf(miniProgramId));
        MiniProgramFilingDTO cached = redisUtils.get(cacheKey, MiniProgramFilingDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】小程序备案，miniProgramId={}", miniProgramId);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramFiling> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramFiling::getMiniProgramId, miniProgramId);
        MiniProgramFiling filing = filingMapper.selectOne(wrapper);

        if (filing != null) {
            MiniProgramFilingDTO dto = BeanCopyUtils.copy(filing, MiniProgramFilingDTO.class);
            redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
            log.info("💾【缓存已存】小程序备案，miniProgramId={}", miniProgramId);
            return dto;
        }

        return null;
    }

    @Override
    public List<MiniProgramFilingDTO> listFilingsByStatus(Long tenantId, String filingStatus) {
        log.info("⭐【Service】根据状态查询备案列表，tenantId={}, status={}", tenantId, filingStatus);

        String cacheKey = buildCacheKey("filing", "status", tenantId + ":" + filingStatus);
        List<MiniProgramFilingDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】状态备案列表，tenantId={}, status={}", tenantId, filingStatus);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramFiling> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramFiling::getTenantId, tenantId);
        wrapper.eq(MiniProgramFiling::getFilingStatus, filingStatus);
        List<MiniProgramFiling> list = filingMapper.selectList(wrapper);
        List<MiniProgramFilingDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramFilingDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】状态备案列表，tenantId={}, status={}, count={}", tenantId, filingStatus, dtoList.size());
        }

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniProgramFilingDTO createFiling(MiniProgramFilingDTO filingDTO) {
        log.info("⭐【Service】创建备案信息，miniProgramId={}", filingDTO.getMiniProgramId());

        String lockKey = "lock:filing:create:" + filingDTO.getMiniProgramId();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            locked = distributedLock.tryLock(lockKey, lockValue, 10);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            LambdaQueryWrapper<MiniProgramFiling> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MiniProgramFiling::getMiniProgramId, filingDTO.getMiniProgramId());
            if (filingMapper.selectCount(wrapper) > 0) {
                throw new BizException("该小程序已存在备案记录");
            }

            MiniProgramFiling filing = BeanCopyUtils.copy(filingDTO, MiniProgramFiling.class);
            Long id = idGeneratorRpc.generateUserId();
            filing.setId(id);
            filing.setCreateTime(LocalDateTime.now());
            filing.setUpdateTime(LocalDateTime.now());

            int result = filingMapper.insert(filing);
            if (result <= 0) {
                throw new BizException("创建备案信息失败");
            }

            MiniProgramFilingDTO createdDTO = BeanCopyUtils.copy(filing, MiniProgramFilingDTO.class);

            String messageKey = "filing-create-" + id;
            rocketMQUtils.syncSendWithKey(TOPIC_FILING, createdDTO, messageKey);
            log.info("📤【MQ已发送】备案创建消息，id={}", id);

            clearTenantCache(filingDTO.getTenantId());

            log.info("✅【创建成功】备案信息，id={}", id);
            return createdDTO;

        } finally {
            if (locked) {
                distributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFiling(MiniProgramFilingDTO filingDTO) {
        log.info("⭐【Service】更新备案信息，id={}", filingDTO.getId());

        MiniProgramFiling existing = filingMapper.selectById(filingDTO.getId());
        if (existing == null) {
            throw new BizException("备案信息不存在，id=" + filingDTO.getId());
        }

        if (!existing.getVersion().equals(filingDTO.getVersion())) {
            throw new BizException("数据已被其他用户修改，请刷新后重试");
        }

        MiniProgramFiling filing = BeanCopyUtils.copy(filingDTO, MiniProgramFiling.class);
        filing.setUpdateTime(LocalDateTime.now());
        filing.setVersion(existing.getVersion() + 1);

        int result = filingMapper.updateById(filing);
        if (result <= 0) {
            throw new BizException("更新备案信息失败");
        }

        clearAllCache(filingDTO.getId(), filingDTO.getMiniProgramId(), filingDTO.getTenantId());

        String messageKey = "filing-update-" + filingDTO.getId();
        rocketMQUtils.syncSendWithKey(TOPIC_FILING, filingDTO, messageKey);
        log.info("📤【MQ已发送】备案更新消息，id={}", filingDTO.getId());

        log.info("✅【更新成功】备案信息，id={}", filingDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiling(Long id) {
        log.info("⭐【Service】删除备案信息，id={}", id);

        MiniProgramFiling existing = filingMapper.selectById(id);
        if (existing == null) {
            throw new BizException("备案信息不存在，id=" + id);
        }

        int result = filingMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除备案信息失败");
        }

        clearAllCache(id, existing.getMiniProgramId(), existing.getTenantId());

        MiniProgramFilingDTO deletedDTO = BeanCopyUtils.copy(existing, MiniProgramFilingDTO.class);
        String messageKey = "filing-delete-" + id;
        rocketMQUtils.syncSendWithKey(TOPIC_FILING, deletedDTO, messageKey);
        log.info("📤【MQ已发送】备案删除消息，id={}", id);

        log.info("✅【删除成功】备案信息，id={}", id);
    }

    private String buildCacheKey(String module, String type, String identifier) {
        return String.format("duda:user:%s:%s:%s", module, type, identifier);
    }

    private void clearTenantCache(Long tenantId) {
        if (tenantId != null) {
            String tenantCacheKey = buildCacheKey("filing", "tenant", String.valueOf(tenantId));
            redisUtils.delete(tenantCacheKey);
            log.info("🗑️【缓存已清除】租户备案列表，tenantId={}", tenantId);
        }
    }

    private void clearAllCache(Long id, Long miniProgramId, Long tenantId) {
        String idCacheKey = buildCacheKey("filing", "id", String.valueOf(id));
        redisUtils.delete(idCacheKey);

        if (miniProgramId != null) {
            String miniProgramCacheKey = buildCacheKey("filing", "miniprogram", String.valueOf(miniProgramId));
            redisUtils.delete(miniProgramCacheKey);
        }

        if (tenantId != null) {
            clearTenantCache(tenantId);
        }

        log.info("🗑️【缓存已清除】备案所有相关缓存，id={}", id);
    }
}
