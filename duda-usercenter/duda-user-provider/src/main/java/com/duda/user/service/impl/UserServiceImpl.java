package com.duda.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duda.common.domain.PageResult;
import com.duda.common.web.exception.BizException;
import com.duda.common.redis.RedisUtils;
import com.duda.common.util.BeanCopyUtils;
import com.duda.id.api.IdGeneratorRpc;
import com.duda.user.dto.UserDTO;
import com.duda.user.dto.UserLoginReqDTO;
import com.duda.user.dto.UserRegisterReqDTO;
import com.duda.user.entity.UserAccount;
import com.duda.user.mapper.UserAccountMapper;
import com.duda.user.po.UserPO;
import com.duda.user.mapper.UserMapper;
import com.duda.common.redis.key.UserRedisKeyBuilder;
import com.duda.user.service.UserService;
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
 * 用户服务实现
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserRedisKeyBuilder redisKeyBuilder;

    @DubboReference(
        group = "INFRA_GROUP",
        version = "1.0.0",
        registry = "infraRegistry"  // ← 指定使用 infraRegistry
    )
    private IdGeneratorRpc idGeneratorRpc;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterReqDTO registerReq) {
        logger.info("用户注册，用户名:{}", registerReq.getUsername());

        // 1. 检查用户名是否已存在（在 user_accounts 表）
        boolean usernameExists = userAccountMapper.existsByLoginTypeAndAccount("username", registerReq.getUsername());
        if (usernameExists) {
            throw new BizException("用户名已存在");
        }

        // 2. 检查手机号是否已存在（在 user_accounts 表）
        if (StringUtils.hasText(registerReq.getPhone())) {
            boolean phoneExists = userAccountMapper.existsByLoginTypeAndAccount("phone", registerReq.getPhone());
            if (phoneExists) {
                throw new BizException("手机号已被注册");
            }
        }

        // 3. 创建用户（users 表）
        String hashedPassword = BCrypt.hashpw(registerReq.getPassword()); // BCrypt加密
        Long userId = idGeneratorRpc.generateUserId(); // 调用RPC生成雪花ID
        UserPO userPO = new UserPO();
        userPO.setId(userId);
        userPO.setUserType(registerReq.getUserType());
        userPO.setUsername(registerReq.getUsername()); // 兼容旧字段，用于数据迁移兼容
        userPO.setPassword(hashedPassword); // 兼容旧字段，保持数据一致性
        userPO.setRealName(registerReq.getRealName());
        userPO.setStatus("inactive"); // 默认未激活
        userPO.setDeleted(0); // 未删除

        // 4. 创建登录账号（user_accounts 表）
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setUserShard(0); // TODO: 根据userId计算分片
        userAccount.setLoginType("username"); // 目前只支持username注册
        userAccount.setLoginAccount(registerReq.getUsername());
        userAccount.setPassword(hashedPassword); // 使用相同的加密密码
        userAccount.setVerified(true); // 用户名登录默认已验证
        userAccount.setIsPrimary(true); // 第一个登录方式为主账号
        userAccount.setStatus("active");

        // 5. 保存到数据库（先保存users，再保存user_accounts）
        int result = userMapper.insert(userPO);
        if (result <= 0) {
            throw new BizException("注册失败");
        }

        // 6. 保存登录账号
        // 注意：user_accounts 表的 id 是自增的，不需要设置
        userAccountMapper.insert(userAccount);

        logger.info("用户注册成功，userId:{}, username:{}", userPO.getId(), registerReq.getUsername());
        return userPO.getId();
    }

    @Override
    public UserDTO login(UserLoginReqDTO loginReq) {
        logger.info("用户登录，用户名:{}", loginReq.getUsername());

        // 1. 从 user_accounts 表查询登录账号
        UserAccount userAccount = userAccountMapper.selectByLoginTypeAndAccount("username", loginReq.getUsername());

        if (userAccount == null) {
            throw new BizException("用户名或密码错误");
        }

        // 2. 根据 user_id 查询用户信息
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getId, userAccount.getUserId());
        UserPO userPO = userMapper.selectOne(wrapper);

        if (userPO == null) {
            throw new BizException("用户信息不存在");
        }

        // 3. 验证密码（从 user_account 表）
        boolean passwordMatch = BCrypt.checkpw(loginReq.getPassword(), userAccount.getPassword());
        if (!passwordMatch) {
            throw new BizException("用户名或密码错误");
        }
        if (!passwordMatch) {
            throw new BizException("用户名或密码错误");
        }

        // 4. 检查账户状态
        if ("frozen".equals(userPO.getStatus())) {
            throw new BizException("账户已冻结");
        }
        if ("deleted".equals(userPO.getStatus())) {
            throw new BizException("账户已删除");
        }

        // 5. 更新最后登录时间和IP
        userPO.setLastLoginTime(LocalDateTime.now());
        userPO.setLastLoginIp(getClientIp()); // TODO: 从请求中获取真实IP
        userMapper.updateById(userPO);

        // 6. 转换为DTO并返回
        UserDTO userDTO = BeanCopyUtils.copy(userPO, UserDTO.class);

        // 7. 激活状态（首次登录自动激活）
        if ("inactive".equals(userPO.getStatus())) {
            userPO.setStatus("active");
            userMapper.updateById(userPO);
            userDTO.setStatus("active");
        }

        logger.info("用户登录成功，userId:{}, username:{}", userPO.getId(), loginReq.getUsername());
        return userDTO;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        // 1. 先从缓存获取
        String cacheKey = redisKeyBuilder.buildUserInfoKey(userId);
        UserDTO cachedUser = redisUtils.get(cacheKey, UserDTO.class);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 2. 从数据库查询
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new BizException("用户不存在");
        }

        // 3. 转换为DTO
        UserDTO userDTO = BeanCopyUtils.copy(userPO, UserDTO.class);

        // 4. 存入缓存（1小时）
        redisUtils.set(cacheKey, userDTO, 3600);

        return userDTO;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username);
        UserPO userPO = userMapper.selectOne(wrapper);

        if (userPO == null) {
            throw new BizException("用户不存在");
        }

        return BeanCopyUtils.copy(userPO, UserDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(UserDTO userDTO) {
        logger.info("更新用户信息，userId:{}", userDTO.getId());

        // 1. 查询原数据
        UserPO userPO = userMapper.selectById(userDTO.getId());
        if (userPO == null) {
            throw new BizException("用户不存在");
        }

        // 2. 更新字段
        userPO.setRealName(userDTO.getRealName());
        userPO.setPhone(userDTO.getPhone());
        userPO.setEmail(userDTO.getEmail());

        // 3. 更新数据库
        int result = userMapper.updateById(userPO);

        // 4. 清除缓存
        String cacheKey = redisKeyBuilder.buildUserInfoKey(userDTO.getId());
        redisUtils.delete(cacheKey);

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUser(Long userId) {
        logger.info("删除用户，userId:{}", userId);

        // 1. 软删除
        UserPO userPO = new UserPO();
        userPO.setId(userId);
        userPO.setDeleted(1);

        int result = userMapper.updateById(userPO);

        // 2. 清除缓存
        String cacheKey = redisKeyBuilder.buildUserInfoKey(userId);
        redisUtils.delete(cacheKey);

        return result > 0;
    }

    @Override
    public PageResult pageUsers(String userType, String status, String keyword, Integer pageNum, Integer pageSize) {
        // 1. 构建查询条件
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(userType)) {
            wrapper.eq(UserPO::getUserType, userType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(UserPO::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UserPO::getUsername, keyword)
                    .or().like(UserPO::getRealName, keyword)
                    .or().like(UserPO::getPhone, keyword));
        }

        // 2. 按创建时间倒序
        wrapper.orderByDesc(UserPO::getCreateTime);

        // 3. 分页查询
        Page<UserPO> page = new Page<>(pageNum, pageSize);
        IPage<UserPO> pageResult = userMapper.selectPage(page, wrapper);

        // 4. 转换并返回
        List<UserDTO> records = BeanCopyUtils.copyList(pageResult.getRecords(), UserDTO.class);
        return PageResult.of(records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    /**
     * 获取客户端IP（TODO: 从HttpServletRequest中获取）
     */
    private String getClientIp() {
        return "127.0.0.1"; // 暂时返回本地IP
    }
}
