package com.duda.user.rpc;

import com.duda.user.dto.merchant.MerchantUserDTO;
import com.duda.user.service.merchant.MerchantUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 商户用户RPC实现类
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
public class MerchantUserRpcImpl implements IMerchantUserRpc {

    @Resource
    private MerchantUserService merchantUserService;

    @Override
    public MerchantUserDTO getMerchantUserById(Long id) {
        log.info("【RPC Provider】获取商户用户，id={}", id);
        return merchantUserService.getMerchantUserById(id);
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户商户用户列表，tenantId={}", tenantId);
        return merchantUserService.listMerchantUsersByTenantId(tenantId);
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersByMerchant(Long tenantId, Long merchantId) {
        log.info("【RPC Provider】查询商户用户列表，tenantId={}, merchantId={}", tenantId, merchantId);
        return merchantUserService.listMerchantUsersByMerchant(tenantId, merchantId);
    }

    @Override
    public MerchantUserDTO getMerchantUserByUserId(Long tenantId, Long merchantId, String merchantUserId) {
        log.info("【RPC Provider】根据用户ID查询商户用户，tenantId={}, merchantId={}, merchantUserId={}",
                tenantId, merchantId, merchantUserId);
        return merchantUserService.getMerchantUserByUserId(tenantId, merchantId, merchantUserId);
    }

    @Override
    public MerchantUserDTO getMerchantUserByOpenid(Long tenantId, Long merchantId, String openid) {
        log.info("【RPC Provider】根据OpenID查询商户用户，tenantId={}, merchantId={}, openid={}",
                tenantId, merchantId, openid);
        return merchantUserService.getMerchantUserByOpenid(tenantId, merchantId, openid);
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersByPlatformUser(Long tenantId, Long platformUserId, Byte platformUserShard) {
        log.info("【RPC Provider】根据平台用户查询商户列表，tenantId={}, platformUserId={}, platformUserShard={}",
                tenantId, platformUserId, platformUserShard);
        return merchantUserService.listMerchantUsersByPlatformUser(tenantId, platformUserId, platformUserShard);
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersPage(Long tenantId, Long merchantId, Byte status,
                                                        Integer pageNum, Integer pageSize) {
        log.info("【RPC Provider】分页查询商户用户列表，tenantId={}, merchantId={}, status={}, pageNum={}, pageSize={}",
                tenantId, merchantId, status, pageNum, pageSize);
        return merchantUserService.listMerchantUsersPage(tenantId, merchantId, status, pageNum, pageSize);
    }

    @Override
    public MerchantUserDTO createMerchantUser(MerchantUserDTO merchantUserDTO) {
        log.info("【RPC Provider】创建商户用户，merchantUserId={}", merchantUserDTO.getMerchantUserId());
        return merchantUserService.createMerchantUser(merchantUserDTO);
    }

    @Override
    public void updateMerchantUser(MerchantUserDTO merchantUserDTO) {
        log.info("【RPC Provider】更新商户用户，id={}", merchantUserDTO.getId());
        merchantUserService.updateMerchantUser(merchantUserDTO);
    }

    @Override
    public void deleteMerchantUser(Long id) {
        log.info("【RPC Provider】删除商户用户，id={}", id);
        merchantUserService.deleteMerchantUser(id);
    }

    @Override
    public int countMerchantUsersByTenantId(Long tenantId) {
        log.info("【RPC Provider】统计租户商户用户数量，tenantId={}", tenantId);
        return merchantUserService.countMerchantUsersByTenantId(tenantId);
    }

    @Override
    public int countMerchantUsersByMerchant(Long tenantId, Long merchantId) {
        log.info("【RPC Provider】统计商户用户数量，tenantId={}, merchantId={}", tenantId, merchantId);
        return merchantUserService.countMerchantUsersByMerchant(tenantId, merchantId);
    }

    @Override
    public int countMerchantsByPlatformUserId(Long platformUserId) {
        log.info("【RPC Provider】统计用户的商户数量，platformUserId={}", platformUserId);
        return merchantUserService.countMerchantsByPlatformUserId(platformUserId);
    }
}
