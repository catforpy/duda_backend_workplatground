package com.duda.file.dto.upload;

/**
 * 上传策略枚举
 *
 * @author duda
 * @date 2025-03-13
 */
public enum UploadStrategy {

    /**
     * 简单上传
     * 适用于小文件(<100MB)
     * 优势: 实现简单,一次性上传
     * 劣势: 大文件上传失败需要重新上传
     */
    SIMPLE_UPLOAD("simple", "简单上传", 100 * 1024 * 1024L),

    /**
     * 分片上传
     * 适用于大文件(>=100MB)
     * 优势: 支持断点续传,并发上传
     * 劣势: 实现复杂,需要管理分片状态
     */
    MULTIPART_UPLOAD("multipart", "分片上传", Long.MAX_VALUE),

    /**
     * 追加上传
     * 适用于日志文件等追加场景
     * 优势: 可以多次追加
     * 劣势: 只能追加,不能修改已有内容
     */
    APPEND_UPLOAD("append", "追加上传", Long.MAX_VALUE),

    /**
     * 表单上传
     * 适用于Web端上传
     * 优势: 直接从浏览器上传到OSS
     * 劣势: 需要生成签名表单
     */
    FORM_UPLOAD("form", "表单上传", 5 * 1024 * 1024 * 1024L),

    /**
     * STS临时凭证上传
     * 适用于移动端上传
     * 优势: 临时凭证,安全可控
     * 劣势: 需要STS服务支持
     */
    STS_UPLOAD("sts", "STS凭证上传", Long.MAX_VALUE),

    /**
     * 预签名URL上传
     * 适用于临时分享上传
     * 优势: 临时URL,无需暴露密钥
     * 劣势: URL有有效期限制
     */
    PRESIGNED_UPLOAD("presigned", "预签名URL上传", Long.MAX_VALUE);

    /**
     * 策略代码
     */
    private final String code;

    /**
     * 策略名称
     */
    private final String name;

    /**
     * 最大文件大小(字节)
     */
    private final Long maxFileSize;

    UploadStrategy(String code, String name, Long maxFileSize) {
        this.code = code;
        this.name = name;
        this.maxFileSize = maxFileSize;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * 根据文件大小选择合适的上传策略
     *
     * @param fileSize 文件大小
     * @return 推荐的上传策略
     */
    public static UploadStrategy selectStrategy(long fileSize) {
        // 小文件(<100MB): 使用简单上传
        if (fileSize < 100 * 1024 * 1024) {
            return SIMPLE_UPLOAD;
        }
        // 大文件(>=100MB): 使用分片上传
        else {
            return MULTIPART_UPLOAD;
        }
    }

    /**
     * 判断文件大小是否适用该策略
     *
     * @param fileSize 文件大小
     * @return 是否适用
     */
    public boolean isApplicable(long fileSize) {
        return fileSize <= maxFileSize;
    }

    /**
     * 根据code获取策略
     *
     * @param code 策略代码
     * @return 上传策略
     */
    public static UploadStrategy fromCode(String code) {
        for (UploadStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown upload strategy: " + code);
    }
}
