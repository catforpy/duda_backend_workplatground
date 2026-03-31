package com.duda.user.api.service.impl;

import com.duda.user.api.service.MiniProgramFilingApiService;
import com.duda.user.dto.miniprogram.MiniProgramFilingDTO;
import com.duda.user.rpc.IMiniProgramFilingRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MiniProgramFilingApiServiceImpl implements MiniProgramFilingApiService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramFilingApiServiceImpl.class);

    @DubboReference(version = "1.0.0", group = "DUDA_USER_GROUP", timeout = 30000, check = false)
    private IMiniProgramFilingRpc miniProgramFilingRpc;

    @Override
    public MiniProgramFilingDTO getFilingById(Long id) {
        log.info("【API服务】查询备案信息，id={}", id);
        return miniProgramFilingRpc.getFilingById(id);
    }

    @Override
    public List<MiniProgramFilingDTO> listFilingsByTenantId(Long tenantId) {
        log.info("【API服务】查询租户备案列表，tenantId={}", tenantId);
        return miniProgramFilingRpc.listFilingsByTenantId(tenantId);
    }

    @Override
    public MiniProgramFilingDTO getFilingByMiniProgramId(Long miniProgramId) {
        log.info("【API服务】根据小程序ID查询备案，miniProgramId={}", miniProgramId);
        return miniProgramFilingRpc.getFilingByMiniProgramId(miniProgramId);
    }

    @Override
    public List<MiniProgramFilingDTO> listFilingsByStatus(Long tenantId, String filingStatus) {
        log.info("【API服务】根据状态查询备案列表，tenantId={}, status={}", tenantId, filingStatus);
        return miniProgramFilingRpc.listFilingsByStatus(tenantId, filingStatus);
    }

    @Override
    public MiniProgramFilingDTO createFiling(MiniProgramFilingDTO filingDTO) {
        log.info("【API服务】创建备案信息，miniProgramId={}", filingDTO.getMiniProgramId());
        return miniProgramFilingRpc.createFiling(filingDTO);
    }

    @Override
    public void updateFiling(MiniProgramFilingDTO filingDTO) {
        log.info("【API服务】更新备案信息，id={}", filingDTO.getId());
        miniProgramFilingRpc.updateFiling(filingDTO);
    }

    @Override
    public void deleteFiling(Long id) {
        log.info("【API服务】删除备案信息，id={}", id);
        miniProgramFilingRpc.deleteFiling(id);
    }
}
