package com.duda.user.service.miniprogram.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.redis.RedisUtils;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.miniprogram.MiniProgramDevelopmentTaskDTO;
import com.duda.user.entity.miniprogram.MiniProgramDevelopmentTask;
import com.duda.user.mapper.miniprogram.MiniProgramDevelopmentTaskMapper;
import com.duda.user.service.miniprogram.MiniProgramDevelopmentTaskService;
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
 * 小程序开发任务服务实现
 *
 * 技术要点：
 * 1. CacheAside缓存模式：单条30分钟，列表5分钟，统计1分钟
 * 2. RedisDistributedLock：创建操作使用分布式锁防止重复（基于taskNo）
 * 3. RocketMQ消息：同步发送生命周期事件消息
 * 4. 租户隔离：所有查询必须过滤tenant_id
 * 5. 乐观锁：使用version字段防止并发更新
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class MiniProgramDevelopmentTaskServiceImpl implements MiniProgramDevelopmentTaskService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramDevelopmentTaskServiceImpl.class);

    private static final int CACHE_EXPIRE_SINGLE = 1800;  // 30分钟
    private static final int CACHE_EXPIRE_LIST = 300;     // 5分钟
    private static final int CACHE_EXPIRE_COUNT = 60;     // 1分钟

    private static final String TOPIC_TASK = "MINIPROGRAM_TASK_LIFECYCLE";

    @Resource
    private MiniProgramDevelopmentTaskMapper taskMapper;

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
    public MiniProgramDevelopmentTaskDTO getTaskById(Long id) {
        log.info("⭐【Service】查询开发任务，id={}", id);

        String cacheKey = buildCacheKey("task", "id", String.valueOf(id));
        MiniProgramDevelopmentTaskDTO cached = redisUtils.get(cacheKey, MiniProgramDevelopmentTaskDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】开发任务，id={}", id);
            return cached;
        }

        MiniProgramDevelopmentTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException("开发任务不存在，id=" + id);
        }

        MiniProgramDevelopmentTaskDTO dto = BeanCopyUtils.copy(task, MiniProgramDevelopmentTaskDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】开发任务，id={}", id);

        return dto;
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByTenantId(Long tenantId) {
        log.info("⭐【Service】查询租户任务列表，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("task", "tenant", String.valueOf(tenantId));
        List<MiniProgramDevelopmentTaskDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】租户任务列表，tenantId={}", tenantId);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramDevelopmentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramDevelopmentTask::getTenantId, tenantId);
        List<MiniProgramDevelopmentTask> list = taskMapper.selectList(wrapper);
        List<MiniProgramDevelopmentTaskDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramDevelopmentTaskDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】租户任务列表，tenantId={}, count={}", tenantId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public MiniProgramDevelopmentTaskDTO getTaskByNo(String taskNo) {
        log.info("⭐【Service】根据任务编号查询，taskNo={}", taskNo);

        String cacheKey = buildCacheKey("task", "no", taskNo);
        MiniProgramDevelopmentTaskDTO cached = redisUtils.get(cacheKey, MiniProgramDevelopmentTaskDTO.class);
        if (cached != null) {
            log.info("✅【缓存命中】任务编号，taskNo={}", taskNo);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramDevelopmentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramDevelopmentTask::getTaskNo, taskNo);
        MiniProgramDevelopmentTask task = taskMapper.selectOne(wrapper);

        if (task == null) {
            throw new BizException("开发任务不存在，taskNo=" + taskNo);
        }

        MiniProgramDevelopmentTaskDTO dto = BeanCopyUtils.copy(task, MiniProgramDevelopmentTaskDTO.class);
        redisUtils.set(cacheKey, dto, CACHE_EXPIRE_SINGLE);
        log.info("💾【缓存已存】任务编号，taskNo={}", taskNo);

        return dto;
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByClient(Long tenantId, Long clientCompanyId) {
        log.info("⭐【Service】根据客户公司查询任务，tenantId={}, clientCompanyId={}", tenantId, clientCompanyId);

        String cacheKey = buildCacheKey("task", "client", tenantId + ":" + clientCompanyId);
        List<MiniProgramDevelopmentTaskDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】客户公司任务列表，clientCompanyId={}", clientCompanyId);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramDevelopmentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramDevelopmentTask::getTenantId, tenantId);
        wrapper.eq(MiniProgramDevelopmentTask::getClientCompanyId, clientCompanyId);
        List<MiniProgramDevelopmentTask> list = taskMapper.selectList(wrapper);
        List<MiniProgramDevelopmentTaskDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramDevelopmentTaskDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】客户公司任务列表，clientCompanyId={}, count={}", clientCompanyId, dtoList.size());
        }

        return dtoList;
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByStatus(Long tenantId, String taskStatus) {
        log.info("⭐【Service】根据状态查询任务，tenantId={}, status={}", tenantId, taskStatus);

        String cacheKey = buildCacheKey("task", "status", tenantId + ":" + taskStatus);
        List<MiniProgramDevelopmentTaskDTO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.info("✅【缓存命中】状态任务列表，tenantId={}, status={}", tenantId, taskStatus);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramDevelopmentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramDevelopmentTask::getTenantId, tenantId);
        wrapper.eq(MiniProgramDevelopmentTask::getTaskStatus, taskStatus);
        List<MiniProgramDevelopmentTask> list = taskMapper.selectList(wrapper);
        List<MiniProgramDevelopmentTaskDTO> dtoList = BeanCopyUtils.copyList(list, MiniProgramDevelopmentTaskDTO.class);

        if (!dtoList.isEmpty()) {
            redisUtils.set(cacheKey, dtoList, CACHE_EXPIRE_LIST);
            log.info("💾【缓存已存】状态任务列表，tenantId={}, status={}, count={}", tenantId, taskStatus, dtoList.size());
        }

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniProgramDevelopmentTaskDTO createTask(MiniProgramDevelopmentTaskDTO taskDTO) {
        log.info("⭐【Service】创建开发任务，taskNo={}", taskDTO.getTaskNo());

        String lockKey = "lock:task:create:" + taskDTO.getTaskNo();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            locked = distributedLock.tryLock(lockKey, lockValue, 10);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            LambdaQueryWrapper<MiniProgramDevelopmentTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MiniProgramDevelopmentTask::getTaskNo, taskDTO.getTaskNo());
            if (taskMapper.selectCount(wrapper) > 0) {
                throw new BizException("任务编号已存在，taskNo=" + taskDTO.getTaskNo());
            }

            MiniProgramDevelopmentTask task = BeanCopyUtils.copy(taskDTO, MiniProgramDevelopmentTask.class);
            Long id = idGeneratorRpc.generateUserId();
            task.setId(id);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            int result = taskMapper.insert(task);
            if (result <= 0) {
                throw new BizException("创建开发任务失败");
            }

            MiniProgramDevelopmentTaskDTO createdDTO = BeanCopyUtils.copy(task, MiniProgramDevelopmentTaskDTO.class);

            String messageKey = "task-create-" + id;
            try {
                rocketMQUtils.syncSendWithKey(TOPIC_TASK, createdDTO, messageKey);
                log.info("📤【MQ已发送】任务创建消息，id={}", id);
            } catch (Exception e) {
                log.error("❌【MQ发送失败】任务创建消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
            }

            clearTenantCache(taskDTO.getTenantId());

            log.info("✅【创建成功】开发任务，id={}, taskNo={}", id, taskDTO.getTaskNo());
            return createdDTO;

        } finally {
            if (locked) {
                distributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(MiniProgramDevelopmentTaskDTO taskDTO) {
        log.info("⭐【Service】更新开发任务，id={}", taskDTO.getId());

        MiniProgramDevelopmentTask existing = taskMapper.selectById(taskDTO.getId());
        if (existing == null) {
            throw new BizException("开发任务不存在，id=" + taskDTO.getId());
        }

        if (!existing.getVersion().equals(taskDTO.getVersion())) {
            throw new BizException("数据已被其他用户修改，请刷新后重试");
        }

        MiniProgramDevelopmentTask task = BeanCopyUtils.copy(taskDTO, MiniProgramDevelopmentTask.class);
        task.setUpdateTime(LocalDateTime.now());
        task.setVersion(existing.getVersion() + 1);

        int result = taskMapper.updateById(task);
        if (result <= 0) {
            throw new BizException("更新开发任务失败");
        }

        clearAllCache(taskDTO.getId(), taskDTO.getTaskNo(), taskDTO.getTenantId(), taskDTO.getClientCompanyId());

        String messageKey = "task-update-" + taskDTO.getId();
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_TASK, taskDTO, messageKey);
            log.info("📤【MQ已发送】任务更新消息，id={}", taskDTO.getId());
        } catch (Exception e) {
            log.error("❌【MQ发送失败】任务更新消息，id={}, key={}, error={}", taskDTO.getId(), messageKey, e.getMessage());
        }

        log.info("✅【更新成功】开发任务，id={}", taskDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        log.info("⭐【Service】删除开发任务，id={}", id);

        MiniProgramDevelopmentTask existing = taskMapper.selectById(id);
        if (existing == null) {
            throw new BizException("开发任务不存在，id=" + id);
        }

        int result = taskMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除开发任务失败");
        }

        clearAllCache(id, existing.getTaskNo(), existing.getTenantId(), existing.getClientCompanyId());

        MiniProgramDevelopmentTaskDTO deletedDTO = BeanCopyUtils.copy(existing, MiniProgramDevelopmentTaskDTO.class);
        String messageKey = "task-delete-" + id;
        try {
            rocketMQUtils.syncSendWithKey(TOPIC_TASK, deletedDTO, messageKey);
            log.info("📤【MQ已发送】任务删除消息，id={}", id);
        } catch (Exception e) {
            log.error("❌【MQ发送失败】任务删除消息，id={}, key={}, error={}", id, messageKey, e.getMessage());
        }

        log.info("✅【删除成功】开发任务，id={}", id);
    }

    @Override
    public int countTasksByTenantId(Long tenantId) {
        log.info("⭐【Service】统计租户任务数量，tenantId={}", tenantId);

        String cacheKey = buildCacheKey("task", "count", String.valueOf(tenantId));
        Integer cached = redisUtils.get(cacheKey, Integer.class);
        if (cached != null) {
            log.info("✅【缓存命中】任务数量统计，tenantId={}, count={}", tenantId, cached);
            return cached;
        }

        LambdaQueryWrapper<MiniProgramDevelopmentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MiniProgramDevelopmentTask::getTenantId, tenantId);
        Integer count = Math.toIntExact(taskMapper.selectCount(wrapper));

        redisUtils.set(cacheKey, count, CACHE_EXPIRE_COUNT);
        log.info("💾【缓存已存】任务数量统计，tenantId={}, count={}", tenantId, count);

        return count;
    }

    private String buildCacheKey(String module, String type, String identifier) {
        return String.format("duda:user:%s:%s:%s", module, type, identifier);
    }

    private void clearTenantCache(Long tenantId) {
        if (tenantId != null) {
            String tenantCacheKey = buildCacheKey("task", "tenant", String.valueOf(tenantId));
            redisUtils.delete(tenantCacheKey);
            redisUtils.delete(buildCacheKey("task", "count", String.valueOf(tenantId)));
            log.info("🗑️【缓存已清除】租户任务列表，tenantId={}", tenantId);
        }
    }

    private void clearAllCache(Long id, String taskNo, Long tenantId, Long clientCompanyId) {
        String idCacheKey = buildCacheKey("task", "id", String.valueOf(id));
        redisUtils.delete(idCacheKey);

        if (StringUtils.hasText(taskNo)) {
            String noCacheKey = buildCacheKey("task", "no", taskNo);
            redisUtils.delete(noCacheKey);
        }

        if (tenantId != null) {
            clearTenantCache(tenantId);
        }

        if (clientCompanyId != null) {
            String clientCacheKey = buildCacheKey("task", "client", tenantId + ":" + clientCompanyId);
            redisUtils.delete(clientCacheKey);
        }

        log.info("🗑️【缓存已清除】任务所有相关缓存，id={}", id);
    }
}
