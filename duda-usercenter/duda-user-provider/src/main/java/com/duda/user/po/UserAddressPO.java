package com.duda.user.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户地址PO
 *
 * 存储用户的收货地址
 * 与users表是一对多关系
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@TableName("user_addresses")
public class UserAddressPO {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联users表）
     */
    private Long userId;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 省份代码
     */
    private String provinceCode;

    /**
     * 城市
     */
    private String city;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 区县
     */
    private String district;

    /**
     * 区县代码
     */
    private String districtCode;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 地址标签：home-家, company-公司, school-学校
     */
    private String tag;

    /**
     * 是否默认地址：1-是, 0-否
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
