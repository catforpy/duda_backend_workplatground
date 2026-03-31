package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.SupplyProductDTO;
import com.duda.tenant.api.service.SupplyProductService;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应链商品Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/supply/product")
public class SupplyProductController {

    @Autowired
    private SupplyProductService supplyProductService;

    /**
     * 根据ID查询商品
     *
     * @param id 商品ID
     * @return 商品DTO
     */
    @GetMapping("/{id}")
    public ResultVO<SupplyProductDTO> getById(@PathVariable Long id) {
        log.info("查询商品: id={}", id);
        SupplyProductDTO product = supplyProductService.getById(id);
        if (product == null) {
            return ResultVO.error("商品不存在");
        }
        return ResultVO.success(product);
    }

    /**
     * 根据商品编码查询
     *
     * @param productCode 商品编码
     * @return 商品DTO
     */
    @GetMapping("/code/{productCode}")
    public ResultVO<SupplyProductDTO> getByProductCode(@PathVariable String productCode) {
        log.info("根据商品编码查询: productCode={}", productCode);
        SupplyProductDTO product = supplyProductService.getByProductCode(productCode);
        if (product == null) {
            return ResultVO.error("商品不存在");
        }
        return ResultVO.success(product);
    }

    /**
     * 创建商品（用户A创建商品）
     *
     * @param dto 商品DTO
     * @return 创建的商品
     */
    @PostMapping("/create")
    public ResultVO<SupplyProductDTO> create(@RequestBody SupplyProductDTO dto) {
        log.info("创建商品: productCode={}, productName={}, supplierTenantId={}",
                dto.getProductCode(), dto.getProductName(), dto.getSupplierTenantId());

        // 参数校验
        if (dto.getProductCode() == null || dto.getProductCode().isEmpty()) {
            return ResultVO.error("商品编码不能为空");
        }
        if (dto.getProductName() == null || dto.getProductName().isEmpty()) {
            return ResultVO.error("商品名称不能为空");
        }
        if (dto.getSupplierTenantId() == null) {
            return ResultVO.error("供应商租户ID不能为空");
        }
        if (dto.getSupplyPrice() == null) {
            return ResultVO.error("供应价不能为空");
        }

        SupplyProductDTO created = supplyProductService.create(dto);
        return ResultVO.success("创建商品成功", created);
    }

    /**
     * 更新商品
     *
     * @param dto 商品DTO
     * @return 更新后的商品
     */
    @PostMapping("/update")
    public ResultVO<SupplyProductDTO> update(@RequestBody SupplyProductDTO dto) {
        log.info("更新商品: id={}, productCode={}", dto.getId(), dto.getProductCode());

        if (dto.getId() == null) {
            return ResultVO.error("商品ID不能为空");
        }

        SupplyProductDTO updated = supplyProductService.update(dto);
        return ResultVO.success("更新商品成功", updated);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 是否成功
     */
    @PostMapping("/delete")
    public ResultVO<Boolean> delete(@RequestParam Long id) {
        log.info("删除商品: id={}", id);

        if (id == null) {
            return ResultVO.error("商品ID不能为空");
        }

        Boolean result = supplyProductService.delete(id);
        return ResultVO.success("删除商品成功", result);
    }

    /**
     * 查询供应商的商品列表（用户A视角）
     *
     * @param supplierTenantId 供应商租户ID
     * @return 商品列表
     */
    @GetMapping("/list/supplier/{supplierTenantId}")
    public ResultVO<List<SupplyProductDTO>> listBySupplier(@PathVariable Long supplierTenantId) {
        log.info("查询供应商商品列表: supplierTenantId={}", supplierTenantId);
        List<SupplyProductDTO> products = supplyProductService.listBySupplier(supplierTenantId);
        return ResultVO.success(products);
    }

    /**
     * 更新库存数量
     *
     * @param id 商品ID
     * @param quantity 库存变化量（可为负数）
     * @return 是否成功
     */
    @PostMapping("/updateStock")
    public ResultVO<Boolean> updateStock(
            @RequestParam Long id,
            @RequestParam Integer quantity) {
        log.info("更新库存: id={}, quantity={}", id, quantity);

        if (id == null) {
            return ResultVO.error("商品ID不能为空");
        }
        if (quantity == null) {
            return ResultVO.error("库存变化量不能为空");
        }

        Boolean result = supplyProductService.updateStock(id, quantity);
        return ResultVO.success("更新库存成功", result);
    }

    /**
     * 查询商品市场（所有审核通过的商品）
     * 小程序A可以从中选择商品进行分销
     *
     * @return 商品列表
     */
    @GetMapping("/market")
    public ResultVO<List<SupplyProductDTO>> listMarketProducts() {
        log.info("查询商品市场");
        List<SupplyProductDTO> products = supplyProductService.listMarketProducts();
        return ResultVO.success(products);
    }
}
