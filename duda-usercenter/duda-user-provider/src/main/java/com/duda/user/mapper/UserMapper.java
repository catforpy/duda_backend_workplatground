package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
