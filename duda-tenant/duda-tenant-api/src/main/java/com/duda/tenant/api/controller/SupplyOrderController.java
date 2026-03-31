package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.SupplyOrderDTO;
import com.duda.tenant.api.service.SupplyOrderService;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应链订单Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/supply/order")
public class SupplyOrderController {

    @Autowired
    private SupplyOrderService supplyOrderService;

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单DTO
     */
    @GetMapping("/{id}")
    public ResultVO<SupplyOrderDTO> getById(@PathVariable Long id) {
        log.info("查询订单: id={}", id);
        SupplyOrderDTO order = supplyOrderService.getById(id);
        if (order == null) {
            return ResultVO.error("订单不存在");
        }
        return ResultVO.success(order);
    }

    /**
     * 根据订单号查询
     *
     * @param distributorOrderNo 分销商订单号
     * @return 订单DTO
     */
    @GetMapping("/order/{distributorOrderNo}")
    public ResultVO<SupplyOrderDTO> getByOrderNo(@PathVariable String distributorOrderNo) {
        log.info("根据订单号查询: orderNo={}", distributorOrderNo);
        SupplyOrderDTO order = supplyOrderService.getByOrderNo(distributorOrderNo);
        if (order == null) {
            return ResultVO.error("订单不存在");
        }
        return ResultVO.success(order);
    }

    /**
     * 创建订单（用户B下单）
     *
     * @param dto 订单DTO
     * @return 创建的订单
     */
    @PostMapping("/create")
    public ResultVO<SupplyOrderDTO> create(@RequestBody SupplyOrderDTO dto) {
        log.info("创建订单: distributorOrderNo={}, supplierTenantId={}, distributorTenantId={}",
                dto.getDistributorOrderNo(), dto.getSupplierTenantId(), dto.getDistributorTenantId());

        // 参数校验
        if (dto.getDistributorOrderNo() == null || dto.getDistributorOrderNo().isEmpty()) {
            return ResultVO.error("订单号不能为空");
        }
        if (dto.getDistributorTenantId() == null) {
            return ResultVO.error("分销商租户ID不能为空");
        }
        if (dto.getSupplyProductId() == null) {
            return ResultVO.error("商品ID不能为空");
        }
        if (dto.getProductPrice() == null) {
            return ResultVO.error("商品单价不能为空");
        }
        if (dto.getProductQuantity() == null || dto.getProductQuantity() <= 0) {
            return ResultVO.error("商品数量必须大于0");
        }

        SupplyOrderDTO created = supplyOrderService.create(dto);
        return ResultVO.success("创建订单成功", created);
    }

    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param orderStatus 新订单状态
     * @return 是否成功
     */
    @PostMapping("/updateStatus")
    public ResultVO<Boolean> updateStatus(
            @RequestParam Long id,
            @RequestParam String orderStatus) {
        log.info("更新订单状态: id={}, orderStatus={}", id, orderStatus);

        if (id == null) {
            return ResultVO.error("订单ID不能为空");
        }
        if (orderStatus == null || orderStatus.isEmpty()) {
            return ResultVO.error("订单状态不能为空");
        }

        Boolean result = supplyOrderService.updateStatus(id, orderStatus);
        return ResultVO.success("更新订单状态成功", result);
    }

    /**
     * 支付订单
     *
     * @param id 订单ID
     * @param paymentMethod 支付方式
     * @param paymentNo 支付单号
     * @return 是否成功
     */
    @PostMapping("/pay")
    public ResultVO<Boolean> pay(
            @RequestParam Long id,
            @RequestParam String paymentMethod,
            @RequestParam String paymentNo) {
        log.info("支付订单: id={}, paymentMethod={}, paymentNo={}", id, paymentMethod, paymentNo);

        if (id == null) {
            return ResultVO.error("订单ID不能为空");
        }
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return ResultVO.error("支付方式不能为空");
        }

        Boolean result = supplyOrderService.pay(id, paymentMethod, paymentNo);
        return ResultVO.success("支付订单成功", result);
    }

    /**
     * 供应商发货
     *
     * @param id 订单ID
     * @param logisticsCompany 物流公司
     * @param logisticsNo 物流单号
     * @return 是否成功
     */
    @PostMapping("/ship")
    public ResultVO<Boolean> ship(
            @RequestParam Long id,
            @RequestParam String logisticsCompany,
            @RequestParam String logisticsNo) {
        log.info("供应商发货: id={}, logisticsCompany={}, logisticsNo={}",
                id, logisticsCompany, logisticsNo);

        if (id == null) {
            return ResultVO.error("订单ID不能为空");
        }
        if (logisticsCompany == null || logisticsCompany.isEmpty()) {
            return ResultVO.error("物流公司不能为空");
        }
        if (logisticsNo == null || logisticsNo.isEmpty()) {
            return ResultVO.error("物流单号不能为空");
        }

        Boolean result = supplyOrderService.ship(id, logisticsCompany, logisticsNo);
        return ResultVO.success("发货成功", result);
    }

    /**
     * 查询分销商的订单列表（小程序A视角）
     *
     * @param distributorTenantId 分销商租户ID
     * @return 订单列表
     */
    @GetMapping("/list/distributor/{distributorTenantId}")
    public ResultVO<List<SupplyOrderDTO>> listByDistributor(@PathVariable Long distributorTenantId) {
        log.info("查询分销商订单: distributorTenantId={}", distributorTenantId);
        List<SupplyOrderDTO> orders = supplyOrderService.listByDistributor(distributorTenantId);
        return ResultVO.success(orders);
    }

    /**
     * 查询供应商的订单列表（用户A视角）
     *
     * @param supplierTenantId 供应商租户ID
     * @return 订单列表
     */
    @GetMapping("/list/supplier/{supplierTenantId}")
    public ResultVO<List<SupplyOrderDTO>> listBySupplier(@PathVariable Long supplierTenantId) {
        log.info("查询供应商订单: supplierTenantId={}", supplierTenantId);
        List<SupplyOrderDTO> orders = supplyOrderService.listBySupplier(supplierTenantId);
        return ResultVO.success(orders);
    }

    /**
     * 查询待结算订单
     *
     * @param distributorTenantId 分销商租户ID
     * @param supplierTenantId 供应商租户ID
     * @return 待结算订单列表
     */
    @GetMapping("/list/pendingSettlement")
    public ResultVO<List<SupplyOrderDTO>> listPendingSettlement(
            @RequestParam Long distributorTenantId,
            @RequestParam(required = false) Long supplierTenantId) {
        log.info("查询待结算订单: distributorTenantId={}, supplierTenantId={}",
                distributorTenantId, supplierTenantId);
        List<SupplyOrderDTO> orders = supplyOrderService.listPendingSettlement(
                distributorTenantId, supplierTenantId);
        return ResultVO.success(orders);
    }

    /**
     * 查询订单统计信息
     *
     * @param distributorTenantId 分销商租户ID
     * @return 统计信息
     */
    @GetMapping("/statistics/{distributorTenantId}")
    public ResultVO<String> getOrderStatistics(@PathVariable Long distributorTenantId) {
        log.info("查询订单统计: distributorTenantId={}", distributorTenantId);
        String statistics = supplyOrderService.getOrderStatistics(distributorTenantId);
        return ResultVO.success(statistics);
    }
}
