package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.po.UserAddressPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户地址Mapper
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressPO> {
}
