package com.duda.user.api.service;

import com.duda.user.dto.merchant.MerchantUserDTO;

import java.util.List;

/**
 * 商户用户API服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface MerchantUserApiService {

    MerchantUserDTO getMerchantUserById(Long id);

    List<MerchantUserDTO> listUsersByMerchantId(Long merchantId);

    List<MerchantUserDTO> listMerchantsByPlatformUserId(Long platformUserId);

    MerchantUserDTO getMerchantUser(Long merchantId, Long platformUserId);

    MerchantUserDTO getMerchantUserByOpenid(Long merchantId, String openid);

    MerchantUserDTO bindMerchantUser(MerchantUserDTO merchantUserDTO);

    Boolean updateVisitInfo(Long merchantId, Long platformUserId);

    Boolean unbindMerchantUser(Long merchantId, Long platformUserId);

    int countUsersByMerchantId(Long merchantId);

    int countMerchantsByPlatformUserId(Long platformUserId);
}
