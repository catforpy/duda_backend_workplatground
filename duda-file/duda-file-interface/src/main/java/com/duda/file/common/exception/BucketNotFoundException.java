package com.duda.file.common.exception;

/**
 * Bucket不存在异常
 *
 * @author duda
 * @date 2025-03-13
 */
public class BucketNotFoundException extends StorageException {

    private String bucketName;

    public BucketNotFoundException(String bucketName) {
        super("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
        this.bucketName = bucketName;
    }

    public BucketNotFoundException(String bucketName, Throwable cause) {
        super("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName, cause);
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
