package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.SupplyDistributionDTO;
import com.duda.tenant.entity.SupplyDistribution;
import com.duda.tenant.entity.SupplyProduct;
import com.duda.tenant.mapper.SupplyDistributionMapper;
import com.duda.tenant.service.SupplyDistributionService;
import com.duda.tenant.service.SupplyProductService;
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
 * 供应链分销记录服务实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@Service
public class SupplyDistributionServiceImpl
    extends ServiceImpl<SupplyDistributionMapper, SupplyDistribution>
    implements SupplyDistributionService {

    @Autowired
    private SupplyDistributionMapper distributionMapper;

    @Autowired
    private SupplyProductService supplyProductService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyDistribution createDistribution(SupplyDistributionDTO dto) {
        log.info("开始创建分销记录: supplyProductId={}, distributorTenantId={}, stockMode={}, salePrice={}",
            dto.getSupplyProductId(), dto.getDistributorTenantId(), dto.getStockMode(), dto.getSalePrice());

        // 1. 参数校验
        validateCreateParams(dto);

        // 2. 检查商品是否存在
        SupplyProduct product = supplyProductService.getById(dto.getSupplyProductId());
        if (product == null) {
            log.warn("商品不存在: supplyProductId={}", dto.getSupplyProductId());
            throw new BizException(10001, "商品不存在");
        }

        // 3. 检查商品状态
        if (!"on_sale".equals(product.getStatus())) {
            log.warn("商品未上架，无法分销: supplyProductId={}, status={}",
                dto.getSupplyProductId(), product.getStatus());
            throw new BizException(10003, "商品未上架，无法分销");
        }

        // 4. 检查分销关系是否已存在(幂等性)
        LambdaQueryWrapper<SupplyDistribution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyDistribution::getSupplyProductId, dto.getSupplyProductId());
        wrapper.eq(SupplyDistribution::getDistributorTenantId, dto.getDistributorTenantId());
        wrapper.ne(SupplyDistribution::getStatus, "terminated");
        if (count(wrapper) > 0) {
            log.warn("分销关系已存在: supplyProductId={}, distributorTenantId={}",
                dto.getSupplyProductId(), dto.getDistributorTenantId());
            throw new BizException(409, "分销关系已存在");
        }

        // 5. 业务规则校验
        validateBusinessRules(dto, product);

        // 6. 创建分销记录(快照机制)
        SupplyDistribution distribution = new SupplyDistribution();
        BeanUtils.copyProperties(dto, distribution);

        // 快照商品信息
        distribution.setProductCode(product.getProductCode());
        distribution.setProductName(product.getProductName());
        distribution.setSupplyPrice(product.getSupplyPrice()); // 快照供应价
        distribution.setCommissionRate(product.getCommissionRate()); // 快照佣金比例
        // 计算佣金金额
        if (product.getCommissionAmount() != null) {
            distribution.setCommissionAmount(product.getCommissionAmount());
        } else {
            // 佣金金额 = 销售价 * 佣金比例
            distribution.setCommissionAmount(
                dto.getSalePrice().multiply(product.getCommissionRate())
            );
        }

        // 快照发货信息
        distribution.setShippingMode(product.getShippingMode());
        distribution.setShippingAddress(product.getShippingAddress());

        // 设置分销商信息
        distribution.setDistributorTenantCode("TENANT-" + dto.getDistributorTenantId());
        distribution.setDistributorTenantName("租户" + dto.getDistributorTenantId());

        // 设置默认值
        distribution.setStockMode(dto.getStockMode() != null ? dto.getStockMode() : "sync");
        distribution.setLocalStockCount(0);
        distribution.setViewCount(0);
        distribution.setSalesCount(0);
        distribution.setTotalSalesAmount(BigDecimal.ZERO);
        distribution.setStatus("active");
        distribution.setCreatedAt(LocalDateTime.now());
        distribution.setUpdatedAt(LocalDateTime.now());

        // 7. 保存到数据库
        save(distribution);

        // 8. 更新商品的被分销次数
        product.setDistributionCount(product.getDistributionCount() + 1);
        supplyProductService.updateById(product);

        log.info("创建分销记录成功: distributionId={}, supplyProductId={}, distributorTenantId={}",
            distribution.getId(), distribution.getSupplyProductId(), distribution.getDistributorTenantId());

        // TODO: 发送分销创建消息

        return distribution;
    }

    @Override
    public SupplyDistributionDTO getDistributionDTO(Long id) {
        log.debug("查询分销记录: distributionId={}", id);

        if (id == null) {
            throw new BizException(400, "分销记录ID不能为空");
        }

        SupplyDistribution distribution = getById(id);
        if (distribution == null) {
            log.warn("分销记录不存在: distributionId={}", id);
            throw new BizException(20001, "分销记录不存在");
        }

        // 转换为DTO
        SupplyDistributionDTO dto = new SupplyDistributionDTO();
        BeanUtils.copyProperties(distribution, dto);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSalePrice(Long id, BigDecimal salePrice) {
        log.info("开始更新销售价: distributionId={}, salePrice={}", id, salePrice);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "分销记录ID不能为空");
        }
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "销售价必须大于0");
        }

        // 查询分销记录
        SupplyDistribution distribution = getById(id);
        if (distribution == null) {
            throw new BizException(20001, "分销记录不存在");
        }

        // 权限校验(分销商只能操作自己的分销记录)
        // TODO: 从上下文获取当前租户ID进行权限校验
        // Long currentTenantId = TenantContext.getTenantId();
        // if (!currentTenantId.equals(distribution.getDistributorTenantId())) {
        //     log.warn("权限不足: currentTenantId={}, distributorTenantId={}",
        //         currentTenantId, distribution.getDistributorTenantId());
        //     throw new BizException(403, "无权限操作此分销记录");
        // }

        // 业务规则校验
        if ("terminated".equals(distribution.getStatus())) {
            throw new BizException(20002, "分销关系已终止，无法修改");
        }

        // 销售价不能低于供应价
        if (salePrice.compareTo(distribution.getSupplyPrice()) < 0) {
            log.warn("销售价不能低于供应价: salePrice={}, supplyPrice={}",
                salePrice, distribution.getSupplyPrice());
            throw new BizException(10005, "销售价不能低于供应价");
        }

        // 更新销售价
        distribution.setSalePrice(salePrice);
        distribution.setUpdatedAt(LocalDateTime.now());
        updateById(distribution);

        log.info("更新销售价成功: distributionId={}, newSalePrice={}", id, salePrice);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pause(Long id) {
        log.info("开始暂停销售: distributionId={}", id);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "分销记录ID不能为空");
        }

        // 查询分销记录
        SupplyDistribution distribution = getById(id);
        if (distribution == null) {
            throw new BizException(20001, "分销记录不存在");
        }

        // 业务规则校验
        if ("terminated".equals(distribution.getStatus())) {
            throw new BizException(20002, "分销关系已终止，无法暂停");
        }
        if ("paused".equals(distribution.getStatus())) {
            log.warn("分销记录已暂停: distributionId={}", id);
            return true;
        }

        // 更新状态
        distribution.setStatus("paused");
        distribution.setUpdatedAt(LocalDateTime.now());
        updateById(distribution);

        log.info("暂停销售成功: distributionId={}", id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(Long id) {
        log.info("开始恢复销售: distributionId={}", id);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "分销记录ID不能为空");
        }

        // 查询分销记录
        SupplyDistribution distribution = getById(id);
        if (distribution == null) {
            throw new BizException(20001, "分销记录不存在");
        }

        // 业务规则校验
        if ("terminated".equals(distribution.getStatus())) {
            throw new BizException(20002, "分销关系已终止，无法恢复");
        }
        if ("active".equals(distribution.getStatus())) {
            log.warn("分销记录已激活: distributionId={}", id);
            return true;
        }

        // 更新状态
        distribution.setStatus("active");
        distribution.setUpdatedAt(LocalDateTime.now());
        updateById(distribution);

        log.info("恢复销售成功: distributionId={}", id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean terminate(Long id, String reason) {
        log.info("开始终止分销: distributionId={}, reason={}", id, reason);

        // 参数校验
        if (id == null) {
            throw new BizException(400, "分销记录ID不能为空");
        }
        if (StrUtil.isBlank(reason)) {
            throw new BizException(400, "终止原因不能为空");
        }

        // 查询分销记录
        SupplyDistribution distribution = getById(id);
        if (distribution == null) {
            throw new BizException(20001, "分销记录不存在");
        }

        // 业务规则校验
        if ("terminated".equals(distribution.getStatus())) {
            log.warn("分销记录已终止: distributionId={}", id);
            return true;
        }

        // 更新状态
        distribution.setStatus("terminated");
        distribution.setTerminatedAt(LocalDateTime.now());
        distribution.setTerminatedReason(reason);
        distribution.setUpdatedAt(LocalDateTime.now());
        updateById(distribution);

        log.info("终止分销成功: distributionId={}, reason={}", id, reason);

        // TODO: 发送分销终止消息

        return true;
    }

    @Override
    public List<SupplyDistributionDTO> listByDistributor(Long distributorTenantId) {
        log.debug("查询分销商的分销记录列表: distributorTenantId={}", distributorTenantId);

        if (distributorTenantId == null) {
            throw new BizException(400, "分销商租户ID不能为空");
        }

        LambdaQueryWrapper<SupplyDistribution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyDistribution::getDistributorTenantId, distributorTenantId);
        wrapper.ne(SupplyDistribution::getStatus, "terminated");
        wrapper.orderByDesc(SupplyDistribution::getCreatedAt);
        List<SupplyDistribution> list = list(wrapper);

        return list.stream()
            .map(distribution -> {
                SupplyDistributionDTO dto = new SupplyDistributionDTO();
                BeanUtils.copyProperties(distribution, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SupplyDistributionDTO> listByProduct(Long supplyProductId) {
        log.debug("查询商品的分销记录列表: supplyProductId={}", supplyProductId);

        if (supplyProductId == null) {
            throw new BizException(400, "供应链商品ID不能为空");
        }

        LambdaQueryWrapper<SupplyDistribution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplyDistribution::getSupplyProductId, supplyProductId);
        wrapper.ne(SupplyDistribution::getStatus, "terminated");
        wrapper.orderByDesc(SupplyDistribution::getSalesCount);
        List<SupplyDistribution> list = list(wrapper);

        return list.stream()
            .map(distribution -> {
                SupplyDistributionDTO dto = new SupplyDistributionDTO();
                BeanUtils.copyProperties(distribution, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * 校验创建参数
     */
    private void validateCreateParams(SupplyDistributionDTO dto) {
        if (dto == null) {
            throw new BizException(400, "分销信息不能为空");
        }
        if (dto.getSupplyProductId() == null) {
            throw new BizException(400, "供应链商品ID不能为空");
        }
        if (dto.getDistributorTenantId() == null) {
            throw new BizException(400, "分销商租户ID不能为空");
        }
        if (dto.getSalePrice() == null || dto.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "销售价必须大于0");
        }
    }

    /**
     * 校验业务规则
     */
    private void validateBusinessRules(SupplyDistributionDTO dto, SupplyProduct product) {
        // 销售价不能低于供应价
        if (dto.getSalePrice().compareTo(product.getSupplyPrice()) < 0) {
            log.warn("销售价不能低于供应价: salePrice={}, supplyPrice={}",
                dto.getSalePrice(), product.getSupplyPrice());
            throw new BizException(10005, "销售价不能低于供应价");
        }

        // 库存模式校验
        log.info("库存模式校验: stockMode={}, isBlank={}", dto.getStockMode(), StrUtil.isBlank(dto.getStockMode()));
        if (StrUtil.isNotBlank(dto.getStockMode())) {
            if (!"sync".equals(dto.getStockMode()) && !"local".equals(dto.getStockMode())) {
                log.warn("库存模式不合法: stockMode={}", dto.getStockMode());
                throw new BizException(400, "库存模式必须是sync或local");
            }
        }

        // 本地库存数量校验(如果是本地库存模式)
        if ("local".equals(dto.getStockMode()) && dto.getLocalStockCount() != null) {
            if (dto.getLocalStockCount() < 0) {
                throw new BizException(400, "本地库存数量不能为负数");
            }
        }

        // 不能分销自己的商品
        if (dto.getDistributorTenantId().equals(product.getSupplierTenantId())) {
            log.warn("不能分销自己的商品: distributorTenantId={}, supplierTenantId={}",
                dto.getDistributorTenantId(), product.getSupplierTenantId());
            throw new BizException(400, "不能分销自己的商品");
        }
    }
}
