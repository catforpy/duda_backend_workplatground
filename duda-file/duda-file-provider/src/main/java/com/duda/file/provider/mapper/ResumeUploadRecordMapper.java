package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.ResumeUploadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 断点续传记录Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface ResumeUploadRecordMapper {

    /**
     * 根据ID查询
     */
    ResumeUploadRecord selectById(@Param("id") Long id);

    /**
     * 根据记录ID查询
     */
    ResumeUploadRecord selectByRecordId(@Param("recordId") String recordId);

    /**
     * 根据用户ID查询断点续传记录
     */
    List<ResumeUploadRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和分片编号查询记录
     */
    List<ResumeUploadRecord> selectByUserIdAndShard(@Param("userId") Long userId, @Param("userShard") Integer userShard);

    /**
     * 根据状态查询记录
     */
    List<ResumeUploadRecord> selectByStatus(@Param("uploadStatus") String uploadStatus);

    /**
     * 查询指定时间之前未完成的记录
     */
    List<ResumeUploadRecord> selectExpired(@Param("expireTime") String expireTime);

    /**
     * 插入断点续传记录
     */
    int insert(ResumeUploadRecord resumeUploadRecord);

    /**
     * 更新断点续传记录
     */
    int update(ResumeUploadRecord resumeUploadRecord);

    /**
     * 更新上传状态
     */
    int updateStatus(@Param("recordId") String recordId, @Param("uploadStatus") String uploadStatus);

    /**
     * 更新上传进度
     */
    int updateProgress(@Param("recordId") String recordId,
                       @Param("uploadedParts") String uploadedParts,
                       @Param("lastUpdateTime") String lastUpdateTime);

    /**
     * 删除断点续传记录
     */
    int deleteByRecordId(@Param("recordId") String recordId);

    /**
     * 批量删除过期的记录
     */
    int batchDeleteExpired(@Param("expireTime") String expireTime);
}
