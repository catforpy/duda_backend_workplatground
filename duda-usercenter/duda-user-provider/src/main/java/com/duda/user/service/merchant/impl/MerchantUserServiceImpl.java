package com.duda.user.service.merchant.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duda.common.redis.RedisUtils;
import com.duda.common.redis.key.MerchantRedisKeyBuilder;
import com.duda.common.util.BeanCopyUtils;
import com.duda.common.web.exception.BizException;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.merchant.MerchantUserDTO;
import com.duda.user.entity.merchant.MerchantUser;
import com.duda.user.mapper.merchant.MerchantUserMapper;
import com.duda.user.service.merchant.MerchantUserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户用户映射服务实现
 *
 * 说明：多租户SAAS模式的核心服务实现
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Service
public class MerchantUserServiceImpl implements MerchantUserService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantUserServiceImpl.class);

    @Resource
    private MerchantUserMapper merchantUserMapper;

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
    public List<MerchantUserDTO> listUsersByMerchantId(Long merchantId) {
        logger.info("查询商户用户列表，merchantId:{}", merchantId);

        // 1. 先从缓存获取
        String cacheKey = redisKeyBuilder.buildMerchantUserListKey(merchantId);
        List<MerchantUserDTO> cachedUsers = redisUtils.get(cacheKey, List.class);
        if (cachedUsers != null && !cachedUsers.isEmpty()) {
            logger.info("缓存命中，商户用户列表，merchantId:{}", merchantId);
            return cachedUsers;
        }

        // 2. 从数据库查询
        List<MerchantUser> merchantUsers = merchantUserMapper.selectByMerchantId(merchantId);

        // 3. 转换为DTO
        List<MerchantUserDTO> merchantUserDTOs = BeanCopyUtils.copyList(merchantUsers, MerchantUserDTO.class);

        // 4. 存入缓存（30分钟）
        if (merchantUserDTOs != null && !merchantUserDTOs.isEmpty()) {
            redisUtils.set(cacheKey, merchantUserDTOs, 1800);
            logger.info("商户用户列表已缓存，merchantId:{}, 用户数:{}", merchantId, merchantUserDTOs.size());
        }

        return merchantUserDTOs;
    }

    @Override
    public List<MerchantUserDTO> listMerchantsByPlatformUserId(Long platformUserId) {
        logger.info("查询用户的所有商户，platformUserId:{}", platformUserId);

        // 1. 先从缓存获取
        String cacheKey = redisKeyBuilder.buildPlatformUserMerchantsKey(platformUserId);
        List<MerchantUserDTO> cachedMerchants = redisUtils.get(cacheKey, List.class);
        if (cachedMerchants != null && !cachedMerchants.isEmpty()) {
            logger.info("缓存命中，用户商户列表，platformUserId:{}", platformUserId);
            return cachedMerchants;
        }

        // 2. 从数据库查询
        List<MerchantUser> merchantUsers = merchantUserMapper.selectByPlatformUserId(platformUserId);

        // 3. 转换为DTO
        List<MerchantUserDTO> merchantUserDTOs = BeanCopyUtils.copyList(merchantUsers, MerchantUserDTO.class);

        // 4. 存入缓存（30分钟）
        if (merchantUserDTOs != null && !merchantUserDTOs.isEmpty()) {
            redisUtils.set(cacheKey, merchantUserDTOs, 1800);
            logger.info("用户商户列表已缓存，platformUserId:{}, 商户数:{}", platformUserId, merchantUserDTOs.size());
        }

        return merchantUserDTOs;
    }

    @Override
    public MerchantUserDTO getMerchantUser(Long merchantId, Long platformUserId) {
        logger.info("查询商户用户映射，merchantId:{}, platformUserId:{}", merchantId, platformUserId);

        // 1. 先从缓存获取
        String cacheKey = redisKeyBuilder.buildMerchantUserKey(merchantId, platformUserId);
        MerchantUserDTO cachedUser = redisUtils.get(cacheKey, MerchantUserDTO.class);
        if (cachedUser != null) {
            logger.info("缓存命中，商户用户映射");
            return cachedUser;
        }

        // 2. 从数据库查询
        MerchantUser merchantUser = merchantUserMapper.selectByMerchantIdAndPlatformUserId(merchantId, platformUserId);
        if (merchantUser == null) {
            throw new BizException("用户未绑定该商户");
        }

        // 3. 转换为DTO
        MerchantUserDTO merchantUserDTO = BeanCopyUtils.copy(merchantUser, MerchantUserDTO.class);

        // 4. 存入缓存（1小时）
        redisUtils.set(cacheKey, merchantUserDTO, 3600);
        logger.info("商户用户映射已缓存，merchantId:{}, platformUserId:{}", merchantId, platformUserId);

        return merchantUserDTO;
    }

    @Override
    public MerchantUserDTO getMerchantUserByOpenid(Long merchantId, String openid) {
        logger.info("根据OpenID查询商户用户，merchantId:{}, openid:{}", merchantId, openid);

        // 1. 从数据库查询（OpenID查询不缓存）
        MerchantUser merchantUser = merchantUserMapper.selectByMerchantIdAndOpenid(merchantId, openid);
        if (merchantUser == null) {
            throw new BizException("用户不存在");
        }

        return BeanCopyUtils.copy(merchantUser, MerchantUserDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindMerchantUser(MerchantUserDTO merchantUserDTO) {
        logger.info("绑定商户用户，merchantId:{}, platformUserId:{}",
            merchantUserDTO.getMerchantId(), merchantUserDTO.getPlatformUserId());

        // 1. 检查是否已经绑定
        MerchantUser existing = merchantUserMapper.selectByMerchantIdAndPlatformUserId(
            merchantUserDTO.getMerchantId(),
            merchantUserDTO.getPlatformUserId()
        );

        if (existing != null) {
            // 已绑定，更新信息
            existing.setNickname(merchantUserDTO.getNickname());
            existing.setAvatar(merchantUserDTO.getAvatar());
            existing.setGender(merchantUserDTO.getGender());
            existing.setMiniAppOpenid(merchantUserDTO.getMiniAppOpenid());
            merchantUserMapper.updateById(existing);
            logger.info("更新商户用户信息，id:{}", existing.getId());
            return true;
        }

        // 2. 新建绑定关系
        MerchantUser merchantUser = BeanCopyUtils.copy(merchantUserDTO, MerchantUser.class);
        Long id = idGeneratorRpc.generateUserId();
        merchantUser.setId(id);

        // 计算分表shard值（根据platformUserId）
        int platformUserShard = (int) (merchantUserDTO.getPlatformUserId() % 100);
        merchantUser.setPlatformUserShard(platformUserShard);

        // 生成商户虚拟ID（格式：M{merchantId}_{序号}）
        String merchantUserId = String.format("M%d_%03d",
            merchantUserDTO.getMerchantId(),
            System.currentTimeMillis() % 1000
        );
        merchantUser.setMerchantUserId(merchantUserId);

        merchantUser.setStatus(1); // 正常
        merchantUser.setFollowStatus(0); // 未关注
        merchantUser.setVisitCount(0);
        merchantUser.setOrderCount(0);
        merchantUser.setConsumptionAmount(java.math.BigDecimal.ZERO);
        merchantUser.setBindTime(LocalDateTime.now());
        merchantUser.setFirstVisitTime(LocalDateTime.now());

        int result = merchantUserMapper.insert(merchantUser);
        if (result <= 0) {
            throw new BizException("绑定失败");
        }

        // 3. 清除相关缓存
        String listCacheKey = redisKeyBuilder.buildMerchantUserListKey(merchantUserDTO.getMerchantId());
        redisUtils.delete(listCacheKey);

        String platformCacheKey = redisKeyBuilder.buildPlatformUserMerchantsKey(merchantUserDTO.getPlatformUserId());
        redisUtils.delete(platformCacheKey);

        logger.info("商户用户绑定成功，id:{}, merchantUserId:{}", id, merchantUserId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateVisitInfo(Long merchantId, Long platformUserId) {
        logger.info("更新用户访问信息，merchantId:{}, platformUserId:{}", merchantId, platformUserId);

        // 1. 查询映射关系
        MerchantUser merchantUser = merchantUserMapper.selectByMerchantIdAndPlatformUserId(merchantId, platformUserId);
        if (merchantUser == null) {
            throw new BizException("用户未绑定该商户");
        }

        // 2. 更新访问信息
        merchantUser.setVisitCount(merchantUser.getVisitCount() + 1);
        merchantUser.setLastVisitTime(LocalDateTime.now());

        int result = merchantUserMapper.updateById(merchantUser);

        // 3. 清除单个用户的缓存
        String cacheKey = redisKeyBuilder.buildMerchantUserKey(merchantId, platformUserId);
        redisUtils.delete(cacheKey);

        logger.info("用户访问信息更新成功，merchantId:{}, platformUserId:{}, 访问次数:{}",
            merchantId, platformUserId, merchantUser.getVisitCount());

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unbindMerchantUser(Long merchantId, Long platformUserId) {
        logger.info("解绑商户用户，merchantId:{}, platformUserId:{}", merchantId, platformUserId);

        // 1. 软删除
        MerchantUser merchantUser = new MerchantUser();
        merchantUser.setMerchantId(merchantId);
        merchantUser.setPlatformUserId(platformUserId);
        merchantUser.setDeleted(1);

        // 使用自定义方法删除（因为需要联合条件）
        // 这里需要使用MyBatis-Plus的LambdaUpdateWrapper
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<MerchantUser> wrapper =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        wrapper.eq(MerchantUser::getMerchantId, merchantId)
               .eq(MerchantUser::getPlatformUserId, platformUserId)
               .set(MerchantUser::getDeleted, 1);

        int result = merchantUserMapper.update(null, wrapper);

        // 2. 清除所有相关缓存
        String listCacheKey = redisKeyBuilder.buildMerchantUserListKey(merchantId);
        redisUtils.delete(listCacheKey);

        String platformCacheKey = redisKeyBuilder.buildPlatformUserMerchantsKey(platformUserId);
        redisUtils.delete(platformCacheKey);

        String userCacheKey = redisKeyBuilder.buildMerchantUserKey(merchantId, platformUserId);
        redisUtils.delete(userCacheKey);

        logger.info("商户用户解绑成功，merchantId:{}, platformUserId:{}", merchantId, platformUserId);
        return result > 0;
    }

    @Override
    public MerchantUserDTO getMerchantUserById(Long id) {
        logger.info("查询商户用户，id:{}", id);
        MerchantUser merchantUser = merchantUserMapper.selectById(id);
        if (merchantUser == null) {
            throw new BizException("商户用户不存在");
        }
        return BeanCopyUtils.copy(merchantUser, MerchantUserDTO.class);
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersByTenantId(Long tenantId) {
        logger.info("查询租户商户用户列表，tenantId:{}", tenantId);
        // TODO: 实现租户级别的商户用户列表查询
        throw new BizException("功能待实现");
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersByMerchant(Long tenantId, Long merchantId) {
        logger.info("查询商户用户列表，tenantId:{}, merchantId:{}", tenantId, merchantId);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getMerchantId, merchantId);
        wrapper.orderByDesc(MerchantUser::getBindTime);
        List<MerchantUser> list = merchantUserMapper.selectList(wrapper);
        return BeanCopyUtils.copyList(list, MerchantUserDTO.class);
    }

    @Override
    public MerchantUserDTO getMerchantUserByUserId(Long tenantId, Long merchantId, String merchantUserId) {
        logger.info("根据商户用户ID查询，tenantId:{}, merchantId:{}, merchantUserId:{}",
                tenantId, merchantId, merchantUserId);

        // 实际上merchantUserId参数是platformUserId
        Long platformUserId = Long.parseLong(merchantUserId);

        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getMerchantId, merchantId);
        wrapper.eq(MerchantUser::getPlatformUserId, platformUserId);

        MerchantUser merchantUser = merchantUserMapper.selectOne(wrapper);
        if (merchantUser == null) {
            return null;
        }
        return BeanCopyUtils.copy(merchantUser, MerchantUserDTO.class);
    }

    @Override
    public MerchantUserDTO getMerchantUserByOpenid(Long tenantId, Long merchantId, String openid) {
        logger.info("根据OpenID查询商户用户，tenantId:{}, merchantId:{}, openid:{}",
                tenantId, merchantId, openid);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getMerchantId, merchantId);
        wrapper.eq(MerchantUser::getMiniAppOpenid, openid);
        MerchantUser merchantUser = merchantUserMapper.selectOne(wrapper);
        if (merchantUser == null) {
            return null;
        }
        return BeanCopyUtils.copy(merchantUser, MerchantUserDTO.class);
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersByPlatformUser(Long tenantId, Long platformUserId, Byte platformUserShard) {
        logger.info("查询用户的商户列表，tenantId:{}, platformUserId:{}", tenantId, platformUserId);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getPlatformUserId, platformUserId);
        wrapper.orderByDesc(MerchantUser::getBindTime);
        List<MerchantUser> list = merchantUserMapper.selectList(wrapper);
        return BeanCopyUtils.copyList(list, MerchantUserDTO.class);
    }

    @Override
    public List<MerchantUserDTO> listMerchantUsersPage(Long tenantId, Long merchantId, Byte status,
                                                        Integer pageNum, Integer pageSize) {
        logger.info("分页查询商户用户，tenantId:{}, merchantId:{}, status:{}, pageNum:{}, pageSize:{}",
                tenantId, merchantId, status, pageNum, pageSize);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getMerchantId, merchantId);
        if (status != null) {
            wrapper.eq(MerchantUser::getStatus, status.intValue());
        }
        wrapper.orderByDesc(MerchantUser::getBindTime);
        Page<MerchantUser> page = new Page<>(pageNum, pageSize);
        IPage<MerchantUser> pageResult = merchantUserMapper.selectPage(page, wrapper);
        return BeanCopyUtils.copyList(pageResult.getRecords(), MerchantUserDTO.class);
    }

    @Override
    public MerchantUserDTO createMerchantUser(MerchantUserDTO merchantUserDTO) {
        logger.info("创建商户用户，merchantId:{}, platformUserId:{}",
                merchantUserDTO.getMerchantId(), merchantUserDTO.getPlatformUserId());
        // 调用现有的bindMerchantUser方法
        bindMerchantUser(merchantUserDTO);
        // 返回创建的对象
        return getMerchantUser(merchantUserDTO.getMerchantId(), merchantUserDTO.getPlatformUserId());
    }

    @Override
    public void updateMerchantUser(MerchantUserDTO merchantUserDTO) {
        logger.info("更新商户用户，id:{}", merchantUserDTO.getId());
        MerchantUser merchantUser = BeanCopyUtils.copy(merchantUserDTO, MerchantUser.class);
        merchantUserMapper.updateById(merchantUser);
        // 清除缓存
        String cacheKey = redisKeyBuilder.buildMerchantUserKey(
                merchantUserDTO.getMerchantId(), merchantUserDTO.getPlatformUserId());
        redisUtils.delete(cacheKey);
    }

    @Override
    public void deleteMerchantUser(Long id) {
        logger.info("删除商户用户，id:{}", id);
        merchantUserMapper.deleteById(id);
    }

    @Override
    public int countMerchantUsersByTenantId(Long tenantId) {
        logger.info("统计租户商户用户数量，tenantId:{}", tenantId);
        // TODO: 实现租户级别的统计
        throw new BizException("功能待实现");
    }

    @Override
    public int countMerchantUsersByMerchant(Long tenantId, Long merchantId) {
        logger.info("统计商户用户数量，tenantId:{}, merchantId:{}", tenantId, merchantId);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getMerchantId, merchantId);
        return merchantUserMapper.selectCount(wrapper).intValue();
    }

    @Override
    public int countMerchantsByPlatformUserId(Long platformUserId) {
        logger.info("统计用户的商户数量，platformUserId:{}", platformUserId);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getPlatformUserId, platformUserId);
        return merchantUserMapper.selectCount(wrapper).intValue();
    }
}
