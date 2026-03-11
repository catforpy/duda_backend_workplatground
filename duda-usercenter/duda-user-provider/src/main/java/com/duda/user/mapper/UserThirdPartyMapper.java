package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.po.UserThirdPartyPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户第三方账号绑定Mapper
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Mapper
public interface UserThirdPartyMapper extends BaseMapper<UserThirdPartyPO> {
}
