package com.duda.file.provider.service;

import com.alibaba.fastjson2.JSON;
import com.duda.file.provider.entity.OssOperationLog;
import com.duda.file.provider.mapper.OssOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * OSS操作日志服务
 *
 * 功能：
 * 1. 记录所有OSS操作（上传、下载、删除、配置修改等）
 * 2. 提供操作历史查询
 * 3. 统计操作数据
 *
 * @author duda
 * @date 2025-03-14
 */
@Slf4j
@Service
public class OssOperationLogService {

    @Autowired
    private OssOperationLogMapper operationLogMapper;

    /**
     * 记录操作日志
     *
     * @param log 操作日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void logOperation(OssOperationLog log) {
        try {
            // 设置创建时间
            if (log.getCreatedTime() == null) {
                log.setCreatedTime(LocalDateTime.now());
            }

            // 计算耗时
            if (log.getEndTime() != null && log.getStartTime() != null) {
                long duration = java.time.Duration.between(log.getStartTime(), log.getEndTime()).toMillis();
                log.setDurationMs(duration);
            }

            // 保存到数据库
            operationLogMapper.insert(log);

            System.out.println("操作日志已记录: " + log.getOperationType() + " - " + log.getOperationDesc());

        } catch (Exception e) {
            System.err.println("记录操作日志失败: " + e.getMessage());
            // 不抛出异常，避免影响主业务
        }
    }

    /**
     * 记录文件上传操作
     */
    public void logUpload(String bucketName, String objectKey, Long fileSize,
                         String fileType, String etag, String status, String errorMsg) {
        OssOperationLog log = OssOperationLog.builder()
            .bucketName(bucketName)
            .operationType("upload")
            .operationCategory("file")
            .objectKey(objectKey)
            .operationDesc("上传文件: " + objectKey)
            .fileSize(fileSize)
            .fileType(fileType)
            .fileEtag(etag)
            .status(status)
            .errorMessage(errorMsg)
            .operatorType("SYSTEM")
            .operatorName("TestSystem")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now())
            .build();

        logOperation(log);
    }

    /**
     * 记录文件下载操作
     */
    public void logDownload(String bucketName, String objectKey, Long fileSize,
                           String status, String errorMsg) {
        OssOperationLog log = OssOperationLog.builder()
            .bucketName(bucketName)
            .operationType("download")
            .operationCategory("file")
            .objectKey(objectKey)
            .operationDesc("下载文件: " + objectKey)
            .fileSize(fileSize)
            .status(status)
            .errorMessage(errorMsg)
            .operatorType("SYSTEM")
            .operatorName("TestSystem")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now())
            .build();

        logOperation(log);
    }

    /**
     * 记录文件删除操作
     */
    public void logDelete(String bucketName, String objectKey, String status, String errorMsg) {
        OssOperationLog log = OssOperationLog.builder()
            .bucketName(bucketName)
            .operationType("delete")
            .operationCategory("file")
            .objectKey(objectKey)
            .operationDesc("删除文件: " + objectKey)
            .status(status)
            .errorMessage(errorMsg)
            .operatorType("SYSTEM")
            .operatorName("TestSystem")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now())
            .build();

        logOperation(log);
    }

    /**
     * 记录配置修改操作
     */
    public void logConfigChange(String bucketName, String configField,
                              String oldValue, String newValue, String status) {
        OssOperationLog log = OssOperationLog.builder()
            .bucketName(bucketName)
            .operationType("config_change")
            .operationCategory("config")
            .configField(configField)
            .oldValue(oldValue)
            .newValue(newValue)
            .operationDesc("修改配置: " + configField)
            .status(status)
            .operatorType("SYSTEM")
            .operatorName("TestSystem")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now())
            .build();

        logOperation(log);
    }

    /**
     * 记录查询操作
     */
    public void logQuery(String bucketName, String objectKey, String operationDesc,
                       int resultCount) {
        Map<String, Object> response = Map.of(
            "resultCount", resultCount,
            "queryTime", LocalDateTime.now().toString()
        );

        OssOperationLog log = OssOperationLog.builder()
            .bucketName(bucketName)
            .operationType("query")
            .operationCategory("file")
            .objectKey(objectKey)
            .operationDesc(operationDesc)
            .responseData(JSON.toJSONString(response))
            .status("SUCCESS")
            .operatorType("SYSTEM")
            .operatorName("TestSystem")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now())
            .build();

        logOperation(log);
    }
}
