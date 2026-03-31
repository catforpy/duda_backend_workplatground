package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.SupplyProductDTO;
import com.duda.tenant.entity.SupplyProduct;
import com.duda.tenant.mapper.SupplyProductMapper;
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
 * 供应链商品服务实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@Service
public class SupplyProductServiceImpl
    extends ServiceImpl<SupplyProductMapper, SupplyProduct>
    implements SupplyProductService {

    @Autowired
    private SupplyProductMapper productMapper;

    @Autowired
    private TenantUserRelationService tenantUserRelationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyProduct createProduct(SupplyProductDTO dto) {
        log.info("开始创建供应链商品: productName={}", dto.getProductName());

        // 1. 参数校验
        validateCreateParams(dto);

        // 2. 自动关联用户（✨新增）
        // 自动关联供应商用户（用户A）到供应商小程序
        if (dto.getSupplierUserId() != null && dto.getSupplierTenantId() != null) {
            autoJoinTenant(dto.getSupplierUserId(), dto.getSupplierTenantId(), "TENANT_ADMIN");
        }

        // 3. 业务规则校验
        validateBusinessRules(dto);

        // 3. 检查商品编码是否已存在(幂等性)
        LambdaQueryWrapper<SupplyProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyProduct::getProductCode, dto.getProductCode());
        if (count(wrapper) > 0) {
            log.warn("商品编码已存在: productCode={}", dto.getProductCode());
            throw new BizException(409, "商品编码已存在");
        }

        // 4. 创建实体
        SupplyProduct product = new SupplyProduct();
        BeanUtils.copyProperties(dto, product);

        // 设置默认值
        product.setCommissionRate(dto.getCommissionRate() != null
            ? dto.getCommissionRate()
            : new BigDecimal("0.1000"));
        product.setStockCount(dto.getStockCount() != null ? dto.getStockCount() : 0);
        product.setStockSyncMode(dto.getStockSyncMode() != null ? dto.getStockSyncMode() : "realtime");
        product.setReviewStatus("pending");
        product.setStatus("draft");
        product.setViewCount(0);
        product.setFavoriteCount(0);
        product.setDistributionCount(0);
        product.setSalesCount(0);
        product.setRating(BigDecimal.ZERO);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // 5. 保存到数据库
        save(product);

        log.info("创建供应链商品成功: productId={}, productCode={}",
            product.getId(), product.getProductCode());

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyProduct updateProduct(SupplyProductDTO dto) {
        log.info("开始更新供应链商品: productId={}", dto.getId());

        // 1. 参数校验
        if (dto.getId() == null) {
            throw new BizException(400, "商品ID不能为空");
        }

        // 2. 查询商品
        SupplyProduct product = getById(dto.getId());
        if (product == null) {
            log.warn("商品不存在: productId={}", dto.getId());
            throw new BizException(10001, "商品不存在");
        }

        // 3. 权限校验(供应商只能操作自己的商品)
        // TODO: 从上下文获取当前租户ID进行权限校验
        // Long currentTenantId = TenantContext.getTenantId();
        // if (!currentTenantId.equals(product.getSupplierTenantId())) {
        //     log.warn("权限不足: currentTenantId={}, supplierTenantId={}",
        //         currentTenantId, product.getSupplierTenantId());
        //     throw new BizException(403, "无权限操作此商品");
        // }

        // 4. 业务规则校验
        if ("terminated".equals(product.getStatus())) {
            throw new BizException(10003, "商品已终止，无法修改");
        }

        // 5. 更新字段
        if (StrUtil.isNotBlank(dto.getProductName())) {
            product.setProductName(dto.getProductName());
        }
        if (StrUtil.isNotBlank(dto.getProductCover())) {
            product.setProductCover(dto.getProductCover());
        }
        if (StrUtil.isNotBlank(dto.getProductImages())) {
            product.setProductImages(dto.getProductImages());
        }
        if (StrUtil.isNotBlank(dto.getProductDesc())) {
            product.setProductDesc(dto.getProductDesc());
        }
        if (StrUtil.isNotBlank(dto.getProductDetail())) {
            product.setProductDetail(dto.getProductDetail());
        }
        if (dto.getOriginalPrice() != null) {
            product.setOriginalPrice(dto.getOriginalPrice());
        }
        if (dto.getSupplyPrice() != null) {
            product.setSupplyPrice(dto.getSupplyPrice());
        }
        if (dto.getSuggestPrice() != null) {
            product.setSuggestPrice(dto.getSuggestPrice());
        }
        if (dto.getCommissionRate() != null) {
            product.setCommissionRate(dto.getCommissionRate());
        }
        if (dto.getCommissionAmount() != null) {
            product.setCommissionAmount(dto.getCommissionAmount());
        }
        if (dto.getStockCount() != null) {
            product.setStockCount(dto.getStockCount());
        }
        if (StrUtil.isNotBlank(dto.getStockSyncMode())) {
            product.setStockSyncMode(dto.getStockSyncMode());
        }
        if (StrUtil.isNotBlank(dto.getShippingMode())) {
            product.setShippingMode(dto.getShippingMode());
        }
        if (StrUtil.isNotBlank(dto.getShippingAddress())) {
            product.setShippingAddress(dto.getShippingAddress());
        }
        if (StrUtil.isNotBlank(dto.getTags())) {
            product.setTags(dto.getTags());
        }
        if (StrUtil.isNotBlank(dto.getBrand())) {
            product.setBrand(dto.getBrand());
        }
        if (StrUtil.isNotBlank(dto.getStatus())) {
            product.setStatus(dto.getStatus());
        }
        if (StrUtil.isNotBlank(dto.getReviewStatus())) {
            product.setReviewStatus(dto.getReviewStatus());
        }

        product.setUpdatedAt(LocalDateTime.now());

        // 6. 保存更新
        updateById(product);

        log.info("更新供应链商品成功: productId={}", product.getId());

        return product;
    }

    @Override
    public SupplyProductDTO getProductDTO(Long id) {
        log.debug("查询供应链商品: productId={}", id);

        if (id == null) {
            throw new BizException(400, "商品ID不能为空");
        }

        SupplyProduct product = getById(id);
        if (product == null) {
            log.warn("商品不存在: productId={}", id);
            throw new BizException(10001, "商品不存在");
        }

        // 转换为DTO
        SupplyProductDTO dto = new SupplyProductDTO();
        BeanUtils.copyProperties(product, dto);

        return dto;
    }

    @Override
    public SupplyProductDTO getByProductCode(String productCode) {
        log.debug("根据商品编码查询: productCode={}", productCode);

        if (StrUtil.isBlank(productCode)) {
            throw new BizException(400, "商品编码不能为空");
        }

        LambdaQueryWrapper<SupplyProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyProduct::getProductCode, productCode);
        SupplyProduct product = getOne(wrapper);

        if (product == null) {
            log.warn("商品不存在: productCode={}", productCode);
            throw new BizException(10001, "商品不存在");
        }

        SupplyProductDTO dto = new SupplyProductDTO();
        BeanUtils.copyProperties(product, dto);

        return dto;
    }

    @Override
    public List<SupplyProductDTO> listBySupplier(Long supplierTenantId) {
        log.debug("查询供应商商品列表: supplierTenantId={}", supplierTenantId);

        if (supplierTenantId == null) {
            throw new BizException(400, "供应商租户ID不能为空");
        }

        LambdaQueryWrapper<SupplyProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyProduct::getSupplierTenantId, supplierTenantId);
        wrapper.orderByDesc(SupplyProduct::getCreatedAt);
        List<SupplyProduct> list = list(wrapper);

        return list.stream()
            .map(product -> {
                SupplyProductDTO dto = new SupplyProductDTO();
                BeanUtils.copyProperties(product, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SupplyProductDTO> listMarketProducts() {
        log.debug("查询商品市场(所有审核通过的商品)");

        LambdaQueryWrapper<SupplyProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyProduct::getReviewStatus, "approved");
        wrapper.eq(SupplyProduct::getStatus, "on_sale");
        wrapper.orderByDesc(SupplyProduct::getSalesCount);
        List<SupplyProduct> list = list(wrapper);

        return list.stream()
            .map(product -> {
                SupplyProductDTO dto = new SupplyProductDTO();
                BeanUtils.copyProperties(product, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStock(Long id, Integer quantity) {
        log.info("开始更新库存: productId={}, quantity={}", id, quantity);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "商品ID不能为空");
        }
        if (quantity == null || quantity == 0) {
            throw new BizException(400, "库存变化量不能为空或0");
        }

        // TODO: 使用分布式锁(需要duda-common-redis)
        // String lockKey = "stock_lock:" + id;
        // RLock lock = redissonClient.getLock(lockKey);
        // try {
        //     boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
        //     if (!locked) {
        //         log.warn("获取库存锁失败: productId={}", id);
        //         throw new BizException("系统繁忙，请稍后重试");
        //     }

        // 查询商品
        SupplyProduct product = getById(id);
        if (product == null) {
            throw new BizException(10001, "商品不存在");
        }

        // 检查库存
        Integer newStock = product.getStockCount() + quantity;
        if (newStock < 0) {
            log.warn("库存不足: productId={}, currentStock={}, demand={}",
                id, product.getStockCount(), Math.abs(quantity));
            throw new BizException(10002, "库存不足");
        }

        // 更新库存
        product.setStockCount(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        updateById(product);

        // 库存预警检查
        if (product.getWarningStock() != null && newStock <= product.getWarningStock()) {
            log.warn("库存预警: productId={}, newStock={}, warningStock={}",
                id, newStock, product.getWarningStock());
            // TODO: 发送库存预警消息
        }

        log.info("库存更新成功: productId={}, oldStock={}, newStock={}",
            id, product.getStockCount(), newStock);

        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        //     throw new BizException("库存更新失败");
        // } finally {
        //     if (lock.isHeldByCurrentThread()) {
        //         lock.unlock();
        //     }
        // }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(Long id, Boolean approved, Long reviewerId, String reason) {
        log.info("开始审核商品: productId={}, approved={}, reviewerId={}",
            id, approved, reviewerId);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "商品ID不能为空");
        }
        if (approved == null) {
            throw new BizException(400, "审核结果不能为空");
        }
        if (reviewerId == null) {
            throw new BizException(400, "审核员ID不能为空");
        }

        // 查询商品
        SupplyProduct product = getById(id);
        if (product == null) {
            throw new BizException(10001, "商品不存在");
        }

        // 业务规则校验
        if (!"pending".equals(product.getReviewStatus())) {
            log.warn("商品已审核，不能重复审核: productId={}, reviewStatus={}",
                id, product.getReviewStatus());
            throw new BizException(409, "商品已审核，不能重复审核");
        }

        // 更新审核状态
        product.setReviewStatus(approved ? "approved" : "rejected");
        product.setReviewerId(reviewerId);
        product.setReviewTime(LocalDateTime.now());
        product.setReviewReason(reason);

        // 如果审核通过，自动上架
        if (approved) {
            product.setStatus("on_sale");
            log.info("审核通过，自动上架商品: productId={}", id);
        }

        product.setUpdatedAt(LocalDateTime.now());
        updateById(product);

        log.info("审核商品成功: productId={}, approved={}", id, approved);

        // TODO: 发送审核结果消息到供应商

        return true;
    }

    /**
     * 校验创建参数
     */
    private void validateCreateParams(SupplyProductDTO dto) {
        if (dto == null) {
            throw new BizException(400, "商品信息不能为空");
        }
        if (StrUtil.isBlank(dto.getProductName())) {
            throw new BizException(400, "商品名称不能为空");
        }
        if (StrUtil.isBlank(dto.getProductCode())) {
            throw new BizException(400, "商品编码不能为空");
        }
        if (dto.getOriginalPrice() == null) {
            throw new BizException(400, "原价不能为空");
        }
        if (dto.getSupplyPrice() == null) {
            throw new BizException(400, "供应价不能为空");
        }
        if (dto.getSupplierTenantId() == null) {
            throw new BizException(400, "供应商租户ID不能为空");
        }
    }

    /**
     * 校验业务规则
     */
    private void validateBusinessRules(SupplyProductDTO dto) {
        // 价格校验：供应价不能高于原价
        if (dto.getOriginalPrice().compareTo(dto.getSupplyPrice()) < 0) {
            throw new BizException(10004, "供应价不能高于原价");
        }

        // 建议零售价校验：如果设置了建议零售价，不能低于供应价
        if (dto.getSuggestPrice() != null) {
            if (dto.getSuggestPrice().compareTo(dto.getSupplyPrice()) < 0) {
                throw new BizException(10005, "建议零售价不能低于供应价");
            }
        }

        // 佣金比例校验：必须在0-50%之间
        if (dto.getCommissionRate() != null) {
            if (dto.getCommissionRate().compareTo(BigDecimal.ZERO) < 0
                || dto.getCommissionRate().compareTo(new BigDecimal("0.5")) > 0) {
                throw new BizException(400, "佣金比例必须在0-50%之间");
            }
        }

        // 固定佣金金额校验：如果设置了固定佣金，不能高于(原价-供应价)
        if (dto.getCommissionAmount() != null) {
            BigDecimal maxCommission = dto.getOriginalPrice().subtract(dto.getSupplyPrice());
            if (dto.getCommissionAmount().compareTo(maxCommission) > 0) {
                throw new BizException(400, "固定佣金金额不能高于(原价-供应价)");
            }
        }

        // 库存数量校验：不能为负数
        if (dto.getStockCount() != null && dto.getStockCount() < 0) {
            throw new BizException(400, "库存数量不能为负数");
        }

        // 商品类型校验
        if (StrUtil.isBlank(dto.getProductType())) {
            throw new BizException(400, "商品类型不能为空");
        }
        if (!"supplier".equals(dto.getProductType()) && !"platform".equals(dto.getProductType())) {
            throw new BizException(400, "商品类型必须是supplier或platform");
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
            // 自动关联失败不影响商品创建，只记录日志
            log.error("自动关联用户到小程序失败: userId={}, tenantId={}", userId, tenantId, e);
        }
    }
}
