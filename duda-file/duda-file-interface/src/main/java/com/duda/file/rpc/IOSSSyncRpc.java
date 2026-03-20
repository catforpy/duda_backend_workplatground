package com.duda.file.rpc;

/**
 * OSS元数据同步RPC接口
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
public interface IOSSSyncRpc {

    /**
     * 同步指定Bucket的对象元数据到数据库
     *
     * @param bucketName Bucket名称
     * @return 同步的对象数量
     */
    int syncBucketObjects(String bucketName);

    /**
     * 同步所有Bucket的对象元数据到数据库
     *
     * @return 同步的对象总数
     */
    int syncAllBuckets();

    /**
     * 同步指定Bucket的指定前缀的对象
     *
     * @param bucketName Bucket名称
     * @param prefix 对象前缀
     * @return 同步的对象数量
     */
    int syncBucketObjectsByPrefix(String bucketName, String prefix);
}
