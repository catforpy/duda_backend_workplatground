package com.duda.file.common.exception;

/**
 * 上传失败异常
 *
 * @author duda
 * @date 2025-03-13
 */
public class UploadFailedException extends StorageException {

    private String bucketName;
    private String objectKey;
    private Long fileSize;

    public UploadFailedException(String bucketName, String objectKey, String message) {
        super("UPLOAD_FAILED", message);
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }

    public UploadFailedException(String bucketName, String objectKey, Long fileSize, String message) {
        super("UPLOAD_FAILED", message);
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.fileSize = fileSize;
    }

    public UploadFailedException(String bucketName, String objectKey, String message, Throwable cause) {
        super("UPLOAD_FAILED", message, cause);
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public Long getFileSize() {
        return fileSize;
    }
}
