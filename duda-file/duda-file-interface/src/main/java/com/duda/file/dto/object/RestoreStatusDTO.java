package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对象恢复状态DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreStatusDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 恢复状态
     * - IN_PROGRESS: 恢复中
     * - COMPLETED: 恢复完成
     * - FAILED: 恢复失败
     * - EXPIRED: 恢复已过期
     */
    private String restoreStatus;

    /**
     * 恢复状态枚举
     */
    public enum RestoreStatus {
        /**
         * 恢复中
         */
        IN_PROGRESS("IN_PROGRESS", "恢复中"),

        /**
         * 恢复完成
         */
        COMPLETED("COMPLETED", "恢复完成"),

        /**
         * 恢复失败
         */
        FAILED("FAILED", "恢复失败"),

        /**
         * 恢复已过期
         */
        EXPIRED("EXPIRED", "恢复已过期");

        private final String code;
        private final String description;

        RestoreStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 是否正在恢复
     */
    private Boolean inProgress;

    /**
     * 恢复完成时间
     */
    private LocalDateTime restoreTime;

    /**
     * 恢复过期时间
     * 过期后对象将不可读
     */
    private LocalDateTime expiryTime;

    /**
     * 恢复天数
     */
    private Integer days;

    /**
     * 恢复类型
     */
    private String tier;

    /**
     * 错误信息(恢复失败时)
     */
    private String errorMessage;

    /**
     * 是否可读
     */
    private Boolean readable;

    /**
     * 判断对象是否可读
     */
    public boolean isReadable() {
        if (readable != null) {
            return readable;
        }
        return RestoreStatus.COMPLETED.getCode().equals(restoreStatus);
    }

    /**
     * 判断恢复是否正在进行
     */
    public boolean isInProgress() {
        if (inProgress != null) {
            return inProgress;
        }
        return RestoreStatus.IN_PROGRESS.getCode().equals(restoreStatus);
    }

    /**
     * 判断恢复是否已过期
     */
    public boolean isExpired() {
        if (expiryTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
