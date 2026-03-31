package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.entity.Tenant;
import com.duda.tenant.entity.TenantUserRelation;
import com.duda.tenant.mapper.TenantMapper;
import com.duda.tenant.mapper.TenantUserRelationMapper;
import com.duda.tenant.service.TenantUserRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户用户关系Service实现
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantUserRelationServiceImpl extends ServiceImpl<TenantUserRelationMapper, TenantUserRelation>
        implements TenantUserRelationService {

    @Autowired
    private TenantUserRelationMapper relationMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long joinTenant(Long userId, Long tenantId, String roleCode, Integer isPrimary) {
        log.info("用户加入小程序: userId={}, tenantId={}, roleCode={}",
                userId, tenantId, roleCode);

        // 1. 检查租户是否存在
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            log.warn("租户不存在: tenantId={}", tenantId);
            throw new RuntimeException("租户不存在: tenantId=" + tenantId);
        }

        // 2. 检查是否已经关联
        LambdaQueryWrapper<TenantUserRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantUserRelation::getUserId, userId)
                .eq(TenantUserRelation::getTenantId, tenantId);

        TenantUserRelation existingRelation = relationMapper.selectOne(queryWrapper);
        if (existingRelation != null) {
            log.warn("用户已经关联到该小程序: userId={}, tenantId={}", userId, tenantId);
            return existingRelation.getId();
        }

        // 3. 创建新的关联关系
        TenantUserRelation relation = new TenantUserRelation();
        relation.setUserId(userId);
        relation.setTenantId(tenantId);
        relation.setTenantCode(tenant.getTenantCode());
        relation.setUserShard(0);  // 默认分片0
        relation.setRoleCode(roleCode);
        relation.setIsPrimary(isPrimary != null ? isPrimary : 0);
        relation.setStatus("active");
        relation.setJoinTime(LocalDateTime.now());
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());
        relation.setDeleted(0);

        relationMapper.insert(relation);

        log.info("用户加入小程序成功: id={}, userId={}, tenantId={}",
                relation.getId(), relation.getUserId(), relation.getTenantId());

        return relation.getId();
    }

    @Override
    public List<TenantUserRelation> getTenantsByUserId(Long userId) {
        log.info("查询用户的小程序列表: userId={}", userId);

        LambdaQueryWrapper<TenantUserRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantUserRelation::getUserId, userId)
                .orderByDesc(TenantUserRelation::getJoinTime);

        return relationMapper.selectList(queryWrapper);
    }

    @Override
    public List<TenantUserRelation> getUsersByTenantId(Long tenantId) {
        log.info("查询小程序的用户列表: tenantId={}", tenantId);

        LambdaQueryWrapper<TenantUserRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantUserRelation::getTenantId, tenantId)
                .orderByDesc(TenantUserRelation::getJoinTime);

        return relationMapper.selectList(queryWrapper);
    }

    @Override
    public Boolean checkRelation(Long userId, Long tenantId) {
        log.info("检查用户是否已关联小程序: userId={}, tenantId={}", userId, tenantId);

        LambdaQueryWrapper<TenantUserRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantUserRelation::getUserId, userId)
                .eq(TenantUserRelation::getTenantId, tenantId);

        Long count = relationMapper.selectCount(queryWrapper);
        return count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean leaveTenant(Long userId, Long tenantId) {
        log.info("用户离开小程序: userId={}, tenantId={}", userId, tenantId);

        LambdaQueryWrapper<TenantUserRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantUserRelation::getUserId, userId)
                .eq(TenantUserRelation::getTenantId, tenantId);

        TenantUserRelation relation = relationMapper.selectOne(queryWrapper);
        if (relation == null) {
            log.warn("关联关系不存在: userId={}, tenantId={}", userId, tenantId);
            return false;
        }

        int deleted = relationMapper.deleteById(relation.getId());
        log.info("用户离开小程序成功: userId={}, tenantId={}, deleted={}",
                userId, tenantId, deleted);

        return deleted > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, String status) {
        log.info("更新用户状态: id={}, status={}", id, status);

        TenantUserRelation relation = relationMapper.selectById(id);
        if (relation == null) {
            log.warn("关联关系不存在: id={}", id);
            throw new RuntimeException("关联关系不存在: id=" + id);
        }

        relation.setStatus(status);
        relation.setUpdateTime(LocalDateTime.now());

        int updated = relationMapper.updateById(relation);
        log.info("更新状态成功: id={}, status={}, updated={}", id, status, updated);

        return updated > 0;
    }
}
