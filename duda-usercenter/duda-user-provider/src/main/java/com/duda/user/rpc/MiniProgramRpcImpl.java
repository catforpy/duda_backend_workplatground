package com.duda.user.rpc;

import com.duda.user.dto.miniprogram.MiniProgramDTO;
import com.duda.user.service.miniprogram.MiniProgramService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 小程序RPC实现类
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
public class MiniProgramRpcImpl implements IMiniProgramRpc {

    @Resource
    private MiniProgramService miniProgramService;

    @Override
    public MiniProgramDTO getMiniProgramById(Long id) {
        log.info("【RPC Provider】获取小程序，id={}", id);
        return miniProgramService.getMiniProgramById(id);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户小程序列表，tenantId={}", tenantId);
        return miniProgramService.listMiniProgramsByTenantId(tenantId);
    }

    @Override
    public MiniProgramDTO getMiniProgramByAppId(String appid) {
        log.info("【RPC Provider】根据AppID查询小程序，appid={}", appid);
        return miniProgramService.getMiniProgramByAppId(appid);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByStatus(Long tenantId, String status) {
        log.info("【RPC Provider】根据状态查询小程序列表，tenantId={}, status={}", tenantId, status);
        return miniProgramService.listMiniProgramsByStatus(tenantId, status);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByOnlineStatus(Long tenantId, String onlineStatus) {
        log.info("【RPC Provider】根据上线状态查询小程序列表，tenantId={}, onlineStatus={}", tenantId, onlineStatus);
        return miniProgramService.listMiniProgramsByOnlineStatus(tenantId, onlineStatus);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByCompanyId(Long companyId) {
        log.info("【RPC Provider】根据公司ID查询小程序列表，companyId={}", companyId);
        return miniProgramService.listMiniProgramsByCompanyId(companyId);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsPage(Long tenantId, String status, String onlineStatus,
                                                       Integer pageNum, Integer pageSize) {
        log.info("【RPC Provider】分页查询小程序列表，tenantId={}, status={}, onlineStatus={}, pageNum={}, pageSize={}",
                tenantId, status, onlineStatus, pageNum, pageSize);
        return miniProgramService.listMiniProgramsPage(tenantId, status, onlineStatus, pageNum, pageSize);
    }

    @Override
    public MiniProgramDTO createMiniProgram(MiniProgramDTO miniProgramDTO) {
        log.info("【RPC Provider】创建小程序，appid={}", miniProgramDTO.getAppid());
        return miniProgramService.createMiniProgram(miniProgramDTO);
    }

    @Override
    public void updateMiniProgram(MiniProgramDTO miniProgramDTO) {
        log.info("【RPC Provider】更新小程序，id={}", miniProgramDTO.getId());
        miniProgramService.updateMiniProgram(miniProgramDTO);
    }

    @Override
    public void deleteMiniProgram(Long id) {
        log.info("【RPC Provider】删除小程序，id={}", id);
        miniProgramService.deleteMiniProgram(id);
    }

    @Override
    public int countMiniProgramsByTenantId(Long tenantId) {
        log.info("【RPC Provider】统计小程序数量，tenantId={}", tenantId);
        return miniProgramService.countMiniProgramsByTenantId(tenantId);
    }
}
