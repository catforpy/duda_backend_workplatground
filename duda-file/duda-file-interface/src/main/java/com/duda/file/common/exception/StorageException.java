package com.duda.file.common.exception;

/**
 * 存储异常基类
 * 所有存储相关的异常都应继承此类
 *
 * @author duda
 * @date 2025-03-13
 */
public class StorageException extends RuntimeException {

    private String errorCode;
    private String errorMessage;

    public StorageException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public StorageException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    public StorageException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
