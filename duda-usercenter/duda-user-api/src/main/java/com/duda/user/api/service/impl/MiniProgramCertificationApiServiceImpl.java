package com.duda.user.api.service.impl;

import com.duda.user.api.service.MiniProgramCertificationApiService;
import com.duda.user.dto.miniprogram.MiniProgramCertificationDTO;
import com.duda.user.rpc.IMiniProgramCertificationRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小程序认证API服务实现
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class MiniProgramCertificationApiServiceImpl implements MiniProgramCertificationApiService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramCertificationApiServiceImpl.class);

    @DubboReference(version = "1.0.0", group = "DUDA_USER_GROUP", timeout = 30000, check = false)
    private IMiniProgramCertificationRpc miniProgramCertificationRpc;

    @Override
    public MiniProgramCertificationDTO getCertificationById(Long id) {
        log.info("【API服务】查询小程序认证，id={}", id);
        return miniProgramCertificationRpc.getCertificationById(id);
    }

    @Override
    public List<MiniProgramCertificationDTO> listCertificationsByTenantId(Long tenantId) {
        log.info("【API服务】查询租户小程序认证列表，tenantId={}", tenantId);
        return miniProgramCertificationRpc.listCertificationsByTenantId(tenantId);
    }

    @Override
    public MiniProgramCertificationDTO getCertificationByMiniProgramId(Long miniProgramId) {
        log.info("【API服务】根据小程序ID查询认证，miniProgramId={}", miniProgramId);
        return miniProgramCertificationRpc.getCertificationByMiniProgramId(miniProgramId);
    }

    @Override
    public List<MiniProgramCertificationDTO> listCertificationsByStatus(Long tenantId, String certificationStatus) {
        log.info("【API服务】根据状态查询小程序认证列表，tenantId={}, status={}", tenantId, certificationStatus);
        return miniProgramCertificationRpc.listCertificationsByStatus(tenantId, certificationStatus);
    }

    @Override
    public MiniProgramCertificationDTO createCertification(MiniProgramCertificationDTO certificationDTO) {
        log.info("【API服务】创建小程序认证，miniProgramId={}", certificationDTO.getMiniProgramId());
        return miniProgramCertificationRpc.createCertification(certificationDTO);
    }

    @Override
    public void updateCertification(MiniProgramCertificationDTO certificationDTO) {
        log.info("【API服务】更新小程序认证，id={}", certificationDTO.getId());
        miniProgramCertificationRpc.updateCertification(certificationDTO);
    }

    @Override
    public void deleteCertification(Long id) {
        log.info("【API服务】删除小程序认证，id={}", id);
        miniProgramCertificationRpc.deleteCertification(id);
    }
}
