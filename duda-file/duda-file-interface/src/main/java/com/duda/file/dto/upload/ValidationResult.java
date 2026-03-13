package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * 是否验证通过
     */
    @Builder.Default
    private Boolean valid = true;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误详情列表
     */
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    /**
     * 警告信息列表
     */
    @Builder.Default
    private List<ValidationWarning> warnings = new ArrayList<>();

    /**
     * 扩展信息
     */
    private java.util.Map<String, Object> extra;

    /**
     * 添加错误
     */
    public void addError(String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .build());
        this.valid = false;
    }

    /**
     * 添加警告
     */
    public void addWarning(String field, String message) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(ValidationWarning.builder()
                .field(field)
                .message(message)
                .build());
    }

    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * 获取错误数量
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * 获取警告数量
     */
    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }

    /**
     * 构建成功结果
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }

    /**
     * 构建失败结果
     */
    public static ValidationResult failure(String errorCode, String errorMessage) {
        return ValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 构建失败结果(带错误详情)
     */
    public static ValidationResult failure(String errorCode, String errorMessage, List<ValidationError> errors) {
        return ValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .errors(errors)
                .build();
    }

    /**
     * 验证错误
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * 错误字段
         */
        private String field;

        /**
         * 错误代码
         */
        private String code;

        /**
         * 错误信息
         */
        private String message;

        /**
         * 错误值
         */
        private Object value;
    }

    /**
     * 验证警告
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationWarning {
        /**
         * 警告字段
         */
        private String field;

        /**
         * 警告代码
         */
        private String code;

        /**
         * 警告信息
         */
        private String message;

        /**
         * 警告值
         */
        private Object value;
    }
}
