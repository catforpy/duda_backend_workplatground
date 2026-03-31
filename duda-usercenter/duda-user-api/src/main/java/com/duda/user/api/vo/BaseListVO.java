package com.duda.user.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 列表项VO基类
 *
 * 用于列表页面展示的简化数据结构
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
public class BaseListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 状态
     */
    private String status;
}
