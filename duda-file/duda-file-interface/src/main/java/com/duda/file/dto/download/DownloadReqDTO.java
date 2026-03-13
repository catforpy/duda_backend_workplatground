package com.duda.file.dto.download;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 下载请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadReqDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 版本ID(开启版本控制时)
     */
    private String versionId;

    /**
     * 下载范围
     * start: 起始字节位置
     * end: 结束字节位置
     */
    private Range range;

    /**
     * 是否启用断点续传
     */
    private Boolean resumable;

    /**
     * 分片大小(断点续传时使用)
     * 不设置则自动计算
     */
    private Long partSize;

    /**
     * 并发下载线程数(断点续传时使用)
     * 默认: 3
     */
    private Integer concurrentThreads;

    /**
     * 是否启用CRC64校验
     */
    private Boolean enableCRC64;

    /**
     * 是否使用预签名URL
     * 如果为true,则返回URL而非流
     */
    private Boolean usePresignedUrl;

    /**
     * 预签名URL有效期(秒)
     * 默认: 3600 (1小时)
     * 范围: 1-604800 (1秒-7天)
     */
    private Integer urlExpiration;

    /**
     * 响应头限制
     * 只有包含这些响应头的请求才会成功
     */
    private Map<String, String> responseHeaders;

    /**
     * 进度监听器
     */
    private ProgressListener progressListener;

    /**
     * 用户ID(用于权限验证)
     */
    private Long userId;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

    /**
     * 下载范围
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Range {
        /**
         * 起始位置(包含)
         */
        private Long start;

        /**
         * 结束位置(包含,如果为null则表示到文件末尾)
         */
        private Long end;

        /**
         * 转换为HTTP Range请求头格式
         */
        public String toHttpRange() {
            if (start == null) {
                return null;
            }
            if (end == null) {
                return "bytes=" + start + "-";
            }
            return "bytes=" + start + "-" + end;
        }

        /**
         * 获取范围大小
         */
        public long getSize() {
            if (start == null) {
                return 0;
            }
            if (end == null) {
                return -1; // 未知大小
            }
            return end - start + 1;
        }
    }

    /**
     * 进度监听器接口
     */
    public interface ProgressListener {
        /**
         * 下载进度变更
         *
         * @param bytesAlreadyRead 已读取字节数
         * @param totalBytes       总字节数
         * @param stillReadBytes   剩余字节数
         */
        void progressChanged(long bytesAlreadyRead, long totalBytes, long stillReadBytes);

        /**
         * 下载成功
         */
        void onSucceed();

        /**
         * 下载失败
         *
         * @param exception 异常信息
         */
        void onFailed(Exception exception);
    }

    /**
     * 构建默认请求
     */
    public static DownloadReqDTO buildDefault(String bucketName, String objectKey, Long userId) {
        return DownloadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .resumable(false)
                .enableCRC64(true)
                .build();
    }

    /**
     * 构建范围下载请求
     */
    public static DownloadReqDTO buildRange(String bucketName, String objectKey,
                                            Long start, Long end, Long userId) {
        return DownloadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .range(Range.builder().start(start).end(end).build())
                .enableCRC64(true)
                .build();
    }

    /**
     * 构建断点续传请求
     */
    public static DownloadReqDTO buildResumable(String bucketName, String objectKey,
                                                Long userId, Integer concurrentThreads) {
        return DownloadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .resumable(true)
                .concurrentThreads(concurrentThreads != null ? concurrentThreads : 3)
                .enableCRC64(true)
                .build();
    }

    /**
     * 构建预签名URL请求
     */
    public static DownloadReqDTO buildPresignedUrl(String bucketName, String objectKey,
                                                    Long userId, int expiration) {
        return DownloadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .usePresignedUrl(true)
                .urlExpiration(expiration)
                .build();
    }

    /**
     * 验证请求参数
     */
    public boolean validate() {
        if (bucketName == null || bucketName.isEmpty()) {
            return false;
        }
        if (objectKey == null || objectKey.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为范围下载
     */
    public boolean isRangeDownload() {
        return range != null && range.getStart() != null;
    }
}
