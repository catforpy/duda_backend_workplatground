package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 上传结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 对象ETag
     * 用于验证数据完整性
     */
    private String eTag;

    /**
     * 版本ID(开启版本控制时)
     */
    private String versionId;

    /**
     * 上传后的对象URL
     */
    private String objectUrl;

    /**
     * 上传后的文件完整路径
     */
    private String fullPath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 上传方式
     * - simple: 简单上传
     * - multipart: 分片上传
     * - append: 追加上传
     * - form: 表单上传
     * - sts: STS临时凭证上传
     * - presigned: 预签名URL上传
     */
    private String uploadMethod;

    /**
     * 分片上传信息(如果是分片上传)
     */
    private MultipartUploadInfo multipartInfo;

    /**
     * CRC64校验值
     */
    private Long crc64;

    /**
     * 服务端加密算法
     */
    private String serverSideEncryption;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 响应头
     */
    private Map<String, String> responseHeaders;

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
     * 分片上传信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultipartUploadInfo {
        /**
         * 上传ID
         */
        private String uploadId;

        /**
         * 分片总数
         */
        private Integer partCount;

        /**
         * 分片大小
         */
        private Long partSize;

        /**
         * 已上传分片数
         */
        private Integer uploadedPartCount;

        /**
         * 分片ETag列表
         * key: partNumber
         * value: eTag
         */
        private Map<Integer, String> partETags;
    }

    /**
     * 判断上传是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * 获取文件的完整路径
     */
    public String getFullPath() {
        if (fullPath != null) {
            return fullPath;
        }
        return bucketName + "/" + objectKey;
    }
}
