package com.duda.user.rpc;

import com.duda.user.dto.merchant.MerchantDTO;
import com.duda.user.service.merchant.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 商户RPC实现类
 *
 * ⚠️ 重要：
 * 1. 实现IMerchantRpc接口
 * 2. 使用@DubboService注册到Nacos，供api层调用
 * 3. 委托给MerchantService处理业务逻辑
 * 4. 这是provider层的RPC入口，不是业务逻辑实现
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "DUDA_USER_GROUP",
    timeout = 30000
)
public class MerchantRpcImpl implements IMerchantRpc {

    @Resource
    private MerchantService merchantService;

    @Override
    public MerchantDTO getMerchantById(Long merchantId) {
        log.info("【RPC Provider】获取商户信息，merchantId={}", merchantId);
        return merchantService.getMerchantById(merchantId);
    }

    @Override
    public List<MerchantDTO> listMerchantsByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户商户列表，tenantId={}", tenantId);
        return merchantService.listMerchantsByTenantId(tenantId);
    }

    @Override
    public MerchantDTO getMerchantByCode(Long tenantId, String merchantCode) {
        log.info("【RPC Provider】根据编码查询商户，tenantId={}, merchantCode={}", tenantId, merchantCode);
        return merchantService.getMerchantByCode(tenantId, merchantCode);
    }

    @Override
    public List<MerchantDTO> listMerchantsByStatus(Long tenantId, String status) {
        log.info("【RPC Provider】根据状态查询商户列表，tenantId={}, status={}", tenantId, status);
        return merchantService.listMerchantsByStatus(tenantId, status);
    }

    @Override
    public List<MerchantDTO> listMerchantsPage(Long tenantId, String status, Integer pageNum, Integer pageSize) {
        log.info("【RPC Provider】分页查询商户列表，tenantId={}, status={}, pageNum={}, pageSize={}",
                tenantId, status, pageNum, pageSize);
        return merchantService.listMerchantsPage(tenantId, status, pageNum, pageSize);
    }

    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
        log.info("【RPC Provider】创建商户，merchantCode={}", merchantDTO.getMerchantCode());
        MerchantDTO created = merchantService.createMerchant(merchantDTO);
        if (created != null) {
            return created;
        }
        // 如果Service返回的是旧格式（Long），需要查询返回完整DTO
        // 这里假设Service已经正确返回MerchantDTO
        return created;
    }

    @Override
    public void updateMerchant(MerchantDTO merchantDTO) {
        log.info("【RPC Provider】更新商户，id={}", merchantDTO.getId());
        merchantService.updateMerchant(merchantDTO);
    }

    @Override
    public void deleteMerchant(Long merchantId) {
        log.info("【RPC Provider】删除商户，merchantId={}", merchantId);
        merchantService.deleteMerchant(merchantId);
    }

    @Override
    public void updateMerchantStatus(Long merchantId, String status, String auditStatus, String auditRemark) {
        log.info("【RPC Provider】更新商户状态，merchantId={}, status={}, auditStatus={}",
                merchantId, status, auditStatus);
        merchantService.updateMerchantStatus(merchantId, status, auditStatus, auditRemark);
    }

    @Override
    public int countMerchantsByTenantId(Long tenantId) {
        log.info("【RPC Provider】统计商户数量，tenantId={}", tenantId);
        return merchantService.countMerchantsByTenantId(tenantId);
    }
}
