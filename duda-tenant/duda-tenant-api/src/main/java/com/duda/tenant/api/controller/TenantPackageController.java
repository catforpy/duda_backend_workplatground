package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.TenantPackageDTO;
import com.duda.tenant.api.service.TenantPackageService;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户套餐Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/package")
public class TenantPackageController {

    @Autowired
    private TenantPackageService tenantPackageService;

    /**
     * 查询所有套餐
     *
     * @return 套餐列表
     */
    @GetMapping("/list")
    public ResultVO<List<TenantPackageDTO>> listAll() {
        log.info("查询所有套餐");
        List<TenantPackageDTO> packages = tenantPackageService.listAll();
        return ResultVO.success(packages);
    }

    /**
     * 根据ID查询套餐
     *
     * @param id 套餐ID
     * @return 套餐DTO
     */
    @GetMapping("/{id}")
    public ResultVO<TenantPackageDTO> getById(@PathVariable Long id) {
        log.info("查询套餐: id={}", id);
        TenantPackageDTO pkg = tenantPackageService.getById(id);
        if (pkg == null) {
            return ResultVO.error("套餐不存在");
        }
        return ResultVO.success(pkg);
    }

    /**
     * 创建套餐
     *
     * @param packageDTO 套餐DTO
     * @return 创建的套餐
     */
    @PostMapping("/create")
    public ResultVO<TenantPackageDTO> create(@RequestBody TenantPackageDTO packageDTO) {
        log.info("创建套餐: packageCode={}, packageName={}, packageType={}",
                packageDTO.getPackageCode(), packageDTO.getPackageName(), packageDTO.getPackageType());

        // 参数校验
        if (packageDTO.getPackageCode() == null || packageDTO.getPackageCode().isEmpty()) {
            return ResultVO.error("套餐编码不能为空");
        }
        if (packageDTO.getPackageName() == null || packageDTO.getPackageName().isEmpty()) {
            return ResultVO.error("套餐名称不能为空");
        }
        if (packageDTO.getPackageType() == null || packageDTO.getPackageType().isEmpty()) {
            return ResultVO.error("套餐类型不能为空");
        }

        // 校验套餐类型
        if (!"PLATFORM".equals(packageDTO.getPackageType()) && !"TENANT".equals(packageDTO.getPackageType())) {
            return ResultVO.error("套餐类型必须是PLATFORM或TENANT");
        }

        // 校验目标用户类型
        if (packageDTO.getTargetUserType() != null) {
            if (!"ALL".equals(packageDTO.getTargetUserType())
                    && !"RENTAL".equals(packageDTO.getTargetUserType())
                    && !"PARTNER".equals(packageDTO.getTargetUserType())) {
                return ResultVO.error("目标用户类型必须是ALL、RENTAL或PARTNER");
            }
        }

        try {
            TenantPackageDTO created = tenantPackageService.create(packageDTO);
            return ResultVO.success(created);
        } catch (Exception e) {
            log.error("创建套餐失败", e);
            return ResultVO.error("创建套餐失败: " + e.getMessage());
        }
    }

    /**
     * 更新套餐
     *
     * @param id 套餐ID
     * @param packageDTO 套餐DTO
     * @return 更新后的套餐
     */
    @PutMapping("/{id}")
    public ResultVO<TenantPackageDTO> update(@PathVariable Long id, @RequestBody TenantPackageDTO packageDTO) {
        log.info("更新套餐: id={}, packageCode={}", id, packageDTO.getPackageCode());

        // 参数校验
        if (packageDTO.getId() == null) {
            packageDTO.setId(id);
        }

        try {
            TenantPackageDTO updated = tenantPackageService.update(packageDTO);
            if (updated == null) {
                return ResultVO.error("套餐不存在");
            }
            return ResultVO.success(updated);
        } catch (Exception e) {
            log.error("更新套餐失败", e);
            return ResultVO.error("更新套餐失败: " + e.getMessage());
        }
    }

    /**
     * 删除套餐
     *
     * @param id 套餐ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public ResultVO<Boolean> delete(@PathVariable Long id) {
        log.info("删除套餐: id={}", id);

        try {
            Boolean success = tenantPackageService.delete(id);
            if (success) {
                return ResultVO.success(true);
            } else {
                return ResultVO.error("删除套餐失败");
            }
        } catch (Exception e) {
            log.error("删除套餐失败", e);
            return ResultVO.error("删除套餐失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用套餐
     *
     * @param id 套餐ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    @PutMapping("/{id}/toggle")
    public ResultVO<Boolean> toggleActive(@PathVariable Long id, @RequestParam Boolean enabled) {
        log.info("{}套餐: id={}", enabled ? "启用" : "禁用", id);

        try {
            Boolean success = tenantPackageService.toggleActive(id, enabled);
            if (success) {
                return ResultVO.success(true);
            } else {
                return ResultVO.error("操作失败");
            }
        } catch (Exception e) {
            log.error("操作失败", e);
            return ResultVO.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 根据套餐类型查询套餐
     *
     * @param packageType 套餐类型（PLATFORM/TENANT）
     * @return 套餐列表
     */
    @GetMapping("/type/{packageType}")
    public ResultVO<List<TenantPackageDTO>> listByPackageType(@PathVariable String packageType) {
        log.info("根据套餐类型查询: packageType={}", packageType);

        // 校验套餐类型
        if (!"PLATFORM".equals(packageType) && !"TENANT".equals(packageType)) {
            return ResultVO.error("套餐类型必须是PLATFORM或TENANT");
        }

        List<TenantPackageDTO> packages = tenantPackageService.listByPackageType(packageType);
        return ResultVO.success(packages);
    }

    /**
     * 根据租户ID查询套餐
     *
     * @param tenantId 租户ID
     * @return 套餐列表
     */
    @GetMapping("/tenant/{tenantId}")
    public ResultVO<List<TenantPackageDTO>> listByTenantId(@PathVariable Long tenantId) {
        log.info("根据租户ID查询套餐: tenantId={}", tenantId);

        List<TenantPackageDTO> packages = tenantPackageService.listByTenantId(tenantId);
        return ResultVO.success(packages);
    }

    /**
     * 根据目标用户类型查询套餐
     *
     * @param targetUserType 目标用户类型（ALL/RENTAL/PARTNER）
     * @return 套餐列表
     */
    @GetMapping("/target-user/{targetUserType}")
    public ResultVO<List<TenantPackageDTO>> listByTargetUserType(@PathVariable String targetUserType) {
        log.info("根据目标用户类型查询: targetUserType={}", targetUserType);

        // 校验目标用户类型
        if (!"ALL".equals(targetUserType) && !"RENTAL".equals(targetUserType) && !"PARTNER".equals(targetUserType)) {
            return ResultVO.error("目标用户类型必须是ALL、RENTAL或PARTNER");
        }

        List<TenantPackageDTO> packages = tenantPackageService.listByTargetUserType(targetUserType);
        return ResultVO.success(packages);
    }

    /**
     * 复制套餐（用于快速创建新套餐）
     *
     * @param id 原套餐ID
     * @param newPackageCode 新套餐编码
     * @param newPackageName 新套餐名称
     * @return 新套餐
     */
    @PostMapping("/{id}/copy")
    public ResultVO<TenantPackageDTO> copy(
            @PathVariable Long id,
            @RequestParam String newPackageCode,
            @RequestParam String newPackageName) {

        log.info("复制套餐: id={}, newPackageCode={}, newPackageName={}", id, newPackageCode, newPackageName);

        // 参数校验
        if (newPackageCode == null || newPackageCode.isEmpty()) {
            return ResultVO.error("新套餐编码不能为空");
        }
        if (newPackageName == null || newPackageName.isEmpty()) {
            return ResultVO.error("新套餐名称不能为空");
        }

        try {
            TenantPackageDTO copied = tenantPackageService.copy(id, newPackageCode, newPackageName);
            if (copied == null) {
                return ResultVO.error("原套餐不存在");
            }
            return ResultVO.success(copied);
        } catch (Exception e) {
            log.error("复制套餐失败", e);
            return ResultVO.error("复制套餐失败: " + e.getMessage());
        }
    }
}
