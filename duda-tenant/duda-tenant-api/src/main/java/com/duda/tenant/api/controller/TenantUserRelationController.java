package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.TenantUserRelationDTO;
import com.duda.tenant.api.service.TenantUserRelationService;
import com.duda.tenant.api.vo.ResultVO;
import com.duda.tenant.api.vo.TenantUserRelationJoinVO;
import com.duda.tenant.api.vo.TenantUserRelationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户用户关系Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/relation")
public class TenantUserRelationController {

    @Autowired
    private TenantUserRelationService relationService;

    /**
     * 用户加入小程序（创建用户-租户关联）
     *
     * @param joinVO 加入请求VO
     * @return 创建的关系VO
     */
    @PostMapping("/join")
    public ResultVO<TenantUserRelationVO> joinTenant(@RequestBody TenantUserRelationJoinVO joinVO) {
        log.info("用户加入小程序: userId={}, tenantId={}, userRole={}",
                joinVO.getUserId(), joinVO.getTenantId(), joinVO.getUserRole());

        // 调用Service创建关联
        TenantUserRelationDTO relationDTO = relationService.joinTenant(
                joinVO.getUserId(),
                joinVO.getTenantId(),
                joinVO.getUserRole(),
                0  // isPrimary=0
        );

        return ResultVO.success("加入成功", dtoToVo(relationDTO));
    }

    /**
     * 查询用户的小程序列表
     *
     * @param userId 用户ID
     * @return 小程序列表
     */
    @GetMapping("/user/{userId}")
    public ResultVO<List<TenantUserRelationVO>> getTenantsByUserId(@PathVariable("userId") Long userId) {
        log.info("查询用户的小程序列表: userId={}", userId);

        List<TenantUserRelationDTO> relations = relationService.getTenantsByUserId(userId);

        List<TenantUserRelationVO> vos = relations.stream()
                .map(this::dtoToVo)
                .collect(Collectors.toList());

        return ResultVO.success(vos);
    }

    /**
     * 查询小程序的用户列表
     *
     * @param tenantId 租户ID
     * @return 用户列表
     */
    @GetMapping("/tenant/{tenantId}")
    public ResultVO<List<TenantUserRelationVO>> getUsersByTenantId(@PathVariable("tenantId") Long tenantId) {
        log.info("查询小程序的用户列表: tenantId={}", tenantId);

        List<TenantUserRelationDTO> relations = relationService.getUsersByTenantId(tenantId);

        List<TenantUserRelationVO> vos = relations.stream()
                .map(this::dtoToVo)
                .collect(Collectors.toList());

        return ResultVO.success(vos);
    }

    /**
     * 检查用户是否已关联某小程序
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 是否已关联
     */
    @GetMapping("/check")
    public ResultVO<Boolean> checkRelation(
            @RequestParam("userId") Long userId,
            @RequestParam("tenantId") Long tenantId) {
        log.info("检查用户是否已关联小程序: userId={}, tenantId={}", userId, tenantId);

        Boolean hasRelation = relationService.checkRelation(userId, tenantId);

        return ResultVO.success(hasRelation);
    }

    /**
     * 用户离开小程序（删除关联）
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 是否成功
     */
    @DeleteMapping("/leave")
    public ResultVO<Boolean> leaveTenant(
            @RequestParam("userId") Long userId,
            @RequestParam("tenantId") Long tenantId) {
        log.info("用户离开小程序: userId={}, tenantId={}", userId, tenantId);

        Boolean success = relationService.leaveTenant(userId, tenantId);

        return ResultVO.success(success ? "离开成功" : "离开失败", success);
    }

    /**
     * 根据ID查询关系
     *
     * @param id 关系ID
     * @return 关系VO
     */
    @GetMapping("/{id}")
    public ResultVO<TenantUserRelationVO> getRelationById(@PathVariable("id") Long id) {
        log.info("查询关联关系: id={}", id);

        TenantUserRelationDTO relationDTO = relationService.getRelationById(id);

        if (relationDTO == null) {
            return ResultVO.error("关联关系不存在");
        }

        return ResultVO.success(dtoToVo(relationDTO));
    }

    /**
     * 更新用户状态
     *
     * @param id 关系ID
     * @param status 状态（active/inactive）
     * @return 是否成功
     */
    @PutMapping("/status")
    public ResultVO<Boolean> updateStatus(
            @RequestParam("id") Long id,
            @RequestParam("status") String status) {
        log.info("更新用户状态: id={}, status={}", id, status);

        Boolean success = relationService.updateStatus(id, status);

        return ResultVO.success(success ? "更新成功" : "更新失败", success);
    }

    /**
     * DTO转VO
     */
    private TenantUserRelationVO dtoToVo(TenantUserRelationDTO dto) {
        if (dto == null) {
            return null;
        }

        TenantUserRelationVO vo = new TenantUserRelationVO();
        BeanUtils.copyProperties(dto, vo);

        // 设置角色描述
        if (dto.getUserRole() != null) {
            String roleDesc = getRoleDescription(dto.getUserRole());
            vo.setUserRoleDesc(roleDesc);
        }

        return vo;
    }

    /**
     * 获取角色描述
     */
    private String getRoleDescription(String userRole) {
        if ("TENANT_ADMIN".equals(userRole)) {
            return "管理员";
        } else if ("TENANT_USER".equals(userRole)) {
            return "普通用户";
        }
        return userRole;
    }
}
