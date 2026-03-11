package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.po.UserProfilePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户扩展资料Mapper
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfilePO> {
}
