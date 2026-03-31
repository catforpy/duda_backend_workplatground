package com.duda.user.api.service.impl;

import com.duda.user.api.service.MiniProgramApiService;
import com.duda.user.dto.miniprogram.MiniProgramDTO;
import com.duda.user.rpc.IMiniProgramRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小程序API服务实现
 *
 * 通过Dubbo RPC调用Provider层的MiniProgramRpcImpl
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class MiniProgramApiServiceImpl implements MiniProgramApiService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramApiServiceImpl.class);

    /**
     * Dubbo RPC引用 - 调用Provider层的MiniProgramRpcImpl
     *
     * 配置说明：
     * - version: 1.0.0 (与Provider层@DubboService的version一致)
     * - group: DUDA_USER_GROUP (与Provider层@DubboService的group一致)
     * - timeout: 30000 (30秒超时)
     * - check: false (启动时不检查服务是否可用)
     */
    @DubboReference(
        version = "1.0.0",
        group = "DUDA_USER_GROUP",
        timeout = 30000,
        check = false
    )
    private IMiniProgramRpc miniProgramRpc;

    @Override
    public MiniProgramDTO getMiniProgramById(Long id) {
        log.info("【API服务】查询小程序，id={}", id);
        return miniProgramRpc.getMiniProgramById(id);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByTenantId(Long tenantId) {
        log.info("【API服务】查询租户小程序列表，tenantId={}", tenantId);
        return miniProgramRpc.listMiniProgramsByTenantId(tenantId);
    }

    @Override
    public MiniProgramDTO getMiniProgramByAppId(String appid) {
        log.info("【API服务】根据AppID查询小程序，appid={}", appid);
        return miniProgramRpc.getMiniProgramByAppId(appid);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByStatus(Long tenantId, String status) {
        log.info("【API服务】根据状态查询小程序列表，tenantId={}, status={}", tenantId, status);
        return miniProgramRpc.listMiniProgramsByStatus(tenantId, status);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByOnlineStatus(Long tenantId, String onlineStatus) {
        log.info("【API服务】根据上线状态查询小程序列表，tenantId={}, onlineStatus={}", tenantId, onlineStatus);
        return miniProgramRpc.listMiniProgramsByOnlineStatus(tenantId, onlineStatus);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsByCompanyId(Long companyId) {
        log.info("【API服务】根据公司ID查询小程序列表，companyId={}", companyId);
        return miniProgramRpc.listMiniProgramsByCompanyId(companyId);
    }

    @Override
    public List<MiniProgramDTO> listMiniProgramsPage(Long tenantId, String status, String onlineStatus,
                                                    Integer pageNum, Integer pageSize) {
        log.info("【API服务】分页查询小程序列表，tenantId={}, status={}, onlineStatus={}, pageNum={}, pageSize={}",
                tenantId, status, onlineStatus, pageNum, pageSize);
        return miniProgramRpc.listMiniProgramsPage(tenantId, status, onlineStatus, pageNum, pageSize);
    }

    @Override
    public MiniProgramDTO createMiniProgram(MiniProgramDTO miniProgramDTO) {
        log.info("【API服务】创建小程序，appid={}", miniProgramDTO.getAppid());
        return miniProgramRpc.createMiniProgram(miniProgramDTO);
    }

    @Override
    public void updateMiniProgram(MiniProgramDTO miniProgramDTO) {
        log.info("【API服务】更新小程序，id={}", miniProgramDTO.getId());
        miniProgramRpc.updateMiniProgram(miniProgramDTO);
    }

    @Override
    public void deleteMiniProgram(Long id) {
        log.info("【API服务】删除小程序，id={}", id);
        miniProgramRpc.deleteMiniProgram(id);
    }

    @Override
    public int countMiniProgramsByTenantId(Long tenantId) {
        log.info("【API服务】统计租户小程序数量，tenantId={}", tenantId);
        return miniProgramRpc.countMiniProgramsByTenantId(tenantId);
    }
}
