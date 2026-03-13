package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDeleteResultDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 请求删除的对象总数
     */
    private Integer totalRequested;

    /**
     * 删除成功的对象数量
     */
    private Integer successCount;

    /**
     * 删除失败的对象数量
     */
    private Integer failureCount;

    /**
     * 删除成功的对象键列表
     */
    private List<String> deletedKeys;

    /**
     * 删除失败的对象键列表
     */
    private List<String> failedKeys;

    /**
     * 删除失败的详细信息
     * key: objectKey
     * value: 错误信息
     */
    private java.util.Map<String, String> errorMessages;

    /**
     * 是否全部成功
     */
    private Boolean allSuccess;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 编码类型
     */
    private String encodingType;

    /**
     * 判断是否全部成功
     */
    public boolean isAllSuccess() {
        if (allSuccess != null) {
            return allSuccess;
        }
        return failureCount == null || failureCount == 0;
    }

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (totalRequested == null || totalRequested == 0) {
            return 0.0;
        }
        int success = successCount != null ? successCount : 0;
        return (double) success / totalRequested * 100;
    }
}
