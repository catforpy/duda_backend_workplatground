package com.duda.user.service.company.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.redis.RedisUtils;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.company.CompanyQualificationDTO;
import com.duda.user.entity.company.CompanyQualification;
import com.duda.user.mapper.company.CompanyQualificationMapper;
import com.duda.user.service.company.CompanyQualificationService;
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
 * 公司资质服务实现
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
public class CompanyQualificationServiceImpl implements CompanyQualificationService {

    private static final Logger log = LoggerFactory.getLogger(CompanyQualificationServiceImpl.class);

    private static final int CACHE_EXPIRE_SINGLE = 1800;  // 30分钟
    private static final int CACHE_EXPIRE_LIST = 300;     // 5分钟

    private static final String TOPIC_QUALIFICATION = "COMPANY_QUALIFICATION_LIFECYCLE";

    @Resource
    private CompanyQualificationMapper qualificationMapper;

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
    public CompanyQualificationDTO getQualificationById(Long id) {
        log.info("⭐【Service】查询资质信息，id={}", id);

        String cacheKey = buildCacheKey("qualification", "id", String.valueOf(id));
        CompanyQualificationDTO cached = redisUtils.get(cacheKey, CompanyQualificationDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】资质信息，id={}", id);
            return cached;
        }

        CompanyQualification qualification = qualificationMapper.selectById(id);
        if (qualification == null) {
            throw new BizException("资质信息不存在，id=" + id);
        }

        CompanyQualificationDTO dto = BeanCopyUtils.copy(qualification, CompanyQualificationDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】资质信息，id={}", id);

        return dto;
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByTenantId(Long tenantId) {
        log.info("⭐【Service】查询租户资质列表，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("qualification", "tenant", String.valueOf(tenantId));
        List<CompanyQualificationDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】租户资质列表，tenantId={}", tenantId);
            return cached;
        }

        LambdaQueryWrapper<CompanyQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyQualification::getTenantId, tenantId);
        List<CompanyQualification> list = qualificationMapper.selectList(wrapper);
        List<CompanyQualificationDTO> dtoList = BeanCopyUtils.copyList(list, CompanyQualificationDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】租户资质列表，tenantId={}, count={}", tenantId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByCompany(Long tenantId, Long companyId) {
        log.info("⭐【Service】根据公司查询资质，tenantId={}, companyId={}", tenantId, companyId);

        String cacheKey = buildCacheKey("qualification", "company", tenantId + ":" + companyId);
        List<CompanyQualificationDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】公司资质列表，companyId={}", companyId);
            return cached;
        }

        LambdaQueryWrapper<CompanyQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyQualification::getTenantId, tenantId);
        wrapper.eq(CompanyQualification::getCompanyId, companyId);
        List<CompanyQualification> list = qualificationMapper.selectList(wrapper);
        List<CompanyQualificationDTO> dtoList = BeanCopyUtils.copyList(list, CompanyQualificationDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】公司资质列表，companyId={}, count={}", companyId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByType(Long tenantId, String qualificationType) {
        log.info("⭐【Service】根据类型查询资质，tenantId={}, type={}", tenantId, qualificationType);

        String cacheKey = buildCacheKey("qualification", "type", tenantId + ":" + qualificationType);
        List<CompanyQualificationDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】类型资质列表，tenantId={}, type={}", tenantId, qualificationType);
            return cached;
        }

        LambdaQueryWrapper<CompanyQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyQualification::getTenantId, tenantId);
        wrapper.eq(CompanyQualification::getQualificationType, qualificationType);
        List<CompanyQualification> list = qualificationMapper.selectList(wrapper);
        List<CompanyQualificationDTO> dtoList = BeanCopyUtils.copyList(list, CompanyQualificationDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】类型资质列表，tenantId={}, type={}, count={}", tenantId, qualificationType, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByAuditStatus(Long tenantId, String auditStatus) {
        log.info("⭐【Service】根据审核状态查询资质，tenantId={}, auditStatus={}", tenantId, auditStatus);

        String cacheKey = buildCacheKey("qualification", "audit", tenantId + ":" + auditStatus);
        List<CompanyQualificationDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】审核状态资质列表，tenantId={}, auditStatus={}", tenantId, auditStatus);
            return cached;
        }

        LambdaQueryWrapper<CompanyQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyQualification::getTenantId, tenantId);
        wrapper.eq(CompanyQualification::getAuditStatus, auditStatus);
        List<CompanyQualification> list = qualificationMapper.selectList(wrapper);
        List<CompanyQualificationDTO> dtoList = BeanCopyUtils.copyList(list, CompanyQualificationDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】审核状态资质列表，tenantId={}, auditStatus={}, count={}", tenantId, auditStatus, dtoList.size());
        }

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyQualificationDTO createQualification(CompanyQualificationDTO qualificationDTO) {
        log.info("⭐【Service】创建资质信息，companyId={}, type={}", qualificationDTO.getCompanyId(), qualificationDTO.getQualificationType());

        String lockKey = "lock:qualification:create:" + qualificationDTO.getCompanyId() + ":" + qualificationDTO.getQualificationType();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            locked = distributedLock.tryLock(lockKey, lockValue, 10);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            CompanyQualification qualification = BeanCopyUtils.copy(qualificationDTO, CompanyQualification.class);
            Long id = idGeneratorRpc.generateUserId();
            qualification.setId(id);
            qualification.setCreateTime(LocalDateTime.now());
            qualification.setUpdateTime(LocalDateTime.now());

            int result = qualificationMapper.insert(qualification);
            if (result <= 0) {
                throw new BizException("创建资质信息失败");
            }

            CompanyQualificationDTO createdDTO = BeanCopyUtils.copy(qualification, CompanyQualificationDTO.class);

            String messageKey = "qualification-create-" + id;
            try {
                rocketMQUtils.syncSendWithKey(TOPIC_QUALIFICATION, createdDTO, messageKey);
                log.info("📤【MQ已发送】资质创建消息，id={}", id);
            } catch (Exception e) {
                // MQ发送失败不影响主业务流程，只记录日志
                log.error("❌【MQ发送失败】资质创建消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
            }

            clearTenantCache(qualificationDTO.getTenantId());
            clearCompanyCache(qualificationDTO.getTenantId(), qualificationDTO.getCompanyId());

            log.info("✅【创建成功】资质信息，id={}", id);
            return createdDTO;

        } finally {
            if (locked) {
                distributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQualification(CompanyQualificationDTO qualificationDTO) {
        log.info("⭐【Service】更新资质信息，id={}", qualificationDTO.getId());

        CompanyQualification existing = qualificationMapper.selectById(qualificationDTO.getId());
        if (existing == null) {
            throw new BizException("资质信息不存在，id=" + qualificationDTO.getId());
        }

        if (!existing.getVersion().equals(qualificationDTO.getVersion())) {
            throw new BizException("数据已被其他用户修改，请刷新后重试");
        }

        CompanyQualification qualification = BeanCopyUtils.copy(qualificationDTO, CompanyQualification.class);
        qualification.setUpdateTime(LocalDateTime.now());
        qualification.setVersion(existing.getVersion() + 1);

        int result = qualificationMapper.updateById(qualification);
        if (result <= 0) {
            throw new BizException("更新资质信息失败");
        }

        clearAllCache(qualificationDTO.getId(), qualificationDTO.getTenantId(), qualificationDTO.getCompanyId(),
                      qualificationDTO.getQualificationType(), qualificationDTO.getAuditStatus());

        String messageKey = "qualification-update-" + qualificationDTO.getId();
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_QUALIFICATION, qualificationDTO, messageKey);
            log.info("📤【MQ已发送】资质更新消息，id={}", qualificationDTO.getId());
        } catch (Exception e) {
            log.error("❌【MQ发送失败】资质更新消息，id={}, key={}, error={}", qualificationDTO.getId(), messageKey, e.getMessage());
        }

        log.info("✅【更新成功】资质信息，id={}", qualificationDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQualification(Long id) {
        log.info("⭐【Service】删除资质信息，id={}", id);

        CompanyQualification existing = qualificationMapper.selectById(id);
        if (existing == null) {
            throw new BizException("资质信息不存在，id=" + id);
        }

        int result = qualificationMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除资质信息失败");
        }

        clearAllCache(id, existing.getTenantId(), existing.getCompanyId(),
                      existing.getQualificationType(), existing.getAuditStatus());

        CompanyQualificationDTO deletedDTO = BeanCopyUtils.copy(existing, CompanyQualificationDTO.class);
        String messageKey = "qualification-delete-" + id;
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_QUALIFICATION, deletedDTO, messageKey);
            log.info("📤【MQ已发送】资质删除消息，id={}", id);
        } catch (Exception e) {
            log.error("❌【MQ发送失败】资质删除消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
        }

        log.info("✅【删除成功】资质信息，id={}", id);
    }

    private String buildCacheKey(String module, String type, String identifier) {
        return String.format("duda:user:%s:%s:%s", module, type, identifier);
    }

    private void clearTenantCache(Long tenantId) {
        if (tenantId != null) {
            String tenantCacheKey = buildCacheKey("qualification", "tenant", String.valueOf(tenantId));
            redisUtils.delete(tenantCacheKey);
            log.info("🗑️【缓存已清除】租户资质列表，tenantId={}", tenantId);
        }
    }

    private void clearCompanyCache(Long tenantId, Long companyId) {
        if (tenantId != null && companyId != null) {
            String companyCacheKey = buildCacheKey("qualification", "company", tenantId + ":" + companyId);
            redisUtils.delete(companyCacheKey);
        }
    }

    private void clearAllCache(Long id, Long tenantId, Long companyId, String qualificationType, String auditStatus) {
        String idCacheKey = buildCacheKey("qualification", "id", String.valueOf(id));
        redisUtils.delete(idCacheKey);

        if (tenantId != null) {
            clearTenantCache(tenantId);
            clearCompanyCache(tenantId, companyId);

            if (qualificationType != null) {
                String typeCacheKey = buildCacheKey("qualification", "type", tenantId + ":" + qualificationType);
                redisUtils.delete(typeCacheKey);
            }

            if (auditStatus != null) {
                String auditCacheKey = buildCacheKey("qualification", "audit", tenantId + ":" + auditStatus);
                redisUtils.delete(auditCacheKey);
            }
        }

        log.info("🗑️【缓存已清除】资质所有相关缓存，id={}", id);
    }
}
