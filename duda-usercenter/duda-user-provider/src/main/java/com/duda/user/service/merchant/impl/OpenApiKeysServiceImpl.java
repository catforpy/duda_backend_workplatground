package com.duda.user.service.merchant.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.redis.RedisUtils;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.merchant.OpenApiKeySpecDTO;
import com.duda.user.entity.merchant.OpenApiKeys;
import com.duda.user.mapper.merchant.OpenApiKeysMapper;
import com.duda.user.service.merchant.OpenApiKeysService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 开放API密钥服务实现
 *
 * 技术要点：
 * 1. CacheAside缓存模式：单条30分钟，列表5分钟，统计1分钟
 * 2. RedisDistributedLock：创建操作使用分布式锁防止重复
 * 3. RocketMQ消息：同步发送生命周期事件消息
 * 4. 租户隔离：所有查询必须过滤tenant_id
 * 5. 乐观锁：使用version字段防止并发更新
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class OpenApiKeysServiceImpl implements OpenApiKeysService {

    private static final Logger log = LoggerFactory.getLogger(OpenApiKeysServiceImpl.class);

    private static final int CACHE_EXPIRE_SINGLE = 1800;  // 30分钟
    private static final int CACHE_EXPIRE_LIST = 300;     // 5分钟
    private static final int CACHE_EXPIRE_COUNT = 60;     // 1分钟

    private static final String TOPIC_OPENAPI_KEYS = "OPENAPI_KEYS_LIFECYCLE";

    @Resource
    private OpenApiKeysMapper openApiKeysMapper;

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
    public OpenApiKeySpecDTO getOpenApiKeysById(Long id) {
        log.info("⭐【Service】查询API密钥，id={}", id);

        String cacheKey = buildCacheKey("openapikeys", "id", String.valueOf(id));
        OpenApiKeySpecDTO cached = redisUtils.get(cacheKey, OpenApiKeySpecDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】API密钥，id={}", id);
            return cached;
        }

        OpenApiKeys openApiKeys = openApiKeysMapper.selectById(id);
        if (openApiKeys == null) {
            throw new BizException("API密钥不存在，id=" + id);
        }

        OpenApiKeySpecDTO dto = BeanCopyUtils.copy(openApiKeys, OpenApiKeySpecDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】API密钥，id={}", id);

        return dto;
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByTenantId(Long tenantId) {
        log.info("⭐【Service】查询租户API密钥列表，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("openapikeys", "tenant", String.valueOf(tenantId));
        List<OpenApiKeySpecDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】租户API密钥列表，tenantId={}", tenantId);
            return cached;
        }

        LambdaQueryWrapper<OpenApiKeys> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenApiKeys::getTenantId, tenantId);
        List<OpenApiKeys> list = openApiKeysMapper.selectList(wrapper);
        List<OpenApiKeySpecDTO> dtoList = BeanCopyUtils.copyList(list, OpenApiKeySpecDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】租户API密钥列表，tenantId={}, count={}", tenantId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public OpenApiKeySpecDTO getOpenApiKeysByAppId(Long tenantId, String appId) {
        log.info("⭐【Service】根据AppID查询API密钥，tenantId={}, appId={}", tenantId, appId);

        String cacheKey = buildCacheKey("openapikeys", "appid", tenantId + ":" + appId);
        OpenApiKeySpecDTO cached = redisUtils.get(cacheKey, OpenApiKeySpecDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】API密钥AppID，appId={}", appId);
            return cached;
        }

        LambdaQueryWrapper<OpenApiKeys> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenApiKeys::getTenantId, tenantId);
        wrapper.eq(OpenApiKeys::getAppId, appId);
        OpenApiKeys openApiKeys = openApiKeysMapper.selectOne(wrapper);
        if (openApiKeys == null) {
            throw new BizException("API密钥不存在，appId=" + appId);
        }

        OpenApiKeySpecDTO dto = BeanCopyUtils.copy(openApiKeys, OpenApiKeySpecDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】API密钥AppID，appId={}", appId);

        return dto;
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByStatus(Long tenantId, Integer status) {
        log.info("⭐【Service】根据状态查询API密钥，tenantId={}, status={}", tenantId, status);

        String cacheKey = buildCacheKey("openapikeys", "status", tenantId + ":" + status);
        List<OpenApiKeySpecDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】状态API密钥列表，tenantId={}, status={}", tenantId, status);
            return cached;
        }

        LambdaQueryWrapper<OpenApiKeys> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenApiKeys::getTenantId, tenantId);
        wrapper.eq(OpenApiKeys::getStatus, status);
        List<OpenApiKeys> list = openApiKeysMapper.selectList(wrapper);
        List<OpenApiKeySpecDTO> dtoList = BeanCopyUtils.copyList(list, OpenApiKeySpecDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】状态API密钥列表，tenantId={}, status={}, count={}", tenantId, status, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByOwner(Long tenantId, Long appOwnerId) {
        log.info("⭐【Service】根据所有者查询API密钥，tenantId={}, appOwnerId={}", tenantId, appOwnerId);

        String cacheKey = buildCacheKey("openapikeys", "owner", tenantId + ":" + appOwnerId);
        List<OpenApiKeySpecDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】所有者API密钥列表，appOwnerId={}", appOwnerId);
            return cached;
        }

        LambdaQueryWrapper<OpenApiKeys> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenApiKeys::getTenantId, tenantId);
        wrapper.eq(OpenApiKeys::getAppOwnerId, appOwnerId);
        List<OpenApiKeys> list = openApiKeysMapper.selectList(wrapper);
        List<OpenApiKeySpecDTO> dtoList = BeanCopyUtils.copyList(list, OpenApiKeySpecDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】所有者API密钥列表，appOwnerId={}, count={}", appOwnerId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysPage(Long tenantId, Integer status, String appType,
                                                        Integer pageNum, Integer pageSize) {
        log.info("⭐【Service】分页查询API密钥，tenantId={}, status={}, appType={}, pageNum={}, pageSize={}",
                tenantId, status, appType, pageNum, pageSize);

        LambdaQueryWrapper<OpenApiKeys> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenApiKeys::getTenantId, tenantId);

        if (status != null) {
            wrapper.eq(OpenApiKeys::getStatus, status);
        }
        if (StringUtils.hasText(appType)) {
            wrapper.eq(OpenApiKeys::getAppType, appType);
        }

        wrapper.orderByDesc(OpenApiKeys::getCreateTime);

        Page<OpenApiKeys> page = new Page<>(pageNum, pageSize);
        IPage<OpenApiKeys> pageResult = openApiKeysMapper.selectPage(page, wrapper);

        List<OpenApiKeySpecDTO> dtoList = BeanCopyUtils.copyList(pageResult.getRecords(), OpenApiKeySpecDTO.class);
        log.info("✅【分页查询成功】total={}, records={}", pageResult.getTotal(), dtoList.size());

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenApiKeySpecDTO createOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO) {
        log.info("⭐【Service】创建API密钥，appId={}", openApiKeysDTO.getAppId());

        String lockKey = "lock:openapikeys:create:" + openApiKeysDTO.getAppId();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            locked = distributedLock.tryLock(lockKey, lockValue, 10);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            LambdaQueryWrapper<OpenApiKeys> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OpenApiKeys::getAppId, openApiKeysDTO.getAppId());
            if (openApiKeysMapper.selectCount(wrapper) > 0) {
                throw new BizException("应用ID已存在，appId=" + openApiKeysDTO.getAppId());
            }

            OpenApiKeys openApiKeys = BeanCopyUtils.copy(openApiKeysDTO, OpenApiKeys.class);
            Long id = idGeneratorRpc.generateUserId();
            openApiKeys.setId(id);

            // 如果DTO中没有提供appSecret，则生成一个
            if (openApiKeys.getAppSecret() == null || openApiKeys.getAppSecret().isEmpty()) {
                // 生成随机密钥（SHA256哈希）
                String randomSecret = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
                openApiKeys.setAppSecret(randomSecret);
            }

            openApiKeys.setCreateTime(LocalDateTime.now());
            openApiKeys.setUpdateTime(LocalDateTime.now());
            openApiKeys.setTotalRequests(0L);
            openApiKeys.setSuccessRequests(0L);
            openApiKeys.setFailedRequests(0L);
            openApiKeys.setExpireNotificationSent(0);

            int result = openApiKeysMapper.insert(openApiKeys);
            if (result <= 0) {
                throw new BizException("创建API密钥失败");
            }

            OpenApiKeySpecDTO createdDTO = BeanCopyUtils.copy(openApiKeys, OpenApiKeySpecDTO.class);

            String messageKey = "openapikeys-create-" + id;
            try {
                rocketMQUtils.syncSendWithKey(TOPIC_OPENAPI_KEYS, createdDTO, messageKey);
                log.info("📤【MQ已发送】API密钥创建消息，id={}", id);
            } catch (Exception e) {
                // MQ发送失败不影响主业务流程，只记录日志
                log.error("❌【MQ发送失败】API密钥创建消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
            }

            clearTenantCache(openApiKeysDTO.getTenantId());

            log.info("✅【创建成功】API密钥，id={}, appId={}", id, openApiKeysDTO.getAppId());
            return createdDTO;

        } finally {
            if (locked) {
                distributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO) {
        log.info("⭐【Service】更新API密钥，id={}", openApiKeysDTO.getId());

        OpenApiKeys existing = openApiKeysMapper.selectById(openApiKeysDTO.getId());
        if (existing == null) {
            throw new BizException("API密钥不存在，id=" + openApiKeysDTO.getId());
        }

        // 使用LambdaUpdateWrapper避免乐观锁问题
        LambdaUpdateWrapper<OpenApiKeys> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OpenApiKeys::getId, openApiKeysDTO.getId());
        updateWrapper.set(OpenApiKeys::getUpdateTime, LocalDateTime.now());

        // 只更新允许修改的字段
        if (StringUtils.hasText(openApiKeysDTO.getAppName())) {
            updateWrapper.set(OpenApiKeys::getAppName, openApiKeysDTO.getAppName());
        }
        if (StringUtils.hasText(openApiKeysDTO.getAppCode())) {
            updateWrapper.set(OpenApiKeys::getAppCode, openApiKeysDTO.getAppCode());
        }
        if (StringUtils.hasText(openApiKeysDTO.getAppType())) {
            updateWrapper.set(OpenApiKeys::getAppType, openApiKeysDTO.getAppType());
        }
        if (StringUtils.hasText(openApiKeysDTO.getAppCategory())) {
            updateWrapper.set(OpenApiKeys::getAppCategory, openApiKeysDTO.getAppCategory());
        }
        if (StringUtils.hasText(openApiKeysDTO.getAppOwner())) {
            updateWrapper.set(OpenApiKeys::getAppOwner, openApiKeysDTO.getAppOwner());
        }
        if (openApiKeysDTO.getAppOwnerId() != null) {
            updateWrapper.set(OpenApiKeys::getAppOwnerId, openApiKeysDTO.getAppOwnerId());
        }
        if (StringUtils.hasText(openApiKeysDTO.getAppDescription())) {
            updateWrapper.set(OpenApiKeys::getAppDescription, openApiKeysDTO.getAppDescription());
        }
        if (StringUtils.hasText(openApiKeysDTO.getPermissions())) {
            updateWrapper.set(OpenApiKeys::getPermissions, openApiKeysDTO.getPermissions());
        }
        if (StringUtils.hasText(openApiKeysDTO.getPermissionScope())) {
            updateWrapper.set(OpenApiKeys::getPermissionScope, openApiKeysDTO.getPermissionScope());
        }
        if (StringUtils.hasText(openApiKeysDTO.getAllowedResources())) {
            updateWrapper.set(OpenApiKeys::getAllowedResources, openApiKeysDTO.getAllowedResources());
        }
        if (openApiKeysDTO.getRateLimitEnabled() != null) {
            updateWrapper.set(OpenApiKeys::getRateLimitEnabled, openApiKeysDTO.getRateLimitEnabled());
        }
        if (openApiKeysDTO.getRateLimitPerSecond() != null) {
            updateWrapper.set(OpenApiKeys::getRateLimitPerSecond, openApiKeysDTO.getRateLimitPerSecond());
        }
        if (openApiKeysDTO.getRateLimitPerMinute() != null) {
            updateWrapper.set(OpenApiKeys::getRateLimitPerMinute, openApiKeysDTO.getRateLimitPerMinute());
        }
        if (openApiKeysDTO.getRateLimitPerHour() != null) {
            updateWrapper.set(OpenApiKeys::getRateLimitPerHour, openApiKeysDTO.getRateLimitPerHour());
        }
        if (openApiKeysDTO.getRateLimitPerDay() != null) {
            updateWrapper.set(OpenApiKeys::getRateLimitPerDay, openApiKeysDTO.getRateLimitPerDay());
        }
        if (openApiKeysDTO.getIpWhitelistEnabled() != null) {
            updateWrapper.set(OpenApiKeys::getIpWhitelistEnabled, openApiKeysDTO.getIpWhitelistEnabled());
        }
        if (StringUtils.hasText(openApiKeysDTO.getIpWhitelist())) {
            updateWrapper.set(OpenApiKeys::getIpWhitelist, openApiKeysDTO.getIpWhitelist());
        }
        if (StringUtils.hasText(openApiKeysDTO.getIpBlacklist())) {
            updateWrapper.set(OpenApiKeys::getIpBlacklist, openApiKeysDTO.getIpBlacklist());
        }
        if (openApiKeysDTO.getStatus() != null) {
            updateWrapper.set(OpenApiKeys::getStatus, openApiKeysDTO.getStatus());
        }
        if (StringUtils.hasText(openApiKeysDTO.getAuditStatus())) {
            updateWrapper.set(OpenApiKeys::getAuditStatus, openApiKeysDTO.getAuditStatus());
        }
        if (openApiKeysDTO.getExpireTime() != null) {
            updateWrapper.set(OpenApiKeys::getExpireTime, openApiKeysDTO.getExpireTime());
        }

        int result = openApiKeysMapper.update(null, updateWrapper);
        if (result <= 0) {
            throw new BizException("更新API密钥失败");
        }

        clearAllCache(openApiKeysDTO.getId(), openApiKeysDTO.getAppId(), openApiKeysDTO.getTenantId(),
                      openApiKeysDTO.getStatus(), openApiKeysDTO.getAppOwnerId());

        String messageKey = "openapikeys-update-" + openApiKeysDTO.getId();
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_OPENAPI_KEYS, openApiKeysDTO, messageKey);
            log.info("📤【MQ已发送】API密钥更新消息，id={}", openApiKeysDTO.getId());
        } catch (Exception e) {
            log.error("❌【MQ发送失败】API密钥更新消息，id={}, key={}, error={}", openApiKeysDTO.getId(), messageKey, e.getMessage());
        }

        log.info("✅【更新成功】API密钥，id={}", openApiKeysDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOpenApiKeys(Long id) {
        log.info("⭐【Service】删除API密钥，id={}", id);

        OpenApiKeys existing = openApiKeysMapper.selectById(id);
        if (existing == null) {
            throw new BizException("API密钥不存在，id=" + id);
        }

        int result = openApiKeysMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除API密钥失败");
        }

        clearAllCache(id, existing.getAppId(), existing.getTenantId(), existing.getStatus(), existing.getAppOwnerId());

        OpenApiKeySpecDTO deletedDTO = BeanCopyUtils.copy(existing, OpenApiKeySpecDTO.class);
        String messageKey = "openapikeys-delete-" + id;
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_OPENAPI_KEYS, deletedDTO, messageKey);
            log.info("📤【MQ已发送】API密钥删除消息，id={}", id);
        } catch (Exception e) {
            log.error("❌【MQ发送失败】API密钥删除消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
        }

        log.info("✅【删除成功】API密钥，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOpenApiKeysStatus(Long id, Integer status, String auditStatus, String auditRemark) {
        log.info("⭐【Service】更新API密钥状态，id={}, status={}, auditStatus={}", id, status, auditStatus);

        OpenApiKeys existing = openApiKeysMapper.selectById(id);
        if (existing == null) {
            throw new BizException("API密钥不存在，id=" + id);
        }

        // 使用LambdaUpdateWrapper避免乐观锁问题
        LambdaUpdateWrapper<OpenApiKeys> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OpenApiKeys::getId, id);
        updateWrapper.set(OpenApiKeys::getStatus, status);
        updateWrapper.set(OpenApiKeys::getUpdateTime, LocalDateTime.now());

        if (StringUtils.hasText(auditStatus)) {
            updateWrapper.set(OpenApiKeys::getAuditStatus, auditStatus);
        }

        int result = openApiKeysMapper.update(null, updateWrapper);
        if (result <= 0) {
            throw new BizException("更新API密钥状态失败");
        }

        clearAllCache(id, existing.getAppId(), existing.getTenantId(), existing.getStatus(), existing.getAppOwnerId());

        OpenApiKeySpecDTO dto = BeanCopyUtils.copy(existing, OpenApiKeySpecDTO.class);
        String messageKey = "openapikeys-status-" + id;
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_OPENAPI_KEYS, dto, messageKey);
            log.info("📤【MQ已发送】API密钥状态更新消息，id={}", id);
        } catch (Exception e) {
            log.error("❌【MQ发送失败】API密钥状态更新消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
        }

        log.info("✅【更新成功】API密钥状态，id={}", id);
    }

    @Override
    public int countOpenApiKeysByTenantId(Long tenantId) {
        log.info("⭐【Service】统计租户API密钥数量，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("openapikeys", "count", String.valueOf(tenantId));
        Integer cached = redisUtils.get(cacheKey, Integer.class);
        if (cached != null) {
            log.info("✅【缓存命中】API密钥数量统计，tenantId={}, count={}", tenantId, cached);
            return cached;
        }

        LambdaQueryWrapper<OpenApiKeys> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenApiKeys::getTenantId, tenantId);
        Integer count = openApiKeysMapper.selectCount(wrapper).intValue();

        redisUtils.set(cacheKey, count, CACHE_EXPIRE_COUNT);
        log.info("💾【缓存已存】API密钥数量统计，tenantId={}, count={}", tenantId, count);

        return count;
    }

    private String buildCacheKey(String module, String type, String identifier) {
        return String.format("duda:user:%s:%s:%s", module, type, identifier);
    }

    private void clearTenantCache(Long tenantId) {
        if (tenantId != null) {
            String tenantCacheKey = buildCacheKey("openapikeys", "tenant", String.valueOf(tenantId));
            redisUtils.delete(tenantCacheKey);
            redisUtils.delete(buildCacheKey("openapikeys", "count", String.valueOf(tenantId)));
            log.info("🗑️【缓存已清除】租户API密钥列表，tenantId={}", tenantId);
        }
    }

    private void clearAllCache(Long id, String appId, Long tenantId, Integer status, Long appOwnerId) {
        String idCacheKey = buildCacheKey("openapikeys", "id", String.valueOf(id));
        redisUtils.delete(idCacheKey);

        if (StringUtils.hasText(appId) && tenantId != null) {
            String appIdCacheKey = buildCacheKey("openapikeys", "appid", tenantId + ":" + appId);
            redisUtils.delete(appIdCacheKey);
        }

        if (tenantId != null) {
            clearTenantCache(tenantId);

            if (status != null) {
                String statusCacheKey = buildCacheKey("openapikeys", "status", tenantId + ":" + status);
                redisUtils.delete(statusCacheKey);
            }

            if (appOwnerId != null) {
                String ownerCacheKey = buildCacheKey("openapikeys", "owner", tenantId + ":" + appOwnerId);
                redisUtils.delete(ownerCacheKey);
            }
        }

        log.info("🗑️【缓存已清除】API密钥所有相关缓存，id={}", id);
    }
}
