package com.duda.user.provider.service;

import com.duda.user.dto.MiniProgramDTO;

import java.util.List;

/**
 * 小程序Service接口
 */
public interface MiniProgramService {

    MiniProgramDTO getMiniProgramById(Long id);

    List<MiniProgramDTO> listMiniProgramsByTenantId(Long tenantId);

    MiniProgramDTO getMiniProgramByAppId(Long tenantId, String appid);

    List<MiniProgramDTO> listMiniProgramsByTenantIdAndStatus(Long tenantId, String status);

    List<MiniProgramDTO> listMiniProgramsPage(Long tenantId, String status,
                                                String onlineStatus, Integer pageNum, Integer pageSize);

    MiniProgramDTO createMiniProgram(MiniProgramDTO dto);

    void updateMiniProgram(MiniProgramDTO dto);

    void deleteMiniProgram(Long id);

    int countMiniProgramsByTenantId(Long tenantId);
}
