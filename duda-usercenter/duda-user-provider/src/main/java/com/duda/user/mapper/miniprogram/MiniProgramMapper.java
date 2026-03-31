package com.duda.user.mapper.miniprogram;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.miniprogram.MiniProgram;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序Mapper接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Mapper
public interface MiniProgramMapper extends BaseMapper<MiniProgram> {

    /**
     * 根据租户ID查询小程序列表
     */
    List<MiniProgram> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据AppID查询小程序
     */
    MiniProgram selectByAppid(@Param("appid") String appid);

    /**
     * 根据租户ID和状态查询小程序列表
     */
    List<MiniProgram> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);

    /**
     * 根据租户ID和上线状态查询小程序列表
     */
    List<MiniProgram> selectByTenantIdAndOnlineStatus(@Param("tenantId") Long tenantId, @Param("onlineStatus") String onlineStatus);

    /**
     * 根据公司ID查询小程序列表
     */
    List<MiniProgram> selectByCompanyId(@Param("companyId") Long companyId);

    /**
     * 统计租户下的小程序数量
     */
    Integer countByTenantId(@Param("tenantId") Long tenantId);
}
