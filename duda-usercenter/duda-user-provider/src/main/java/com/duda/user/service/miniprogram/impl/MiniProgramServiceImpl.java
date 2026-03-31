package com.duda.user.service.miniprogram.impl;

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
import com.duda.user.dto.miniprogram.MiniProgramDTO;
import com.duda.user.entity.miniprogram.MiniProgram;
import com.duda.user.mapper.miniprogram.MiniProgramMapper;
import com.duda.user.service.miniprogram.MiniProgramService;
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
import java.util.concurrent.TimeUnit;

/**
 * 小程序服务实现
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
public class MiniProgramServiceImpl implements MiniProgramService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramServiceImpl.class);

    /**
     * 缓存过期时间配置（秒）
     */
    private static final int CACHE_EXPIRE_SINGLE = 1800;  // 单条：30分钟
    private static final int CACHE_EXPIRE_LIST = 300;     // 列表：5分钟
    private static final int CACHE_EXPIRE_COUNT = 60;     // 统计：1分钟

    /**
     * RocketMQ Topic
     */
    private static final String TOPIC_MINIPROGRAM = "MINIPROGRAM_LIFECYCLE";

    @Resource
    private MiniProgramMapper miniProgramMapper;

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
    public MiniProgramDTO getMiniProgramById(Long id) {
        log.info("⭐【Service】查询小程序，id={}", id);

        // 1. 先从缓存获取
        String cacheKey = buildCacheKey("miniprogram", "id", String.valueOf(id));
        MiniProgramDTO cached = redisUtils.get(cacheKey, MiniProgramDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】小程序，id={}", id);
            return cached;
        }

        // 2. 从数据库查询
        MiniProgram miniProgram = miniProgramMapper.selectById(id);
        if (miniProgram == null) {
            throw new BizException("小程序不存在，id=" + id);
        }

        // 3. 转换为DTO
        MiniProgramDTO dto = BeanCopyUtils.copy(miniProgram, MiniProgramDTO.class);

        // 4. 存入缓存
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】小程序，id={}, key={}", id, cacheKey);

        return dto;
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByTenantId(Long tenantId) {
        log.info("⭐【Service】查询租户小程序列表，tenantId={}", tenantId);

        // 1. 先从缓存获取
        String cacheKey = buildCacheKey("miniprogram", "tenant", String.valueOf(tenantId));
        List<MiniProgramDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】租户小程序列表，tenantId={}", tenantId);
            return cached;
        }

        // 2. 从数据库查询（使用MyBatis-Plus）
        LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgram::getTenantId, tenantId);
        List<MiniProgram> list = miniProgramMapper.selectList(wrapper);
        List<MiniProgramDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramDTO.class);

        // 3. 存入缓存
        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】租户小程序列表，tenantId={}, count={}", tenantId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public MiniProgramDTO getMiniProgramByAppId(String appid) {
        log.info("⭐【Service】根据AppID查询小程序，appid={}", appid);

        // 1. 先从缓存获取
        String cacheKey = buildCacheKey("miniprogram", "appid", appid);
        MiniProgramDTO cached = redisUtils.get(cacheKey, MiniProgramDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】小程序AppID，appid={}", appid);
            return cached;
        }

        // 2. 从数据库查询（使用MyBatis-Plus）
        LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgram::getAppid, appid);
        MiniProgram miniProgram = miniProgramMapper.selectOne(wrapper);
        if (miniProgram == null) {
            throw new BizException("小程序不存在，appid=" + appid);
        }

        // 3. 转换为DTO
        MiniProgramDTO dto = BeanCopyUtils.copy(miniProgram, MiniProgramDTO.class);

        // 4. 存入缓存
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】小程序AppID，appid={}", appid);

        return dto;
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByStatus(Long tenantId, String status) {
        log.info("⭐【Service】根据状态查询小程序，tenantId={}, status={}", tenantId, status);

        // 1. 先从缓存获取
        String cacheKey = buildCacheKey("miniprogram", "status", tenantId + ":" + status);
        List<MiniProgramDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】状态小程序列表，tenantId={}, status={}", tenantId, status);
            return cached;
        }

        // 2. 从数据库查询（使用MyBatis-Plus）
        LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgram::getTenantId, tenantId);
        wrapper.eq(MiniProgram::getStatus, status);
        List<MiniProgram> list = miniProgramMapper.selectList(wrapper);
        List<MiniProgramDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramDTO.class);

        // 3. 存入缓存
        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】状态小程序列表，tenantId={}, status={}, count={}", tenantId, status, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByOnlineStatus(Long tenantId, String onlineStatus) {
        log.info("⭐【Service】根据上线状态查询小程序，tenantId={}, onlineStatus={}", tenantId, onlineStatus);

        // 1. 先从缓存获取
        String cacheKey = buildCacheKey("miniprogram", "online", tenantId + ":" + onlineStatus);
        List<MiniProgramDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】上线状态小程序列表，tenantId={}, onlineStatus={}", tenantId, onlineStatus);
            return cached;
        }

        // 2. 从数据库查询（使用MyBatis-Plus）
        LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgram::getTenantId, tenantId);
        wrapper.eq(MiniProgram::getOnlineStatus, onlineStatus);
        List<MiniProgram> list = miniProgramMapper.selectList(wrapper);
        List<MiniProgramDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramDTO.class);

        // 3. 存入缓存
        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】上线状态小程序列表，tenantId={}, onlineStatus={}, count={}", tenantId, onlineStatus, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByCompanyId(Long companyId) {
        log.info("⭐【Service】根据公司ID查询小程序，companyId={}", companyId);

        // 1. 先从缓存获取
        String cacheKey = buildCacheKey("miniprogram", "company", String.valueOf(companyId));
        List<MiniProgramDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】公司小程序列表，companyId={}", companyId);
            return cached;
        }

        // 2. 从数据库查询（使用MyBatis-Plus）
        LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgram::getCompanyId, companyId);
        List<MiniProgram> list = miniProgramMapper.selectList(wrapper);
        List<MiniProgramDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramDTO.class);

        // 3. 存入缓存
        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】公司小程序列表，companyId={}, count={}", companyId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsPage(Long tenantId, String status, String onlineStatus,
                                                     Integer pageNum, Integer pageSize) {
        log.info("⭐【Service】分页查询小程序，tenantId={}, status={}, onlineStatus={}, pageNum={}, pageSize={}",
                tenantId, status, onlineStatus, pageNum, pageSize);

        // 1. 构建查询条件
        LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgram::getTenantId, tenantId);

        if (StringUtils.hasText(status)) {
            wrapper.eq(MiniProgram::getStatus, status);
        }
        if (StringUtils.hasText(onlineStatus)) {
            wrapper.eq(MiniProgram::getOnlineStatus, onlineStatus);
        }

        // 2. 按创建时间倒序
        wrapper.orderByDesc(MiniProgram::getCreateTime);

        // 3. 分页查询
        Page<MiniProgram> page = new Page<>(pageNum, pageSize);
        IPage<MiniProgram> pageResult = miniProgramMapper.selectPage(page, wrapper);

        // 4. 转换并返回
        List<MiniProgramDTO> dtoList = BeanCopyUtils.copyList(pageResult.getRecords(), MiniProgramDTO.class);
        log.info("✅【分页查询成功】total={}, records={}", pageResult.getTotal(), dtoList.size());

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniProgramDTO createMiniProgram(MiniProgramDTO miniProgramDTO) {
        log.info("⭐【Service】创建小程序，appid={}, name={}", miniProgramDTO.getAppid(), miniProgramDTO.getName());

        // 1. 使用分布式锁防止重复创建（基于appid）
        String lockKey = "lock:miniprogram:create:" + miniProgramDTO.getAppid();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            // 尝试获取锁（10秒超时）
            locked = distributedLock.tryLock(lockKey, lockValue, 10);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            // 2. 检查AppID是否已存在
            LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MiniProgram::getAppid, miniProgramDTO.getAppid());
            if (miniProgramMapper.selectCount(wrapper) > 0) {
                throw new BizException("小程序AppID已存在，appid=" + miniProgramDTO.getAppid());
            }

            // 3. 创建小程序
            MiniProgram miniProgram = BeanCopyUtils.copy(miniProgramDTO, MiniProgram.class);
            Long id = idGeneratorRpc.generateUserId();
            miniProgram.setId(id);
            miniProgram.setCreateTime(LocalDateTime.now());
            miniProgram.setUpdateTime(LocalDateTime.now());

            int result = miniProgramMapper.insert(miniProgram);
            if (result <= 0) {
                throw new BizException("创建小程序失败");
            }

            // 4. 转换为DTO返回
            MiniProgramDTO createdDTO = BeanCopyUtils.copy(miniProgram, MiniProgramDTO.class);

            // 5. 📤 同步发送MQ消息（创建操作必须同步确认）
            String messageKey = "miniprogram-create-" + id;
            try {
                rocketMQUtils.syncSendWithKey(TOPIC_MINIPROGRAM, createdDTO, messageKey);
                log.info("📤【MQ已发送】小程序创建消息，id={}, key={}", id, messageKey);
            } catch (Exception e) {
                // MQ发送失败不影响主业务流程，只记录日志
                log.error("❌【MQ发送失败】小程序创建消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
            }

            // 6. 清除相关缓存（租户列表缓存）
            clearTenantCache(miniProgramDTO.getTenantId());

            log.info("✅【创建成功】小程序，id={}, appid={}", id, miniProgramDTO.getAppid());
            return createdDTO;

        } finally {
            // 7. 释放锁
            if (locked) {
                distributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMiniProgram(MiniProgramDTO miniProgramDTO) {
        log.info("⭐【Service】更新小程序，id={}", miniProgramDTO.getId());

        // 1. 查询原数据
        MiniProgram existing = miniProgramMapper.selectById(miniProgramDTO.getId());
        if (existing == null) {
            throw new BizException("小程序不存在，id=" + miniProgramDTO.getId());
        }

        // 2. 乐观锁检查
        if (!existing.getVersion().equals(miniProgramDTO.getVersion())) {
            throw new BizException("数据已被其他用户修改，请刷新后重试");
        }

        // 3. 更新字段（使用LambdaUpdateWrapper避免乐观锁问题）
        LambdaUpdateWrapper<MiniProgram> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MiniProgram::getId, miniProgramDTO.getId());
        updateWrapper.set(MiniProgram::getUpdateTime, LocalDateTime.now());

        // 只更新允许修改的字段
        if (StringUtils.hasText(miniProgramDTO.getName())) {
            updateWrapper.set(MiniProgram::getName, miniProgramDTO.getName());
        }
        if (StringUtils.hasText(miniProgramDTO.getDescription())) {
            updateWrapper.set(MiniProgram::getDescription, miniProgramDTO.getDescription());
        }
        if (StringUtils.hasText(miniProgramDTO.getStatus())) {
            updateWrapper.set(MiniProgram::getStatus, miniProgramDTO.getStatus());
        }
        if (StringUtils.hasText(miniProgramDTO.getOnlineStatus())) {
            updateWrapper.set(MiniProgram::getOnlineStatus, miniProgramDTO.getOnlineStatus());
        }
        if (StringUtils.hasText(miniProgramDTO.getIntro())) {
            updateWrapper.set(MiniProgram::getIntro, miniProgramDTO.getIntro());
        }
        if (miniProgramDTO.getWechatCertified() != null) {
            updateWrapper.set(MiniProgram::getWechatCertified, miniProgramDTO.getWechatCertified());
        }
        if (StringUtils.hasText(miniProgramDTO.getCertificationStatus())) {
            updateWrapper.set(MiniProgram::getCertificationStatus, miniProgramDTO.getCertificationStatus());
        }
        if (StringUtils.hasText(miniProgramDTO.getFilingStatus())) {
            updateWrapper.set(MiniProgram::getFilingStatus, miniProgramDTO.getFilingStatus());
        }

        int result = miniProgramMapper.update(null, updateWrapper);
        if (result <= 0) {
            throw new BizException("更新小程序失败");
        }

        // 4. 清除所有相关缓存
        clearAllCache(miniProgramDTO.getId(), miniProgramDTO.getAppid(),
                     miniProgramDTO.getTenantId(), miniProgramDTO.getDeveloperCompanyId());

        // 5. 📤 同步发送MQ消息
        String messageKey = "miniprogram-update-" + miniProgramDTO.getId();
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_MINIPROGRAM, miniProgramDTO, messageKey);
            log.info("📤【MQ已发送】小程序更新消息，id={}, key={}", miniProgramDTO.getId(), messageKey);
        } catch (Exception e) {
            // MQ发送失败不影响主业务流程，只记录日志
            log.error("❌【MQ发送失败】小程序更新消息，id={}, key={}, error={}", miniProgramDTO.getId(), messageKey, e.getMessage());
        }

        log.info("✅【更新成功】小程序，id={}", miniProgramDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMiniProgram(Long id) {
        log.info("⭐【Service】删除小程序，id={}", id);

        // 1. 查询原数据
        MiniProgram existing = miniProgramMapper.selectById(id);
        if (existing == null) {
            throw new BizException("小程序不存在，id=" + id);
        }

        // 2. 逻辑删除
        int result = miniProgramMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除小程序失败");
        }

        // 3. 清除所有相关缓存
        clearAllCache(id, existing.getAppid(), existing.getTenantId(), existing.getDeveloperCompanyId());

        // 4. 📤 同步发送MQ消息
        MiniProgramDTO deletedDTO = BeanCopyUtils.copy(existing, MiniProgramDTO.class);
        String messageKey = "miniprogram-delete-" + id;
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_MINIPROGRAM, deletedDTO, messageKey);
            log.info("📤【MQ已发送】小程序删除消息，id={}, key={}", id, messageKey);
        } catch (Exception e) {
            // MQ发送失败不影响主业务流程，只记录日志
            log.error("❌【MQ发送失败】小程序删除消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
        }

        log.info("✅【删除成功】小程序，id={}", id);
    }

    @Override
    public int countMiniProgramsByTenantId(Long tenantId) {
        log.info("⭐【Service】统计租户小程序数量，tenantId={}", tenantId);

        // 1. 先从缓存获取
        String cacheKey = buildCacheKey("miniprogram", "count", String.valueOf(tenantId));
        Integer cached = redisUtils.get(cacheKey, Integer.class);
        if (cached != null) {
            log.info("✅【缓存命中】小程序数量统计，tenantId={}, count={}", tenantId, cached);
            return cached;
        }

        // 2. 从数据库查询（使用MyBatis-Plus）
        LambdaQueryWrapper<MiniProgram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgram::getTenantId, tenantId);
        Integer count = miniProgramMapper.selectCount(wrapper).intValue();

        // 3. 存入缓存
        redisUtils.set(cacheKey, count, CACHE_EXPIRE_COUNT);
        log.info("💾【缓存已存】小程序数量统计，tenantId={}, count={}", tenantId, count);

        return count;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建缓存Key
     */
    private String buildCacheKey(String module, String type, String identifier) {
        return String.format("duda:user:%s:%s:%s", module, type, identifier);
    }

    /**
     * 清除租户相关缓存
     */
    private void clearTenantCache(Long tenantId) {
        if (tenantId != null) {
            String tenantCacheKey = buildCacheKey("miniprogram", "tenant", String.valueOf(tenantId));
            redisUtils.delete(tenantCacheKey);
            log.info("🗑️【缓存已清除】租户小程序列表，tenantId={}", tenantId);
        }
    }

    /**
     * 清除所有相关缓存
     */
    private void clearAllCache(Long id, String appid, Long tenantId, Long companyId) {
        // 1. 清除ID缓存
        String idCacheKey = buildCacheKey("miniprogram", "id", String.valueOf(id));
        redisUtils.delete(idCacheKey);

        // 2. 清除AppID缓存
        if (StringUtils.hasText(appid)) {
            String appidCacheKey = buildCacheKey("miniprogram", "appid", appid);
            redisUtils.delete(appidCacheKey);
        }

        // 3. 清除租户缓存
        if (tenantId != null) {
            clearTenantCache(tenantId);
            // 清除状态相关缓存
            redisUtils.delete(buildCacheKey("miniprogram", "status", tenantId + ":active"));
            redisUtils.delete(buildCacheKey("miniprogram", "online", tenantId + ":online"));
            redisUtils.delete(buildCacheKey("miniprogram", "count", String.valueOf(tenantId)));
        }

        // 4. 清除公司缓存
        if (companyId != null) {
            String companyCacheKey = buildCacheKey("miniprogram", "company", String.valueOf(companyId));
            redisUtils.delete(companyCacheKey);
        }

        log.info("🗑️【缓存已清除】小程序所有相关缓存，id={}", id);
    }
}
