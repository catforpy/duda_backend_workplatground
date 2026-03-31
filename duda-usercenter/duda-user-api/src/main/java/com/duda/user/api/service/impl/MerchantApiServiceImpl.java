package com.duda.user.api.service.impl;

import com.duda.user.api.service.MerchantApiService;
import com.duda.user.dto.merchant.MerchantDTO;
import com.duda.user.rpc.IMerchantRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商户API服务实现
 *
 * 通过Dubbo RPC调用Provider层的MerchantRpcImpl
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class MerchantApiServiceImpl implements MerchantApiService {

    private static final Logger log = LoggerFactory.getLogger(MerchantApiServiceImpl.class);

    /**
     * Dubbo RPC引用 - 调用Provider层的MerchantRpcImpl
     *
     * 配置说明：
     * - version: 1.0.0 (与Provider层@DubboService的version一致)
     * - group: DUDA_USER_GROUP (与Provider层@DubboService的group一致)
     * - timeout: 30000 (30秒超时)
     * - check: false (启动时不检查服务是否可用，避免循环依赖)
     */
    @DubboReference(
        version = "1.0.0",
        group = "DUDA_USER_GROUP",
        timeout = 30000,
        check = false
    )
    private IMerchantRpc merchantRpc;

    @Override
    public MerchantDTO getMerchantById(Long merchantId) {
        log.info("【API服务】查询商户，merchantId={}", merchantId);
        return merchantRpc.getMerchantById(merchantId);
    }

    @Override
    public List<MerchantDTO> listMerchantsByTenantId(Long tenantId) {
        log.info("【API服务】查询租户商户列表，tenantId={}", tenantId);
        return merchantRpc.listMerchantsByTenantId(tenantId);
    }

    @Override
    public MerchantDTO getMerchantByCode(Long tenantId, String merchantCode) {
        log.info("【API服务】根据编码查询商户，tenantId={}, merchantCode={}", tenantId, merchantCode);
        return merchantRpc.getMerchantByCode(tenantId, merchantCode);
    }

    @Override
    public List<MerchantDTO> listMerchantsByStatus(Long tenantId, String status) {
        log.info("【API服务】根据状态查询商户列表，tenantId={}, status={}", tenantId, status);
        return merchantRpc.listMerchantsByStatus(tenantId, status);
    }

    @Override
    public List<MerchantDTO> listMerchantsPage(Long tenantId, String status, Integer pageNum, Integer pageSize) {
        log.info("【API服务】分页查询商户列表，tenantId={}, status={}, pageNum={}, pageSize={}",
                tenantId, status, pageNum, pageSize);
        return merchantRpc.listMerchantsPage(tenantId, status, pageNum, pageSize);
    }

    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
        log.info("【API服务】创建商户，merchantCode={}", merchantDTO.getMerchantCode());
        return merchantRpc.createMerchant(merchantDTO);
    }

    @Override
    public void updateMerchant(MerchantDTO merchantDTO) {
        log.info("【API服务】更新商户，merchantId={}", merchantDTO.getId());
        merchantRpc.updateMerchant(merchantDTO);
    }

    @Override
    public void deleteMerchant(Long merchantId) {
        log.info("【API服务】删除商户，merchantId={}", merchantId);
        merchantRpc.deleteMerchant(merchantId);
    }

    @Override
    public void updateMerchantStatus(Long merchantId, String status, String auditStatus, String auditRemark) {
        log.info("【API服务】更新商户状态，merchantId={}, status={}, auditStatus={}",
                merchantId, status, auditStatus);
        merchantRpc.updateMerchantStatus(merchantId, status, auditStatus, auditRemark);
    }

    @Override
    public int countMerchantsByTenantId(Long tenantId) {
        log.info("【API服务】统计租户商户数量，tenantId={}", tenantId);
        return merchantRpc.countMerchantsByTenantId(tenantId);
    }
}
