package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.UploadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 上传记录Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface UploadRecordMapper {

    /**
     * 根据ID查询
     */
    UploadRecord selectById(@Param("id") Long id);

    /**
     * 根据上传ID查询
     */
    UploadRecord selectByUploadId(@Param("uploadId") String uploadId);

    /**
     * 根据用户ID查询上传记录
     */
    List<UploadRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和分片编号查询上传记录
     */
    List<UploadRecord> selectByUserIdAndShard(@Param("userId") Long userId, @Param("userShard") Integer userShard);

    /**
     * 根据Bucket和对象键查询上传记录
     */
    List<UploadRecord> selectByBucketAndKey(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 根据上传状态查询记录
     */
    List<UploadRecord> selectByStatus(@Param("uploadStatus") String uploadStatus);

    /**
     * 插入上传记录
     */
    int insert(UploadRecord uploadRecord);

    /**
     * 更新上传记录
     */
    int update(UploadRecord uploadRecord);

    /**
     * 更新上传状态
     */
    int updateStatus(@Param("id") Long id, @Param("uploadStatus") String uploadStatus);

    /**
     * 更新上传进度
     */
    int updateProgress(@Param("id") Long id, @Param("uploadedParts") Integer uploadedParts);

    /**
     * 删除上传记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询最近的上传记录
     */
    List<UploadRecord> selectRecent(@Param("limit") Integer limit);
}
