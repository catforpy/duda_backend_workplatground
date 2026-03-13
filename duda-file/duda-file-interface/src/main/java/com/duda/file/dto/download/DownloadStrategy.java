package com.duda.file.dto.download;

/**
 * 下载策略枚举
 *
 * @author duda
 * @date 2025-03-13
 */
public enum DownloadStrategy {

    /**
     * 简单下载
     * 适用于小文件(<100MB)
     * 优势: 实现简单,一次性下载
     * 劣势: 大文件下载失败需要重新下载
     */
    SIMPLE_DOWNLOAD("simple", "简单下载", 100 * 1024 * 1024L),

    /**
     * 断点续传下载
     * 适用于大文件(>=100MB)
     * 优势: 支持断点续传,并发下载
     * 劣势: 实现复杂,需要管理分片状态
     */
    RESUMABLE_DOWNLOAD("resumable", "断点续传下载", Long.MAX_VALUE),

    /**
     * 预签名URL下载
     * 适用于临时分享下载
     * 优势: 临时URL,无需暴露密钥
     * 劣势: URL有有效期限制
     */
    PRESIGNED_DOWNLOAD("presigned", "预签名URL下载", Long.MAX_VALUE),

    /**
     * 范围下载
     * 适用于分片下载或部分下载
     * 优势: 可以下载文件的指定部分
     * 劣势: 需要知道文件大小
     */
    RANGE_DOWNLOAD("range", "范围下载", Long.MAX_VALUE);

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

    DownloadStrategy(String code, String name, Long maxFileSize) {
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
     * 根据文件大小选择合适的下载策略
     *
     * @param fileSize 文件大小
     * @return 推荐的下载策略
     */
    public static DownloadStrategy selectStrategy(long fileSize) {
        // 小文件(<100MB): 使用简单下载
        if (fileSize < 100 * 1024 * 1024) {
            return SIMPLE_DOWNLOAD;
        }
        // 大文件(>=100MB): 使用断点续传
        else {
            return RESUMABLE_DOWNLOAD;
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
     * @return 下载策略
     */
    public static DownloadStrategy fromCode(String code) {
        for (DownloadStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown download strategy: " + code);
    }
}
