package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 设置对象标签结果DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetObjectTaggingResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 标签数量
     */
    private Integer tagCount;
}
