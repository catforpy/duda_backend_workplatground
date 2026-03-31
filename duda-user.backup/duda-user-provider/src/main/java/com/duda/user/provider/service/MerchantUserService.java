package com.duda.user.provider.service;

import com.duda.user.dto.MerchantUserDTO;

import java.util.List;

/**
 * 商户UserService接口
 */
public interface MerchantUserService {

    MerchantUserDTO getMerchantUserById(Long id);

    List<MerchantUserDTO> listMerchantUsersByTenantId(Long tenantId);

    List<MerchantUserDTO> listMerchantUsersByMerchant(Long tenantId, Long merchantId);

    MerchantUserDTO getMerchantUserByUserId(Long tenantId, Long merchantId, String merchantUserId);

    MerchantUserDTO getMerchantUserByOpenid(Long tenantId, Long merchantId, String openid);

    List<MerchantUserDTO> listMerchantUsersPage(Long tenantId, Long merchantId,
                                                  Byte status, Integer pageNum, Integer pageSize);

    MerchantUserDTO createMerchantUser(MerchantUserDTO dto);

    void updateMerchantUser(MerchantUserDTO dto);

    void deleteMerchantUser(Long id);

    int countMerchantUsersByTenantId(Long tenantId);

    int countMerchantUsersByMerchant(Long tenantId, Long merchantId);

    List<MerchantUserDTO> listMerchantUsersByPlatformUser(Long tenantId,
                                                          Long platformUserId, Byte platformUserShard);
}
