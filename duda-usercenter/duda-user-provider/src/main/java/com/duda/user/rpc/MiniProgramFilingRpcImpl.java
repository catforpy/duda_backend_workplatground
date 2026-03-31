package com.duda.user.rpc;

import com.duda.user.dto.miniprogram.MiniProgramFilingDTO;
import com.duda.user.service.miniprogram.MiniProgramFilingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 小程序备案RPC实现类
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
public class MiniProgramFilingRpcImpl implements IMiniProgramFilingRpc {

    @Resource
    private MiniProgramFilingService miniProgramFilingService;

    @Override
    public MiniProgramFilingDTO getFilingById(Long id) {
        log.info("【RPC Provider】获取备案信息，id={}", id);
        return miniProgramFilingService.getFilingById(id);
    }

    @Override
    public List<MiniProgramFilingDTO> listFilingsByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户备案列表，tenantId={}", tenantId);
        return miniProgramFilingService.listFilingsByTenantId(tenantId);
    }

    @Override
    public MiniProgramFilingDTO getFilingByMiniProgramId(Long miniProgramId) {
        log.info("【RPC Provider】根据小程序ID查询备案，miniProgramId={}", miniProgramId);
        return miniProgramFilingService.getFilingByMiniProgramId(miniProgramId);
    }

    @Override
    public List<MiniProgramFilingDTO> listFilingsByStatus(Long tenantId, String filingStatus) {
        log.info("【RPC Provider】根据状态查询备案列表，tenantId={}, filingStatus={}", tenantId, filingStatus);
        return miniProgramFilingService.listFilingsByStatus(tenantId, filingStatus);
    }

    @Override
    public MiniProgramFilingDTO createFiling(MiniProgramFilingDTO filingDTO) {
        log.info("【RPC Provider】创建备案信息，miniProgramId={}", filingDTO.getMiniProgramId());
        return miniProgramFilingService.createFiling(filingDTO);
    }

    @Override
    public void updateFiling(MiniProgramFilingDTO filingDTO) {
        log.info("【RPC Provider】更新备案信息，id={}", filingDTO.getId());
        miniProgramFilingService.updateFiling(filingDTO);
    }

    @Override
    public void deleteFiling(Long id) {
        log.info("【RPC Provider】删除备案信息，id={}", id);
        miniProgramFilingService.deleteFiling(id);
    }
}
