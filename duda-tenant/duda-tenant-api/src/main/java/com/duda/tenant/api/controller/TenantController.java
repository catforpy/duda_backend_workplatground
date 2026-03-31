package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.TenantDTO;
import com.duda.tenant.api.service.TenantService;
import com.duda.tenant.api.vo.ResultVO;
import com.duda.tenant.api.vo.TenantCreateVO;
import com.duda.tenant.api.vo.TenantUpdateVO;
import com.duda.tenant.api.vo.TenantVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户Controller
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    /**
     * 根据ID查询租户
     *
     * @param id 租户ID
     * @return 租户VO
     */
    @GetMapping("/{id}")
    public ResultVO<TenantVO> getTenantById(@PathVariable("id") Long id) {
        log.info("查询租户: id={}", id);
        TenantDTO tenantDTO = tenantService.getTenantById(id);
        return ResultVO.success(dtoToVo(tenantDTO));
    }

    /**
     * 根据租户编码查询租户
     *
     * @param tenantCode 租户编码
     * @return 租户VO
     */
    @GetMapping("/code/{tenantCode}")
    public ResultVO<TenantVO> getTenantByCode(@PathVariable("tenantCode") String tenantCode) {
        log.info("查询租户: tenantCode={}", tenantCode);
        TenantDTO tenantDTO = tenantService.getTenantByCode(tenantCode);
        return ResultVO.success(dtoToVo(tenantDTO));
    }

    /**
     * 创建租户
     *
     * @param createVO 创建租户VO
     * @return 租户VO
     */
    @PostMapping
    public ResultVO<TenantVO> createTenant(@RequestBody TenantCreateVO createVO) {
        log.info("创建租户: tenantCode={}, tenantName={}",
                createVO.getTenantCode(), createVO.getTenantName());

        // VO转DTO
        TenantDTO tenantDTO = new TenantDTO();
        BeanUtils.copyProperties(createVO, tenantDTO);

        // 调用Service创建租户
        TenantDTO createdDTO = tenantService.createTenant(tenantDTO);

        return ResultVO.success("创建成功", dtoToVo(createdDTO));
    }

    /**
     * 更新租户
     *
     * @param updateVO 更新租户VO
     * @return 租户VO
     */
    @PutMapping
    public ResultVO<TenantVO> updateTenant(@RequestBody TenantUpdateVO updateVO) {
        log.info("更新租户: id={}", updateVO.getId());

        // VO转DTO
        TenantDTO tenantDTO = new TenantDTO();
        BeanUtils.copyProperties(updateVO, tenantDTO);

        // 调用Service更新租户
        TenantDTO updatedDTO = tenantService.updateTenant(tenantDTO);

        return ResultVO.success("更新成功", dtoToVo(updatedDTO));
    }

    /**
     * 暂停租户
     *
     * @param id 租户ID
     * @return 是否成功
     */
    @PutMapping("/{id}/suspend")
    public ResultVO<Boolean> suspendTenant(@PathVariable("id") Long id) {
        log.info("暂停租户: id={}", id);
        Boolean result = tenantService.suspendTenant(id);
        return ResultVO.success("暂停成功", result);
    }

    /**
     * 激活租户
     *
     * @param id 租户ID
     * @return 是否成功
     */
    @PutMapping("/{id}/activate")
    public ResultVO<Boolean> activateTenant(@PathVariable("id") Long id) {
        log.info("激活租户: id={}", id);
        Boolean result = tenantService.activateTenant(id);
        return ResultVO.success("激活成功", result);
    }

    /**
     * 更新租户套餐
     *
     * @param id 租户ID
     * @param packageId 套餐ID
     * @return 是否成功
     */
    @PutMapping("/{id}/package")
    public ResultVO<Boolean> updatePackage(@PathVariable("id") Long id, @RequestParam Long packageId) {
        log.info("更新租户套餐: id={}, packageId={}", id, packageId);
        Boolean result = tenantService.updatePackage(id, packageId);
        return ResultVO.success("套餐更新成功", result);
    }

    /**
     * 检查租户是否有效
     *
     * @param id 租户ID
     * @return 检查结果
     */
    @GetMapping("/{id}/check")
    public ResultVO<com.duda.tenant.api.dto.TenantCheckDTO> checkTenantValid(@PathVariable("id") Long id) {
        log.info("检查租户有效性: id={}", id);
        com.duda.tenant.api.dto.TenantCheckDTO checkResult = tenantService.checkTenantValid(id);
        return ResultVO.success(checkResult);
    }

    /**
     * 查询租户列表
     *
     * @param tenantType 租户类型(可选)
     * @param tenantStatus 租户状态(可选)
     * @return 租户列表
     */
    @GetMapping("/list")
    public ResultVO<List<TenantVO>> listTenants(
            @RequestParam(required = false) String tenantType,
            @RequestParam(required = false) String tenantStatus) {
        log.info("查询租户列表: tenantType={}, tenantStatus={}", tenantType, tenantStatus);
        // TODO: 实现查询列表逻辑
        return ResultVO.success(List.of());
    }

    /**
     * DTO转VO
     */
    private TenantVO dtoToVo(TenantDTO dto) {
        if (dto == null) {
            return null;
        }
        TenantVO vo = new TenantVO();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }
}
