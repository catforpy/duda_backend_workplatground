package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.SupplyDistributionDTO;
import com.duda.tenant.api.dto.SupplyOrderDTO;
import com.duda.tenant.api.dto.SupplyProductDTO;
import com.duda.tenant.entity.SupplyDistribution;
import com.duda.tenant.entity.SupplyOrder;
import com.duda.tenant.entity.SupplyProduct;
import com.duda.tenant.mapper.SupplyOrderMapper;
import com.duda.tenant.service.SupplyDistributionService;
import com.duda.tenant.service.SupplyOrderService;
import com.duda.tenant.service.SupplyProductService;
import com.duda.tenant.service.TenantUserRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应链订单服务实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@Service
public class SupplyOrderServiceImpl
    extends ServiceImpl<SupplyOrderMapper, SupplyOrder>
    implements SupplyOrderService {

    @Autowired
    private SupplyOrderMapper orderMapper;

    @Autowired
    private SupplyProductService supplyProductService;

    @Autowired
    private SupplyDistributionService supplyDistributionService;

    @Autowired
    private TenantUserRelationService tenantUserRelationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyOrder createOrder(SupplyOrderDTO dto) {
        log.info("开始创建供应链订单: supplyProductId={}, distributorTenantId={}",
            dto.getSupplyProductId(), dto.getDistributorTenantId());

        // 1. 参数校验
        validateCreateParams(dto);

        // 2. 自动关联用户（✨新增）
        // 2.1 自动关联购买用户（用户B）到分销商小程序
        if (dto.getCustomerUserId() != null && dto.getDistributorTenantId() != null) {
            autoJoinTenant(dto.getCustomerUserId(), dto.getDistributorTenantId(), "TENANT_USER");
        }

        // 2.2 自动关联供应商用户（用户A）到供应商小程序
        if (dto.getSupplierUserId() != null && dto.getSupplierTenantId() != null) {
            autoJoinTenant(dto.getSupplierUserId(), dto.getSupplierTenantId(), "TENANT_ADMIN");
        }

        // 3. 检查商品是否存在
        SupplyProduct product = supplyProductService.getById(dto.getSupplyProductId());
        if (product == null) {
            log.warn("商品不存在: supplyProductId={}", dto.getSupplyProductId());
            throw new BizException(10001, "商品不存在");
        }

        // 3. 检查商品状态
        if (!"on_sale".equals(product.getStatus())) {
            log.warn("商品未上架，无法下单: supplyProductId={}, status={}",
                dto.getSupplyProductId(), product.getStatus());
            throw new BizException(10003, "商品未上架，无法下单");
        }

        // 4. 检查分销关系是否存在
        LambdaQueryWrapper<SupplyDistribution> distWrapper = new LambdaQueryWrapper<>();
        distWrapper.eq(SupplyDistribution::getSupplyProductId, dto.getSupplyProductId());
        distWrapper.eq(SupplyDistribution::getDistributorTenantId, dto.getDistributorTenantId());
        distWrapper.eq(SupplyDistribution::getStatus, "active");
        SupplyDistribution distribution = supplyDistributionService.getOne(distWrapper);
        if (distribution == null) {
            log.warn("分销关系不存在或已终止: supplyProductId={}, distributorTenantId={}",
                dto.getSupplyProductId(), dto.getDistributorTenantId());
            throw new BizException(20001, "分销关系不存在或已终止");
        }

        // 5. 检查订单号是否重复(幂等性)
        if (StrUtil.isNotBlank(dto.getDistributorOrderNo())) {
            LambdaQueryWrapper<SupplyOrder> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(SupplyOrder::getDistributorOrderNo, dto.getDistributorOrderNo());
            if (count(orderWrapper) > 0) {
                log.warn("订单号已存在: distributorOrderNo={}", dto.getDistributorOrderNo());
                throw new BizException(409, "订单号已存在");
            }
        }

        // 6. 业务规则校验
        validateBusinessRules(dto, product, distribution);

        // 7. 创建订单
        SupplyOrder order = new SupplyOrder();
        BeanUtils.copyProperties(dto, order);

        // 从商品快照获取信息
        order.setProductCode(product.getProductCode());
        order.setProductName(product.getProductName());
        order.setSupplierTenantId(product.getSupplierTenantId());
        order.setSupplierTenantName(product.getSupplierTenantName());
        order.setShippingMode(product.getShippingMode());

        // 从分销快照获取价格和佣金信息
        order.setSupplyPrice(distribution.getSupplyPrice());
        // 计算佣金金额
        if (distribution.getCommissionAmount() != null) {
            order.setCommissionAmount(distribution.getCommissionAmount());
        } else {
            // 佣金金额 = (销售价 - 供应价) * 佣金比例
            BigDecimal profit = dto.getProductPrice().subtract(distribution.getSupplyPrice());
            order.setCommissionAmount(
                profit.multiply(distribution.getCommissionRate())
            );
        }

        // 计算订单总金额
        order.setTotalAmount(dto.getProductPrice().multiply(new BigDecimal(dto.getProductQuantity())));

        // 设置初始状态
        order.setOrderStatus("pending");
        order.setShippingStatus("pending");
        order.setSettlementStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 8. 保存订单
        save(order);

        // 9. 扣减库存(如果有库存需要扣减)
        if ("local".equals(distribution.getStockMode())) {
            // 本地库存模式，扣减分销商的本地库存
            Integer newStock = distribution.getLocalStockCount() - dto.getProductQuantity();
            if (newStock < 0) {
                log.warn("本地库存不足: distributionId={}, currentStock={}, demand={}",
                    distribution.getId(), distribution.getLocalStockCount(), dto.getProductQuantity());
                throw new BizException(10002, "库存不足");
            }
            distribution.setLocalStockCount(newStock);
            distribution.setUpdatedAt(LocalDateTime.now());
            supplyDistributionService.updateById(distribution);
            log.info("扣减本地库存成功: distributionId={}, newStock={}",
                distribution.getId(), newStock);
        } else {
            // 同步供应商库存模式，扣减供应商库存
            supplyProductService.updateStock(product.getId(), -dto.getProductQuantity());
        }

        // 10. 更新商品销售数量
        product.setSalesCount(product.getSalesCount() + dto.getProductQuantity());
        supplyProductService.updateById(product);

        // 11. 更新分销记录销售数量和销售总额
        distribution.setSalesCount(distribution.getSalesCount() + dto.getProductQuantity());
        distribution.setTotalSalesAmount(
            distribution.getTotalSalesAmount().add(order.getTotalAmount())
        );
        distribution.setUpdatedAt(LocalDateTime.now());
        supplyDistributionService.updateById(distribution);

        log.info("创建供应链订单成功: orderId={}, orderNo={}, totalAmount={}",
            order.getId(), dto.getDistributorOrderNo(), order.getTotalAmount());

        // TODO: 发送订单创建消息到MQ

        return order;
    }

    @Override
    public SupplyOrderDTO getOrderDTO(Long id) {
        log.debug("查询供应链订单: orderId={}", id);

        if (id == null) {
            throw new BizException(400, "订单ID不能为空");
        }

        SupplyOrder order = getById(id);
        if (order == null) {
            log.warn("订单不存在: orderId={}", id);
            throw new BizException(30001, "订单不存在");
        }

        // 转换为DTO
        SupplyOrderDTO dto = new SupplyOrderDTO();
        BeanUtils.copyProperties(order, dto);

        return dto;
    }

    @Override
    public SupplyOrderDTO getByOrderNo(String distributorOrderNo) {
        log.debug("根据订单号查询: distributorOrderNo={}", distributorOrderNo);

        if (StrUtil.isBlank(distributorOrderNo)) {
            throw new BizException(400, "订单号不能为空");
        }

        LambdaQueryWrapper<SupplyOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyOrder::getDistributorOrderNo, distributorOrderNo);
        SupplyOrder order = getOne(wrapper);

        if (order == null) {
            log.warn("订单不存在: distributorOrderNo={}", distributorOrderNo);
            throw new BizException(30001, "订单不存在");
        }

        SupplyOrderDTO dto = new SupplyOrderDTO();
        BeanUtils.copyProperties(order, dto);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, String orderStatus) {
        log.info("开始更新订单状态: orderId={}, newStatus={}", id, orderStatus);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "订单ID不能为空");
        }
        if (StrUtil.isBlank(orderStatus)) {
            throw new BizException(400, "订单状态不能为空");
        }

        // 查询订单
        SupplyOrder order = getById(id);
        if (order == null) {
            throw new BizException(30001, "订单不存在");
        }

        // 业务规则校验：状态流转合法性
        if (!isValidStatusTransition(order.getOrderStatus(), orderStatus)) {
            log.warn("订单状态流转不合法: currentStatus={}, newStatus={}",
                order.getOrderStatus(), orderStatus);
            throw new BizException(30002, "订单状态不允许此操作");
        }

        // 更新订单状态
        order.setOrderStatus(orderStatus);
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        log.info("更新订单状态成功: orderId={}, newStatus={}", id, orderStatus);

        // TODO: 发送订单状态变更消息

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pay(Long id, String paymentMethod, String paymentNo) {
        log.info("开始支付订单: orderId={}, paymentMethod={}, paymentNo={}",
            id, paymentMethod, paymentNo);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "订单ID不能为空");
        }
        if (StrUtil.isBlank(paymentMethod)) {
            throw new BizException(400, "支付方式不能为空");
        }
        if (StrUtil.isBlank(paymentNo)) {
            throw new BizException(400, "支付单号不能为空");
        }

        // 查询订单
        SupplyOrder order = getById(id);
        if (order == null) {
            log.warn("订单不存在: orderId={}", id);
            throw new BizException(10001, "订单不存在");
        }

        // 检查订单状态
        if (!"pending".equals(order.getOrderStatus())) {
            log.warn("订单状态不允许支付: orderId={}, orderStatus={}", id, order.getOrderStatus());
            throw new BizException(10005, "订单状态不允许支付");
        }

        // TODO: 验证支付金额是否匹配
        // TODO: 调用第三方支付平台验证支付状态

        // 更新订单状态为已支付
        order.setOrderStatus("paid");
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        log.info("订单支付成功: orderId={}, paymentNo={}", id, paymentNo);

        // TODO: 发送支付成功消息
        // TODO: 触发库存扣减
        // TODO: 通知供应商发货

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean ship(Long id, String logisticsCompany, String logisticsNo) {
        log.info("开始订单发货: orderId={}, logisticsCompany={}, logisticsNo={}",
            id, logisticsCompany, logisticsNo);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "订单ID不能为空");
        }
        if (StrUtil.isBlank(logisticsCompany)) {
            throw new BizException(400, "物流公司不能为空");
        }
        if (StrUtil.isBlank(logisticsNo)) {
            throw new BizException(400, "物流单号不能为空");
        }

        // 查询订单
        SupplyOrder order = getById(id);
        if (order == null) {
            throw new BizException(30001, "订单不存在");
        }

        // 业务规则校验：只有已支付订单才能发货
        if (!"paid".equals(order.getOrderStatus()) && !"shipped".equals(order.getOrderStatus())) {
            log.warn("订单状态不允许发货: orderStatus={}", order.getOrderStatus());
            throw new BizException(30002, "订单状态不允许发货");
        }

        // 已发货订单不能重复发货
        if ("shipped".equals(order.getShippingStatus())) {
            log.warn("订单已发货，不能重复发货: orderId={}", id);
            throw new BizException(30002, "订单已发货，不能重复发货");
        }

        // 更新发货信息
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        order.setShippingStatus("shipping");
        order.setShippingTime(LocalDateTime.now());

        // 如果订单状态是已支付，更新为已发货
        if ("paid".equals(order.getOrderStatus())) {
            order.setOrderStatus("shipped");
        }

        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        log.info("订单发货成功: orderId={}, logisticsNo={}", id, logisticsNo);

        // TODO: 发送发货通知消息

        return true;
    }

    @Override
    public List<SupplyOrderDTO> listByDistributor(Long distributorTenantId) {
        log.debug("查询分销商的订单列表: distributorTenantId={}", distributorTenantId);

        if (distributorTenantId == null) {
            throw new BizException(400, "分销商租户ID不能为空");
        }

        LambdaQueryWrapper<SupplyOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyOrder::getDistributorTenantId, distributorTenantId);
        wrapper.orderByDesc(SupplyOrder::getCreatedAt);
        List<SupplyOrder> list = list(wrapper);

        return list.stream()
            .map(order -> {
                SupplyOrderDTO dto = new SupplyOrderDTO();
                BeanUtils.copyProperties(order, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SupplyOrderDTO> listBySupplier(Long supplierTenantId) {
        log.debug("查询供应商的订单列表: supplierTenantId={}", supplierTenantId);

        if (supplierTenantId == null) {
            throw new BizException(400, "供应商租户ID不能为空");
        }

        LambdaQueryWrapper<SupplyOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyOrder::getSupplierTenantId, supplierTenantId);
        wrapper.orderByDesc(SupplyOrder::getCreatedAt);
        List<SupplyOrder> list = list(wrapper);

        return list.stream()
            .map(order -> {
                SupplyOrderDTO dto = new SupplyOrderDTO();
                BeanUtils.copyProperties(order, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SupplyOrderDTO> listPendingSettlement(Long distributorTenantId, Long supplierTenantId) {
        log.debug("查询待结算订单列表: distributorTenantId={}, supplierTenantId={}",
            distributorTenantId, supplierTenantId);

        LambdaQueryWrapper<SupplyOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyOrder::getDistributorTenantId, distributorTenantId);
        if (supplierTenantId != null) {
            wrapper.eq(SupplyOrder::getSupplierTenantId, supplierTenantId);
        }
        wrapper.eq(SupplyOrder::getSettlementStatus, "pending");
        wrapper.eq(SupplyOrder::getOrderStatus, "completed");
        wrapper.orderByDesc(SupplyOrder::getCreatedAt);
        List<SupplyOrder> list = list(wrapper);

        return list.stream()
            .map(order -> {
                SupplyOrderDTO dto = new SupplyOrderDTO();
                BeanUtils.copyProperties(order, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * 校验创建参数
     */
    private void validateCreateParams(SupplyOrderDTO dto) {
        if (dto == null) {
            throw new BizException(400, "订单信息不能为空");
        }
        if (dto.getSupplyProductId() == null) {
            throw new BizException(400, "供应链商品ID不能为空");
        }
        if (dto.getDistributorTenantId() == null) {
            throw new BizException(400, "分销商租户ID不能为空");
        }
        if (StrUtil.isBlank(dto.getDistributorOrderNo())) {
            throw new BizException(400, "分销商订单号不能为空");
        }
        if (dto.getProductQuantity() == null || dto.getProductQuantity() <= 0) {
            throw new BizException(400, "购买数量必须大于0");
        }
        if (dto.getProductPrice() == null || dto.getProductPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "商品单价必须大于0");
        }
        if (StrUtil.isBlank(dto.getCustomerName())) {
            throw new BizException(400, "客户姓名不能为空");
        }
        if (StrUtil.isBlank(dto.getCustomerPhone())) {
            throw new BizException(400, "客户手机号不能为空");
        }
        if (StrUtil.isBlank(dto.getCustomerAddress())) {
            throw new BizException(400, "客户收货地址不能为空");
        }
    }

    /**
     * 校验业务规则
     */
    private void validateBusinessRules(SupplyOrderDTO dto, SupplyProduct product,
                                       SupplyDistribution distribution) {
        // 价格校验：订单单价应该等于分销记录的销售价
        if (dto.getProductPrice().compareTo(distribution.getSalePrice()) != 0) {
            log.warn("订单单价与销售价不一致: productPrice={}, salePrice={}",
                dto.getProductPrice(), distribution.getSalePrice());
            throw new BizException(400, "订单单价与销售价不一致");
        }

        // 库存校验
        if ("local".equals(distribution.getStockMode())) {
            // 本地库存模式
            if (distribution.getLocalStockCount() < dto.getProductQuantity()) {
                log.warn("本地库存不足: localStock={}, demand={}",
                    distribution.getLocalStockCount(), dto.getProductQuantity());
                throw new BizException(10002, "库存不足");
            }
        } else {
            // 同步供应商库存模式
            if (product.getStockCount() < dto.getProductQuantity()) {
                log.warn("供应商库存不足: stock={}, demand={}",
                    product.getStockCount(), dto.getProductQuantity());
                throw new BizException(10002, "库存不足");
            }
        }

        // 手机号格式校验
        if (!dto.getCustomerPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BizException(400, "客户手机号格式不正确");
        }
    }

    /**
     * 校验订单状态流转是否合法
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // 允许的状态流转
        switch (currentStatus) {
            case "pending":
                return "paid".equals(newStatus) || "refunded".equals(newStatus);
            case "paid":
                return "shipped".equals(newStatus) || "refunded".equals(newStatus);
            case "shipped":
                return "completed".equals(newStatus) || "refunded".equals(newStatus);
            case "completed":
                return false; // 已完成订单不能变更状态
            case "refunded":
                return false; // 已退款订单不能变更状态
            default:
                return false;
        }
    }

    /**
     * 自动关联用户到小程序（✨新增）
     * 如果用户还没有关联到该小程序，自动创建关联关系
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @param roleCode 角色编码
     */
    private void autoJoinTenant(Long userId, Long tenantId, String roleCode) {
        try {
            // 检查是否已经关联
            Boolean isRelated = tenantUserRelationService.checkRelation(userId, tenantId);

            if (!isRelated) {
                // 未关联，自动创建关联关系
                log.info("自动关联用户到小程序: userId={}, tenantId={}, roleCode={}",
                        userId, tenantId, roleCode);

                tenantUserRelationService.joinTenant(userId, tenantId, roleCode, 0);

                log.info("自动关联成功: userId={}, tenantId={}", userId, tenantId);
            } else {
                log.debug("用户已关联到小程序: userId={}, tenantId={}", userId, tenantId);
            }
        } catch (Exception e) {
            // 自动关联失败不影响订单创建，只记录日志
            log.error("自动关联用户到小程序失败: userId={}, tenantId={}", userId, tenantId, e);
        }
    }
}
