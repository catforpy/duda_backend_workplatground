package com.duda.user.service.miniprogram;

import com.duda.user.dto.miniprogram.MiniProgramDTO;

import java.util.List;

/**
 * 小程序服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface MiniProgramService {

    /**
     * 根据ID查询小程序（带缓存）
     *
     * @param id 小程序ID
     * @return 小程序信息
     */
    MiniProgramDTO getMiniProgramById(Long id);

    /**
     * 根据租户ID查询小程序列表（带缓存）
     *
     * @param tenantId 租户ID
     * @return 小程序列表
     */
    List<MiniProgramDTO> listMiniProgramsByTenantId(Long tenantId);

    /**
     * 根据AppID查询小程序（带缓存）
     *
     * @param appid 小程序AppID
     * @return 小程序信息
     */
    MiniProgramDTO getMiniProgramByAppId(String appid);

    /**
     * 根据状态查询小程序列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return 小程序列表
     */
    List<MiniProgramDTO> listMiniProgramsByStatus(Long tenantId, String status);

    /**
     * 根据上线状态查询小程序列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param onlineStatus 上线状态
     * @return 小程序列表
     */
    List<MiniProgramDTO> listMiniProgramsByOnlineStatus(Long tenantId, String onlineStatus);

    /**
     * 根据公司ID查询小程序列表（带缓存）
     *
     * @param companyId 公司ID
     * @return 小程序列表
     */
    List<MiniProgramDTO> listMiniProgramsByCompanyId(Long companyId);

    /**
     * 分页查询小程序列表
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @param onlineStatus 上线状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 小程序列表
     */
    List<MiniProgramDTO> listMiniProgramsPage(Long tenantId, String status, String onlineStatus,
                                             Integer pageNum, Integer pageSize);

    /**
     * 创建小程序
     *
     * @param miniProgramDTO 小程序信息
     * @return 创建的小程序信息
     */
    MiniProgramDTO createMiniProgram(MiniProgramDTO miniProgramDTO);

    /**
     * 更新小程序
     *
     * @param miniProgramDTO 小程序信息
     */
    void updateMiniProgram(MiniProgramDTO miniProgramDTO);

    /**
     * 删除小程序
     *
     * @param id 小程序ID
     */
    void deleteMiniProgram(Long id);

    /**
     * 统计租户下的小程序数量（带缓存）
     *
     * @param tenantId 租户ID
     * @return 小程序数量
     */
    int countMiniProgramsByTenantId(Long tenantId);
}
