package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantOssConfigDTO;
import com.duda.tenant.entity.TenantOssConfig;
import com.duda.tenant.mapper.TenantOssConfigMapper;
import com.duda.tenant.service.TenantOssConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * OSS配置表服务实现
 */
@Slf4j
@Service
public class TenantOssConfigServiceImpl
    extends ServiceImpl<TenantOssConfigMapper, TenantOssConfig>
    implements TenantOssConfigService {

    @Autowired
    private TenantOssConfigMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantOssConfig createConfig(TenantOssConfigDTO dto) {
        log.info("创建OSS配置: tenantId={}", dto.getTenantId());
        if (dto.getTenantId() == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        TenantOssConfig entity = new TenantOssConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("active");
        }
        save(entity);
        log.info("创建OSS配置成功: id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantOssConfig updateConfig(TenantOssConfigDTO dto) {
        log.info("更新OSS配置: id={}", dto.getId());
        TenantOssConfig entity = getById(dto.getId());
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        return entity;
    }

    @Override
    public TenantOssConfigDTO getConfigDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantOssConfig entity = getById(id);
        if (entity == null) {
            throw new BizException(46001, "OSS配置不存在");
        }
        TenantOssConfigDTO dto = new TenantOssConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public TenantOssConfigDTO getTenantConfig(Long tenantId) {
        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        TenantOssConfig entity = lambdaQuery()
            .eq(TenantOssConfig::getTenantId, tenantId)
            .eq(TenantOssConfig::getStatus, "active")
            .one();
        if (entity == null) return null;
        TenantOssConfigDTO dto = new TenantOssConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
