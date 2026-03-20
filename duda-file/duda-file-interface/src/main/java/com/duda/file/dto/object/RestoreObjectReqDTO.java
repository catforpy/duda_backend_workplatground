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
 * 恢复归档对象请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreObjectReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 恢复天数
     * 对象恢复后的可读保留天数
     * 范围: 1-7天
     */
    private Integer days;

    /**
     * 恢复类型
     * - Standard: 标准恢复(1-5分钟,适用于Archive)
     * - Expedited: 快速恢复(实时,适用于Cold Archive)
     * - Bulk: 批量恢复(5-12小时,适用于Cold Archive)
     */
    private String tier;

    /**
     * 恢复类型枚举
     */
    public enum RestoreTier {
        /**
         * 标准恢复
         */
        Standard("Standard"),

        /**
         * 快速恢复
         */
        Expedited("Expedited"),

        /**
         * 批量恢复
         */
        Bulk("Bulk");

        private final String code;

        RestoreTier(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * 构建标准恢复请求
     */
    public static RestoreObjectReqDTO buildStandard(String bucketName, String objectKey, int days) {
        return RestoreObjectReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .days(days)
                .tier(RestoreTier.Standard.getCode())
                .build();
    }

    /**
     * 构建快速恢复请求
     */
    public static RestoreObjectReqDTO buildExpedited(String bucketName, String objectKey, int days) {
        return RestoreObjectReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .days(days)
                .tier(RestoreTier.Expedited.getCode())
                .build();
    }
}
