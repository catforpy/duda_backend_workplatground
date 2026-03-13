package com.duda.file.dto.download;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 下载结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadResultDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 对象输入流
     */
    private InputStream inputStream;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 对象ETag
     */
    private String eTag;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 内容编码
     */
    private String contentEncoding;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModified;

    /**
     * 下载时间
     */
    private LocalDateTime downloadTime;

    /**
     * 下载方式
     * - simple: 简单下载
     * - resumable: 断点续传下载
     * - presigned: 预签名URL下载
     */
    private String downloadMethod;

    /**
     * 断点续传信息(如果是断点续传)
     */
    private ResumableDownloadInfo resumableInfo;

    /**
     * CRC64校验值
     */
    private Long crc64;

    /**
     * Content-MD5
     */
    private String contentMD5;

    /**
     * 对象元数据
     */
    private Map<String, String> metadata;

    /**
     * 响应头
     */
    private Map<String, String> responseHeaders;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 对象URL(如果是预签名URL下载)
     */
    private String objectUrl;

    /**
     * URL过期时间(如果是预签名URL下载)
     */
    private LocalDateTime expirationTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误码(失败时)
     */
    private String errorCode;

    /**
     * 错误信息(失败时)
     */
    private String errorMessage;

    /**
     * 扩展信息
     */
    private Map<String, Object> extra;

    /**
     * 断点续传信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumableDownloadInfo {
        /**
         * 下载范围
         * start: 起始字节位置
         * end: 结束字节位置
         */
        private Range range;

        /**
         * 总文件大小
         */
        private Long totalSize;

        /**
         * 已下载大小
         */
        private Long downloadedSize;

        /**
         * 下载进度(0-100)
         */
        private Double progress;

        /**
         * 分片下载信息
         */
        private PartDownloadInfo partInfo;

        /**
         * 字节范围
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
             * 结束位置(包含)
             */
            private Long end;

            /**
             * 范围大小
             */
            public long getSize() {
                if (start == null || end == null) {
                    return 0;
                }
                return end - start + 1;
            }

            /**
             * 转换为HTTP Range请求头格式
             */
            public String toHttpRange() {
                return "bytes=" + start + "-" + end;
            }
        }

        /**
         * 分片下载信息
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PartDownloadInfo {
            /**
             * 分片大小
             */
            private Long partSize;

            /**
             * 分片总数
             */
            private Integer totalPartCount;

            /**
             * 已下载分片数
             */
            private Integer downloadedPartCount;

            /**
             * 并发下载线程数
             */
            private Integer concurrentThreads;
        }
    }

    /**
     * 判断下载是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * 判断是否为断点续传
     */
    public boolean isResumable() {
        return resumableInfo != null;
    }

    /**
     * 判断是否为预签名URL下载
     */
    public boolean isPresignedUrl() {
        return objectUrl != null && !objectUrl.isEmpty();
    }

    /**
     * 获取下载进度百分比
     */
    public String getProgressText() {
        if (resumableInfo == null || resumableInfo.getProgress() == null) {
            return "100%";
        }
        return String.format("%.2f%%", resumableInfo.getProgress());
    }
}
