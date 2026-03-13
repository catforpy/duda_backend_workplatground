package com.duda.file.common.exception;

/**
 * Object不存在异常
 *
 * @author duda
 * @date 2025-03-13
 */
public class ObjectNotFoundException extends StorageException {

    private String bucketName;
    private String objectKey;

    public ObjectNotFoundException(String bucketName, String objectKey) {
        super("OBJECT_NOT_FOUND", "Object not found: " + bucketName + "/" + objectKey);
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }

    public ObjectNotFoundException(String bucketName, String objectKey, Throwable cause) {
        super("OBJECT_NOT_FOUND", "Object not found: " + bucketName + "/" + objectKey, cause);
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
