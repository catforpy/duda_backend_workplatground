package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.TenantConfigDTO;
import com.duda.tenant.api.service.TenantConfigService;
import com.duda.tenant.api.vo.ResultVO;
import com.duda.tenant.api.vo.TenantConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户配置Controller
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class TenantConfigController {

    @Autowired
    private TenantConfigService tenantConfigService;

    /**
     * 根据租户ID和配置键查询配置
     *
     * @param tenantId 租户ID
     * @param configKey 配置键
     * @return 配置VO
     */
    @GetMapping("/{tenantId}/{configKey}")
    public ResultVO<TenantConfigVO> getConfig(@PathVariable Long tenantId, @PathVariable String configKey) {
        log.info("查询租户配置: tenantId={}, configKey={}", tenantId, configKey);
        TenantConfigDTO configDTO = tenantConfigService.getConfig(tenantId, configKey);
        return ResultVO.success(dtoToVo(configDTO));
    }

    /**
     * 根据租户ID查询所有配置
     *
     * @param tenantId 租户ID
     * @return 配置列表
     */
    @GetMapping("/list/{tenantId}")
    public ResultVO<List<TenantConfigVO>> listConfigs(@PathVariable Long tenantId) {
        log.info("查询租户配置列表: tenantId={}", tenantId);
        List<TenantConfigDTO> configs = tenantConfigService.listConfigs(tenantId);
        List<TenantConfigVO> configVOs = configs.stream()
                .map(this::dtoToVo)
                .collect(Collectors.toList());
        return ResultVO.success(configVOs);
    }

    /**
     * 根据租户ID查询配置Map
     *
     * @param tenantId 租户ID
     * @return 配置Map
     */
    @GetMapping("/map/{tenantId}")
    public ResultVO<Map<String, String>> getConfigMap(@PathVariable Long tenantId) {
        log.info("查询租户配置Map: tenantId={}", tenantId);
        Map<String, String> configMap = tenantConfigService.getConfigMap(tenantId);
        return ResultVO.success(configMap);
    }

    /**
     * 创建配置
     *
     * @param configDTO 配置DTO
     * @return 配置VO
     */
    @PostMapping
    public ResultVO<TenantConfigVO> createConfig(@RequestBody TenantConfigDTO configDTO) {
        log.info("创建租户配置: tenantId={}, configKey={}",
                configDTO.getTenantId(), configDTO.getConfigKey());
        TenantConfigDTO createdDTO = tenantConfigService.createConfig(configDTO);
        return ResultVO.success("创建成功", dtoToVo(createdDTO));
    }

    /**
     * 更新配置
     *
     * @param configDTO 配置DTO
     * @return 配置VO
     */
    @PutMapping
    public ResultVO<TenantConfigVO> updateConfig(@RequestBody TenantConfigDTO configDTO) {
        log.info("更新租户配置: configId={}", configDTO.getId());
        TenantConfigDTO updatedDTO = tenantConfigService.updateConfig(configDTO);
        return ResultVO.success("更新成功", dtoToVo(updatedDTO));
    }

    /**
     * 删除配置
     *
     * @param configId 配置ID
     * @return 是否成功
     */
    @DeleteMapping("/{configId}")
    public ResultVO<Boolean> deleteConfig(@PathVariable Long configId) {
        log.info("删除租户配置: configId={}", configId);
        Boolean result = tenantConfigService.deleteConfig(configId);
        return ResultVO.success("删除成功", result);
    }

    /**
     * DTO转VO
     */
    private TenantConfigVO dtoToVo(TenantConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        TenantConfigVO vo = new TenantConfigVO();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }
}
