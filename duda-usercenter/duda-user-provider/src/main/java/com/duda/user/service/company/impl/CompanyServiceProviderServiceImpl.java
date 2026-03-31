package com.duda.user.service.company.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.redis.RedisUtils;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.company.CompanyServiceProviderDTO;
import com.duda.user.entity.company.CompanyServiceProvider;
import com.duda.user.mapper.company.CompanyServiceProviderMapper;
import com.duda.user.service.company.CompanyServiceProviderService;
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
 * 服务商申请服务实现
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
public class CompanyServiceProviderServiceImpl implements CompanyServiceProviderService {

    private static final Logger log = LoggerFactory.getLogger(CompanyServiceProviderServiceImpl.class);

    private static final int CACHE_EXPIRE_SINGLE = 1800;  // 30分钟
    private static final int CACHE_EXPIRE_LIST = 300;     // 5分钟

    private static final String TOPIC_SERVICE_PROVIDER = "COMPANY_SERVICE_PROVIDER_LIFECYCLE";

    @Resource
    private CompanyServiceProviderMapper serviceProviderMapper;

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
    public CompanyServiceProviderDTO getServiceProviderById(Long id) {
        log.info("⭐【Service】查询服务商申请，id={}", id);

        String cacheKey = buildCacheKey("serviceprovider", "id", String.valueOf(id));
        CompanyServiceProviderDTO cached = redisUtils.get(cacheKey, CompanyServiceProviderDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】服务商申请，id={}", id);
            return cached;
        }

        CompanyServiceProvider serviceProvider = serviceProviderMapper.selectById(id);
        if (serviceProvider == null) {
            throw new BizException("服务商申请不存在，id=" + id);
        }

        CompanyServiceProviderDTO dto = BeanCopyUtils.copy(serviceProvider, CompanyServiceProviderDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】服务商申请，id={}", id);

        return dto;
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByTenantId(Long tenantId) {
        log.info("⭐【Service】查询租户服务商列表，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("serviceprovider", "tenant", String.valueOf(tenantId));
        List<CompanyServiceProviderDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】租户服务商列表，tenantId={}", tenantId);
            return cached;
        }

        LambdaQueryWrapper<CompanyServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyServiceProvider::getTenantId, tenantId);
        List<CompanyServiceProvider> list = serviceProviderMapper.selectList(wrapper);
        List<CompanyServiceProviderDTO> dtoList = BeanCopyUtils.copyList(list, CompanyServiceProviderDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】租户服务商列表，tenantId={}, count={}", tenantId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByCompany(Long tenantId, Long companyId) {
        log.info("⭐【Service】根据公司查询服务商，tenantId={}, companyId={}", tenantId, companyId);

        String cacheKey = buildCacheKey("serviceprovider", "company", tenantId + ":" + companyId);
        List<CompanyServiceProviderDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】公司服务商列表，companyId={}", companyId);
            return cached;
        }

        LambdaQueryWrapper<CompanyServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyServiceProvider::getTenantId, tenantId);
        wrapper.eq(CompanyServiceProvider::getCompanyId, companyId);
        List<CompanyServiceProvider> list = serviceProviderMapper.selectList(wrapper);
        List<CompanyServiceProviderDTO> dtoList = BeanCopyUtils.copyList(list, CompanyServiceProviderDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】公司服务商列表，companyId={}, count={}", companyId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByType(Long tenantId, String applyType) {
        log.info("⭐【Service】根据类型查询服务商，tenantId={}, applyType={}", tenantId, applyType);

        String cacheKey = buildCacheKey("serviceprovider", "type", tenantId + ":" + applyType);
        List<CompanyServiceProviderDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】类型服务商列表，tenantId={}, applyType={}", tenantId, applyType);
            return cached;
        }

        LambdaQueryWrapper<CompanyServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyServiceProvider::getTenantId, tenantId);
        wrapper.eq(CompanyServiceProvider::getApplyType, applyType);
        List<CompanyServiceProvider> list = serviceProviderMapper.selectList(wrapper);
        List<CompanyServiceProviderDTO> dtoList = BeanCopyUtils.copyList(list, CompanyServiceProviderDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】类型服务商列表，tenantId={}, applyType={}, count={}", tenantId, applyType, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByStatus(Long tenantId, String status) {
        log.info("⭐【Service】根据状态查询服务商，tenantId={}, status={}", tenantId, status);

        String cacheKey = buildCacheKey("serviceprovider", "status", tenantId + ":" + status);
        List<CompanyServiceProviderDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】状态服务商列表，tenantId={}, status={}", tenantId, status);
            return cached;
        }

        LambdaQueryWrapper<CompanyServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyServiceProvider::getTenantId, tenantId);
        wrapper.eq(CompanyServiceProvider::getStatus, status);
        List<CompanyServiceProvider> list = serviceProviderMapper.selectList(wrapper);
        List<CompanyServiceProviderDTO> dtoList = BeanCopyUtils.copyList(list, CompanyServiceProviderDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】状态服务商列表，tenantId={}, status={}, count={}", tenantId, status, dtoList.size());
        }

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyServiceProviderDTO createServiceProvider(CompanyServiceProviderDTO serviceProviderDTO) {
        log.info("⭐【Service】创建服务商申请，companyId={}, applyType={}", serviceProviderDTO.getCompanyId(), serviceProviderDTO.getApplyType());

        String lockKey = "lock:serviceprovider:create:" + serviceProviderDTO.getCompanyId() + ":" + serviceProviderDTO.getApplyType();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            locked = distributedLock.tryLock(lockKey, lockValue, 10);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            CompanyServiceProvider serviceProvider = BeanCopyUtils.copy(serviceProviderDTO, CompanyServiceProvider.class);
            Long id = idGeneratorRpc.generateUserId();
            serviceProvider.setId(id);
            serviceProvider.setCreateTime(LocalDateTime.now());
            serviceProvider.setUpdateTime(LocalDateTime.now());

            int result = serviceProviderMapper.insert(serviceProvider);
            if (result <= 0) {
                throw new BizException("创建服务商申请失败");
            }

            CompanyServiceProviderDTO createdDTO = BeanCopyUtils.copy(serviceProvider, CompanyServiceProviderDTO.class);

            String messageKey = "serviceprovider-create-" + id;
            try {
                rocketMQUtils.syncSendWithKey(TOPIC_SERVICE_PROVIDER, createdDTO, messageKey);
                log.info("📤【MQ已发送】服务商申请创建消息，id={}", id);
            } catch (Exception e) {
                log.error("❌【MQ发送失败】服务商申请创建消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
            }

            clearTenantCache(serviceProviderDTO.getTenantId());
            clearCompanyCache(serviceProviderDTO.getTenantId(), serviceProviderDTO.getCompanyId());

            log.info("✅【创建成功】服务商申请，id={}", id);
            return createdDTO;

        } finally {
            if (locked) {
                distributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceProvider(CompanyServiceProviderDTO serviceProviderDTO) {
        log.info("⭐【Service】更新服务商申请，id={}", serviceProviderDTO.getId());

        CompanyServiceProvider existing = serviceProviderMapper.selectById(serviceProviderDTO.getId());
        if (existing == null) {
            throw new BizException("服务商申请不存在，id=" + serviceProviderDTO.getId());
        }

        if (!existing.getVersion().equals(serviceProviderDTO.getVersion())) {
            throw new BizException("数据已被其他用户修改，请刷新后重试");
        }

        CompanyServiceProvider serviceProvider = BeanCopyUtils.copy(serviceProviderDTO, CompanyServiceProvider.class);
        serviceProvider.setUpdateTime(LocalDateTime.now());
        serviceProvider.setVersion(existing.getVersion() + 1);

        int result = serviceProviderMapper.updateById(serviceProvider);
        if (result <= 0) {
            throw new BizException("更新服务商申请失败");
        }

        clearAllCache(serviceProviderDTO.getId(), serviceProviderDTO.getTenantId(), serviceProviderDTO.getCompanyId(),
                      serviceProviderDTO.getApplyType(), serviceProviderDTO.getStatus());

        String messageKey = "serviceprovider-update-" + serviceProviderDTO.getId();
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_SERVICE_PROVIDER, serviceProviderDTO, messageKey);
            log.info("📤【MQ已发送】服务商申请更新消息，id={}", serviceProviderDTO.getId());
        } catch (Exception e) {
            log.error("❌【MQ发送失败】服务商申请更新消息，id={}, key={}, error={}", serviceProviderDTO.getId(), messageKey, e.getMessage());
        }

        log.info("✅【更新成功】服务商申请，id={}", serviceProviderDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceProvider(Long id) {
        log.info("⭐【Service】删除服务商申请，id={}", id);

        CompanyServiceProvider existing = serviceProviderMapper.selectById(id);
        if (existing == null) {
            throw new BizException("服务商申请不存在，id=" + id);
        }

        int result = serviceProviderMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除服务商申请失败");
        }

        clearAllCache(id, existing.getTenantId(), existing.getCompanyId(),
                      existing.getApplyType(), existing.getStatus());

        CompanyServiceProviderDTO deletedDTO = BeanCopyUtils.copy(existing, CompanyServiceProviderDTO.class);
        String messageKey = "serviceprovider-delete-" + id;
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_SERVICE_PROVIDER, deletedDTO, messageKey);
            log.info("📤【MQ已发送】服务商申请删除消息，id={}", id);
        } catch (Exception e) {
            log.error("❌【MQ发送失败】服务商申请删除消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
        }

        log.info("✅【删除成功】服务商申请，id={}", id);
    }

    private String buildCacheKey(String module, String type, String identifier) {
        return String.format("duda:user:%s:%s:%s", module, type, identifier);
    }

    private void clearTenantCache(Long tenantId) {
        if (tenantId != null) {
            String tenantCacheKey = buildCacheKey("serviceprovider", "tenant", String.valueOf(tenantId));
            redisUtils.delete(tenantCacheKey);
            log.info("🗑️【缓存已清除】租户服务商列表，tenantId={}", tenantId);
        }
    }

    private void clearCompanyCache(Long tenantId, Long companyId) {
        if (tenantId != null && companyId != null) {
            String companyCacheKey = buildCacheKey("serviceprovider", "company", tenantId + ":" + companyId);
            redisUtils.delete(companyCacheKey);
        }
    }

    private void clearAllCache(Long id, Long tenantId, Long companyId, String applyType, String status) {
        String idCacheKey = buildCacheKey("serviceprovider", "id", String.valueOf(id));
        redisUtils.delete(idCacheKey);

        if (tenantId != null) {
            clearTenantCache(tenantId);
            clearCompanyCache(tenantId, companyId);

            if (applyType != null) {
                String typeCacheKey = buildCacheKey("serviceprovider", "type", tenantId + ":" + applyType);
                redisUtils.delete(typeCacheKey);
            }

            if (status != null) {
                String statusCacheKey = buildCacheKey("serviceprovider", "status", tenantId + ":" + status);
                redisUtils.delete(statusCacheKey);
            }
        }

        log.info("🗑️【缓存已清除】服务商申请所有相关缓存，id={}", id);
    }
}
