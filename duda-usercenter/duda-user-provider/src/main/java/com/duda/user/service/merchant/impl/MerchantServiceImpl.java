package com.duda.user.service.merchant.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duda.common.domain.PageResult;
import com.duda.common.redis.RedisUtils;
import com.duda.common.redis.key.MerchantRedisKeyBuilder;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.merchant.MerchantDTO;
import com.duda.user.entity.merchant.Merchant;
import com.duda.user.mapper.merchant.MerchantMapper;
import com.duda.user.service.merchant.MerchantService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商户服务实现
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantServiceImpl.class);

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private MerchantRedisKeyBuilder redisKeyBuilder;

    @DubboReference(
        group = "INFRA_GROUP",
        version = "1.0.0",
        registry = "infraRegistry"
    )
    private IdGeneratorRpc idGeneratorRpc;

    @Override
    public MerchantDTO getMerchantById(Long merchantId) {
        logger.info("查询商户信息，merchantId:{}", merchantId);

        // 1. 先从缓存获取（参照UserServiceImpl的写法）
        String cacheKey = redisKeyBuilder.buildMerchantInfoKey(merchantId);
        MerchantDTO cachedMerchant = redisUtils.get(cacheKey, MerchantDTO.class);
        if (cachedMerchant != null) {
            logger.info("缓存命中，merchantId:{}", merchantId);
            return cachedMerchant;
        }

        // 2. 从数据库查询
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BizException("商户不存在");
        }

        // 3. 转换为DTO
        MerchantDTO merchantDTO = BeanCopyUtils.copy(merchant, MerchantDTO.class);

        // 4. 存入缓存（1小时）
        redisUtils.set(cacheKey, merchantDTO, 3600);
        logger.info("商户信息已缓存，merchantId:{}, 缓存key:{}", merchantId, cacheKey);

        return merchantDTO;
    }

    @Override
    public MerchantDTO getMerchantByCode(Long tenantId, String merchantCode) {
        logger.info("根据租户ID和编码查询商户，tenantId:{}, merchantCode:{}", tenantId, merchantCode);

        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getTenantId, tenantId);
        wrapper.eq(Merchant::getMerchantCode, merchantCode);
        Merchant merchant = merchantMapper.selectOne(wrapper);

        if (merchant == null) {
            throw new BizException("商户不存在");
        }

        return BeanCopyUtils.copy(merchant, MerchantDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
        logger.info("创建商户，merchantCode:{}", merchantDTO.getMerchantCode());

        // 1. 检查商户编码是否已存在
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getMerchantCode, merchantDTO.getMerchantCode());
        if (merchantMapper.selectCount(wrapper) > 0) {
            throw new BizException("商户编码已存在");
        }

        // 2. 检查小程序AppID是否已存在
        if (StringUtils.hasText(merchantDTO.getMiniAppAppid())) {
            LambdaQueryWrapper<Merchant> appidWrapper = new LambdaQueryWrapper<>();
            appidWrapper.eq(Merchant::getMiniAppAppid, merchantDTO.getMiniAppAppid());
            if (merchantMapper.selectCount(appidWrapper) > 0) {
                throw new BizException("小程序AppID已存在");
            }
        }

        // 3. 创建商户
        Merchant merchant = BeanCopyUtils.copy(merchantDTO, Merchant.class);
        Long merchantId = idGeneratorRpc.generateUserId(); // 使用雪花算法生成ID
        merchant.setId(merchantId);
        merchant.setStatus("pending"); // 默认待审核
        merchant.setDeleted(0);
        merchant.setTotalUsers(0);
        merchant.setTotalOrders(0);
        merchant.setTotalRevenue(java.math.BigDecimal.ZERO);
        merchant.setVisitCount(0L);

        int result = merchantMapper.insert(merchant);
        if (result <= 0) {
            throw new BizException("创建商户失败");
        }

        logger.info("商户创建成功，merchantId:{}, merchantCode:{}", merchantId, merchantDTO.getMerchantCode());
        return getMerchantById(merchantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMerchant(MerchantDTO merchantDTO) {
        logger.info("更新商户信息，merchantId:{}", merchantDTO.getId());

        // 使用LambdaUpdateWrapper更新，避免乐观锁问题
        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Merchant::getId, merchantDTO.getId());

        // 只更新允许修改的字段
        if (StringUtils.hasText(merchantDTO.getMerchantName())) {
            updateWrapper.set(Merchant::getMerchantName, merchantDTO.getMerchantName());
        }
        if (StringUtils.hasText(merchantDTO.getMerchantShortName())) {
            updateWrapper.set(Merchant::getMerchantShortName, merchantDTO.getMerchantShortName());
        }
        if (StringUtils.hasText(merchantDTO.getContactPerson())) {
            updateWrapper.set(Merchant::getContactPerson, merchantDTO.getContactPerson());
        }
        if (StringUtils.hasText(merchantDTO.getContactPhone())) {
            updateWrapper.set(Merchant::getContactPhone, merchantDTO.getContactPhone());
        }
        if (StringUtils.hasText(merchantDTO.getContactEmail())) {
            updateWrapper.set(Merchant::getContactEmail, merchantDTO.getContactEmail());
        }
        if (StringUtils.hasText(merchantDTO.getProvince())) {
            updateWrapper.set(Merchant::getProvince, merchantDTO.getProvince());
        }
        if (StringUtils.hasText(merchantDTO.getCity())) {
            updateWrapper.set(Merchant::getCity, merchantDTO.getCity());
        }
        if (StringUtils.hasText(merchantDTO.getDistrict())) {
            updateWrapper.set(Merchant::getDistrict, merchantDTO.getDistrict());
        }
        if (StringUtils.hasText(merchantDTO.getAddress())) {
            updateWrapper.set(Merchant::getAddress, merchantDTO.getAddress());
        }
        if (StringUtils.hasText(merchantDTO.getIndustry())) {
            updateWrapper.set(Merchant::getIndustry, merchantDTO.getIndustry());
        }
        if (StringUtils.hasText(merchantDTO.getTags())) {
            updateWrapper.set(Merchant::getTags, merchantDTO.getTags());
        }
        if (StringUtils.hasText(merchantDTO.getDescription())) {
            updateWrapper.set(Merchant::getDescription, merchantDTO.getDescription());
        }

        // 更新数据库
        int result = merchantMapper.update(null, updateWrapper);
        if (result <= 0) {
            throw new BizException("更新商户信息失败");
        }

        // 清除缓存
        String cacheKey = redisKeyBuilder.buildMerchantInfoKey(merchantDTO.getId());
        redisUtils.delete(cacheKey);
        logger.info("商户缓存已清除，merchantId:{}, 缓存key:{}", merchantDTO.getId(), cacheKey);

        logger.info("商户信息更新成功，merchantId:{}", merchantDTO.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMerchant(Long merchantId) {
        logger.info("删除商户，merchantId:{}", merchantId);

        // 1. 软删除
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setDeleted(1);

        int result = merchantMapper.updateById(merchant);

        // 2. 清除缓存
        String cacheKey = redisKeyBuilder.buildMerchantInfoKey(merchantId);
        redisUtils.delete(cacheKey);

        logger.info("商户已删除，merchantId:{}", merchantId);
        return result > 0;
    }

    @Override
    public PageResult pageMerchants(
            String merchantType, String status, String keyword,
            Integer pageNum, Integer pageSize) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(merchantType)) {
            wrapper.eq(Merchant::getMerchantType, merchantType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Merchant::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Merchant::getMerchantName, keyword)
                    .or().like(Merchant::getMerchantCode, keyword)
                    .or().like(Merchant::getContactPhone, keyword));
        }

        // 2. 按创建时间倒序
        wrapper.orderByDesc(Merchant::getCreateTime);

        // 3. 分页查询
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        IPage<Merchant> pageResult = merchantMapper.selectPage(page, wrapper);

        // 4. 转换并返回
        List<MerchantDTO> records = BeanCopyUtils.copyList(pageResult.getRecords(), MerchantDTO.class);
        return PageResult.of(records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public List<MerchantDTO> listMerchantsByPlatformUser(Long platformUserId) {
        // TODO: 实现根据平台用户ID查询所有商户的逻辑
        // 需要查询merchant_users表，然后关联merchants表
        logger.info("查询用户的所有商户，platformUserId:{}", platformUserId);
        throw new BizException("功能待实现");
    }

    @Override
    public List<MerchantDTO> listMerchantsByTenantId(Long tenantId) {
        logger.info("查询租户商户列表，tenantId:{}", tenantId);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getTenantId, tenantId);
        wrapper.orderByDesc(Merchant::getCreateTime);
        List<Merchant> merchants = merchantMapper.selectList(wrapper);
        return BeanCopyUtils.copyList(merchants, MerchantDTO.class);
    }

    @Override
    public List<MerchantDTO> listMerchantsByStatus(Long tenantId, String status) {
        logger.info("根据状态查询租户商户列表，tenantId:{}, status:{}", tenantId, status);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getTenantId, tenantId);
        wrapper.eq(Merchant::getStatus, status);
        wrapper.orderByDesc(Merchant::getCreateTime);
        List<Merchant> merchants = merchantMapper.selectList(wrapper);
        return BeanCopyUtils.copyList(merchants, MerchantDTO.class);
    }

    @Override
    public List<MerchantDTO> listMerchantsPage(Long tenantId, String status, Integer pageNum, Integer pageSize) {
        logger.info("分页查询商户列表，tenantId:{}, status:{}, pageNum:{}, pageSize:{}",
                tenantId, status, pageNum, pageSize);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getTenantId, tenantId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Merchant::getStatus, status);
        }
        wrapper.orderByDesc(Merchant::getCreateTime);
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        IPage<Merchant> pageResult = merchantMapper.selectPage(page, wrapper);
        return BeanCopyUtils.copyList(pageResult.getRecords(), MerchantDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantStatus(Long merchantId, String status, String auditStatus, String auditRemark) {
        logger.info("更新商户状态，merchantId:{}, status:{}, auditStatus:{}", merchantId, status, auditStatus);

        // 使用LambdaUpdateWrapper避免乐观锁问题
        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Merchant::getId, merchantId);
        updateWrapper.set(Merchant::getStatus, status);
        if (StringUtils.hasText(auditStatus)) {
            updateWrapper.set(Merchant::getAuditStatus, auditStatus);
        }
        if (StringUtils.hasText(auditRemark)) {
            updateWrapper.set(Merchant::getAuditRemark, auditRemark);
        }

        int result = merchantMapper.update(null, updateWrapper);
        if (result <= 0) {
            throw new BizException("更新商户状态失败");
        }

        // 清除缓存
        String cacheKey = redisKeyBuilder.buildMerchantInfoKey(merchantId);
        redisUtils.delete(cacheKey);
        logger.info("商户缓存已清除，merchantId:{}", merchantId);
    }

    @Override
    public int countMerchantsByTenantId(Long tenantId) {
        logger.info("统计租户商户数量，tenantId:{}", tenantId);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getTenantId, tenantId);
        return merchantMapper.selectCount(wrapper).intValue();
    }
}
