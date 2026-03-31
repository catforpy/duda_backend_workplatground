package com.duda.user.api.service.impl;

import com.duda.common.context.TenantContext;
import com.duda.user.api.service.MerchantUserApiService;
import com.duda.user.dto.merchant.MerchantUserDTO;
import com.duda.user.rpc.IMerchantUserRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商户用户API服务实现
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class MerchantUserApiServiceImpl implements MerchantUserApiService {

    private static final Logger log = LoggerFactory.getLogger(MerchantUserApiServiceImpl.class);

    @DubboReference(version = "1.0.0", group = "DUDA_USER_GROUP", timeout = 30000, check = false)
    private IMerchantUserRpc merchantUserRpc;

    @Override
    public MerchantUserDTO getMerchantUserById(Long id) {
        log.info("【API服务】查询商户用户，id={}", id);
        return merchantUserRpc.getMerchantUserById(id);
    }

    @Override
    public List<MerchantUserDTO> listUsersByMerchantId(Long merchantId) {
        Long tenantId = TenantContext.getTenantId();
        log.info("【API服务】查询商户用户列表，tenantId={}, merchantId={}", tenantId, merchantId);
        return merchantUserRpc.listMerchantUsersByMerchant(tenantId, merchantId);
    }

    @Override
    public List<MerchantUserDTO> listMerchantsByPlatformUserId(Long platformUserId) {
        Long tenantId = TenantContext.getTenantId();
        log.info("【API服务】查询用户的商户列表，tenantId={}, platformUserId={}", tenantId, platformUserId);
        // 使用默认分片值0
        return merchantUserRpc.listMerchantUsersByPlatformUser(tenantId, platformUserId, (byte) 0);
    }

    @Override
    public MerchantUserDTO getMerchantUser(Long merchantId, Long platformUserId) {
        Long tenantId = TenantContext.getTenantId();
        log.info("【API服务】查询商户用户关系，tenantId={}, merchantId={}, platformUserId={}",
                tenantId, merchantId, platformUserId);
        // 使用merchantUserId参数，这里传platformUserId作为merchantUserId
        return merchantUserRpc.getMerchantUserByUserId(tenantId, merchantId, String.valueOf(platformUserId));
    }

    @Override
    public MerchantUserDTO getMerchantUserByOpenid(Long merchantId, String openid) {
        Long tenantId = TenantContext.getTenantId();
        log.info("【API服务】根据OpenID查询商户用户，tenantId={}, merchantId={}, openid={}",
                tenantId, merchantId, openid);
        return merchantUserRpc.getMerchantUserByOpenid(tenantId, merchantId, openid);
    }

    @Override
    public MerchantUserDTO bindMerchantUser(MerchantUserDTO merchantUserDTO) {
        log.info("【API服务】绑定商户用户，merchantId={}, platformUserId={}",
                merchantUserDTO.getMerchantId(), merchantUserDTO.getPlatformUserId());
        return merchantUserRpc.createMerchantUser(merchantUserDTO);
    }

    @Override
    public Boolean updateVisitInfo(Long merchantId, Long platformUserId) {
        log.info("【API服务】更新访问信息，merchantId={}, platformUserId={}", merchantId, platformUserId);
        // 创建一个DTO用于更新
        MerchantUserDTO dto = new MerchantUserDTO();
        dto.setMerchantId(merchantId);
        dto.setPlatformUserId(platformUserId);
        merchantUserRpc.updateMerchantUser(dto);
        return true;
    }

    @Override
    public Boolean unbindMerchantUser(Long merchantId, Long platformUserId) {
        log.info("【API服务】解绑商户用户，merchantId={}, platformUserId={}", merchantId, platformUserId);
        // TODO: 需要先查询获取ID，然后删除
        return true;
    }

    @Override
    public int countUsersByMerchantId(Long merchantId) {
        Long tenantId = TenantContext.getTenantId();
        log.info("【API服务】统计商户用户数量，tenantId={}, merchantId={}", tenantId, merchantId);
        return merchantUserRpc.countMerchantUsersByMerchant(tenantId, merchantId);
    }

    @Override
    public int countMerchantsByPlatformUserId(Long platformUserId) {
        log.info("【API服务】统计用户的商户数量，platformUserId={}", platformUserId);
        return merchantUserRpc.countMerchantsByPlatformUserId(platformUserId);
    }
}
