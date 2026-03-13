package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.SecurityConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 安全配置Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface SecurityConfigMapper {

    /**
     * 根据ID查询
     */
    SecurityConfig selectById(@Param("id") Long id);

    /**
     * 根据Bucket名称查询
     */
    SecurityConfig selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 查询启用内容检测的Bucket列表
     */
    List<SecurityConfig> selectByContentDetectionEnabled(@Param("enabled") Boolean enabled);

    /**
     * 查询启用病毒扫描的Bucket列表
     */
    List<SecurityConfig> selectByVirusScanEnabled(@Param("enabled") Boolean enabled);

    /**
     * 查询启用敏感数据扫描的Bucket列表
     */
    List<SecurityConfig> selectBySensitiveDataScanEnabled(@Param("enabled") Boolean enabled);

    /**
     * 插入安全配置
     */
    int insert(SecurityConfig securityConfig);

    /**
     * 更新安全配置
     */
    int update(SecurityConfig securityConfig);

    /**
     * 更新加密配置
     */
    int updateEncryption(@Param("bucketName") String bucketName,
                         @Param("encryptionType") String encryptionType,
                         @Param("enableEncryption") Boolean enableEncryption);

    /**
     * 更新内容检测配置
     */
    int updateContentDetection(@Param("bucketName") String bucketName,
                               @Param("enable") Boolean enable,
                               @Param("threshold") String threshold);

    /**
     * 更新病毒扫描配置
     */
    int updateVirusScan(@Param("bucketName") String bucketName,
                        @Param("enable") Boolean enable,
                        @Param("action") String action);

    /**
     * 更新敏感数据扫描配置
     */
    int updateSensitiveDataScan(@Param("bucketName") String bucketName, @Param("enable") Boolean enable);

    /**
     * 删除安全配置
     */
    int deleteByBucketName(@Param("bucketName") String bucketName);

    /**
     * 查询所有安全配置
     */
    List<SecurityConfig> selectAll();
}
