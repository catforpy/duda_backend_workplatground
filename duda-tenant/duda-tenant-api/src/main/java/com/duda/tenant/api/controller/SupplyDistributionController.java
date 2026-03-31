package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.SupplyDistributionDTO;
import com.duda.tenant.api.service.SupplyDistributionService;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应链分销Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/supply/distribution")
public class SupplyDistributionController {

    @Autowired
    private SupplyDistributionService supplyDistributionService;

    /**
     * 根据ID查询分销记录
     *
     * @param id 分销记录ID
     * @return 分销记录DTO
     */
    @GetMapping("/{id}")
    public ResultVO<SupplyDistributionDTO> getById(@PathVariable Long id) {
        log.info("查询分销记录: id={}", id);
        SupplyDistributionDTO distribution = supplyDistributionService.getById(id);
        if (distribution == null) {
            return ResultVO.error("分销记录不存在");
        }
        return ResultVO.success(distribution);
    }

    /**
     * 创建分销记录（小程序A上架商品）
     *
     * @param dto 分销记录DTO
     * @return 创建的分销记录
     */
    @PostMapping("/create")
    public ResultVO<SupplyDistributionDTO> create(@RequestBody SupplyDistributionDTO dto) {
        log.info("创建分销记录: supplyProductId={}, distributorTenantId={}, stockMode={}, salePrice={}",
                dto.getSupplyProductId(), dto.getDistributorTenantId(), dto.getStockMode(), dto.getSalePrice());

        // 参数校验
        if (dto.getSupplyProductId() == null) {
            return ResultVO.error("商品ID不能为空");
        }
        if (dto.getDistributorTenantId() == null) {
            return ResultVO.error("分销商租户ID不能为空");
        }

        SupplyDistributionDTO created = supplyDistributionService.create(dto);
        return ResultVO.success("上架成功", created);
    }

    /**
     * 更新销售价
     *
     * @param id 分销记录ID
     * @param salePrice 新销售价
     * @return 是否成功
     */
    @PostMapping("/updatePrice")
    public ResultVO<Boolean> updateSalePrice(
            @RequestParam Long id,
            @RequestParam BigDecimal salePrice) {
        log.info("更新销售价: id={}, salePrice={}", id, salePrice);

        if (id == null) {
            return ResultVO.error("分销记录ID不能为空");
        }
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return ResultVO.error("销售价必须大于0");
        }

        Boolean result = supplyDistributionService.updateSalePrice(id, salePrice);
        return ResultVO.success("更新销售价成功", result);
    }

    /**
     * 暂停销售
     *
     * @param id 分销记录ID
     * @return 是否成功
     */
    @PostMapping("/pause")
    public ResultVO<Boolean> pause(@RequestParam Long id) {
        log.info("暂停销售: id={}", id);
        Boolean result = supplyDistributionService.pause(id);
        return ResultVO.success("暂停销售成功", result);
    }

    /**
     * 恢复销售
     *
     * @param id 分销记录ID
     * @return 是否成功
     */
    @PostMapping("/activate")
    public ResultVO<Boolean> activate(@RequestParam Long id) {
        log.info("恢复销售: id={}", id);
        Boolean result = supplyDistributionService.activate(id);
        return ResultVO.success("恢复销售成功", result);
    }

    /**
     * 终止分销
     *
     * @param id 分销记录ID
     * @param reason 终止原因
     * @return 是否成功
     */
    @PostMapping("/terminate")
    public ResultVO<Boolean> terminate(
            @RequestParam Long id,
            @RequestParam(required = false) String reason) {
        log.info("终止分销: id={}, reason={}", id, reason);
        Boolean result = supplyDistributionService.terminate(id, reason);
        return ResultVO.success("终止分销成功", result);
    }

    /**
     * 查询分销商的分销记录列表（小程序A视角）
     *
     * @param distributorTenantId 分销商租户ID
     * @return 分销记录列表
     */
    @GetMapping("/list/distributor/{distributorTenantId}")
    public ResultVO<List<SupplyDistributionDTO>> listByDistributor(@PathVariable Long distributorTenantId) {
        log.info("查询分销商的分销记录: distributorTenantId={}", distributorTenantId);
        List<SupplyDistributionDTO> distributions = supplyDistributionService.listByDistributor(distributorTenantId);
        return ResultVO.success(distributions);
    }

    /**
     * 查询商品的分销记录列表（用户A视角）
     *
     * @param supplyProductId 供应链商品ID
     * @return 分销记录列表
     */
    @GetMapping("/list/product/{supplyProductId}")
    public ResultVO<List<SupplyDistributionDTO>> listByProduct(@PathVariable Long supplyProductId) {
        log.info("查询商品的分销记录: supplyProductId={}", supplyProductId);
        List<SupplyDistributionDTO> distributions = supplyDistributionService.listByProduct(supplyProductId);
        return ResultVO.success(distributions);
    }
}
