package com.duda.user.rpc;

import com.duda.user.dto.miniprogram.MiniProgramCertificationDTO;
import com.duda.user.service.miniprogram.MiniProgramCertificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 小程序认证RPC实现类
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
public class MiniProgramCertificationRpcImpl implements IMiniProgramCertificationRpc {

    @Resource
    private MiniProgramCertificationService miniProgramCertificationService;

    @Override
    public MiniProgramCertificationDTO getCertificationById(Long id) {
        log.info("【RPC Provider】获取认证信息，id={}", id);
        return miniProgramCertificationService.getCertificationById(id);
    }

    @Override
    public List<MiniProgramCertificationDTO> listCertificationsByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户认证列表，tenantId={}", tenantId);
        return miniProgramCertificationService.listCertificationsByTenantId(tenantId);
    }

    @Override
    public MiniProgramCertificationDTO getCertificationByMiniProgramId(Long miniProgramId) {
        log.info("【RPC Provider】根据小程序ID查询认证，miniProgramId={}", miniProgramId);
        return miniProgramCertificationService.getCertificationByMiniProgramId(miniProgramId);
    }

    @Override
    public List<MiniProgramCertificationDTO> listCertificationsByStatus(Long tenantId, String certificationStatus) {
        log.info("【RPC Provider】根据状态查询认证列表，tenantId={}, certificationStatus={}", tenantId, certificationStatus);
        return miniProgramCertificationService.listCertificationsByStatus(tenantId, certificationStatus);
    }

    @Override
    public MiniProgramCertificationDTO createCertification(MiniProgramCertificationDTO certificationDTO) {
        log.info("【RPC Provider】创建认证信息，miniProgramId={}", certificationDTO.getMiniProgramId());
        return miniProgramCertificationService.createCertification(certificationDTO);
    }

    @Override
    public void updateCertification(MiniProgramCertificationDTO certificationDTO) {
        log.info("【RPC Provider】更新认证信息，id={}", certificationDTO.getId());
        miniProgramCertificationService.updateCertification(certificationDTO);
    }

    @Override
    public void deleteCertification(Long id) {
        log.info("【RPC Provider】删除认证信息，id={}", id);
        miniProgramCertificationService.deleteCertification(id);
    }
}
