package com.duda.file.api.controller;

import com.duda.common.domain.Result;
import com.duda.file.dto.bucket.*;
import com.duda.file.service.BucketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bucket 管理控制器
 * 提供存储空间(Bucket)的完整管理功能
 *
 * 功能模块：
 * 1. 基础操作 - 创建、删除、查询 Bucket
 * 2. 权限管理 - ACL 设置和查询
 * 3. 配置管理 - 存储类型、地域、配额等
 * 4. 统计信息 - 存储容量、文件数量等
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Tag(name = "Bucket管理", description = "存储空间(Bucket)管理接口")
@RestController
@RequestMapping("/api/bucket")
@CrossOrigin(originPatterns = "*", maxAge = 3600)
public class BucketController {

    @DubboReference(
        version = "1.0.0",
        group = "DUDA_FILE_GROUP",
        check = false
    )
    private BucketService bucketService;

    // ==================== 基础操作 ====================

    /**
     * 创建存储空间
     *
     * 创建一个新的存储空间(Bucket)，用于存储文件和数据。
     *
     * 功能说明：
     * - 可以指定 Bucket 名称或自动生成
     * - 支持多种存储类型（标准、低频、归档等）
     * - 支持多地域部署
     * - 支持自定义 ACL 权限
     *
     * @param request 创建请求参数
     * @return Bucket 信息
     */
    @Operation(summary = "创建存储空间", description = "创建一个新的 Bucket 用于存储文件")
    @PostMapping("/create")
    public Result createBucket(
        @Parameter(description = "创建请求参数", required = true)
        @Valid @RequestBody CreateBucketReqDTO request) {

        // 调用服务创建 Bucket
        BucketDTO bucket = bucketService.createBucket(request);

        return Result.success(bucket);
    }

    /**
     * 删除存储空间
     *
     * 删除指定的 Bucket 及其中的所有数据。
     * ⚠️ 警告：删除操作不可逆，请谨慎操作！
     *
     * 安全措施：
     * - 验证用户权限
     * - 检查 Bucket 是否为空
     * - 可选：强制删除标记
     *
     * @param bucketName Bucket 名称
     * @param userId 用户 ID（从认证信息获取）
     * @param force 是否强制删除（即使不为空）
     * @return 删除结果
     */
    @Operation(summary = "删除存储空间", description = "删除指定的 Bucket 及其所有数据")
    @DeleteMapping("/{bucketName}")
    public Result deleteBucket(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName,
        @Parameter(description = "用户ID", required = true)
        @RequestParam Long userId,
        @Parameter(description = "是否强制删除")
        @RequestParam(defaultValue = "false") Boolean force) {

        // TODO: 实现强制删除逻辑
        bucketService.deleteBucket(bucketName, userId);

        return Result.success(null, "Bucket 删除成功");
    }

    /**
     * 获取存储空间信息
     *
     * 查询指定 Bucket 的详细信息。
     *
     * @param bucketName Bucket 名称
     * @return Bucket 信息
     */
    @Operation(summary = "获取存储空间信息", description = "查询 Bucket 的详细信息")
    @GetMapping("/{bucketName}")
    public Result getBucketInfo(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName) {

        BucketDTO bucket = bucketService.getBucketInfo(bucketName);

        return Result.success(bucket);
    }

    /**
     * 列出用户的存储空间
     *
     * 查询指定用户的所有 Bucket 列表。
     *
     * @param userId 用户 ID
     * @param includeDeleted 是否包含已删除的 Bucket
     * @return Bucket 列表
     */
    @Operation(summary = "列出存储空间", description = "查询用户的所有 Bucket")
    @GetMapping("/list")
    public Result listBuckets(
        @Parameter(description = "用户ID", required = true)
        @RequestParam Long userId,
        @Parameter(description = "是否包含已删除的Bucket")
        @RequestParam(defaultValue = "false") Boolean includeDeleted) {

        List<BucketDTO> buckets = bucketService.listBuckets(userId);

        return Result.success(buckets);
    }

    /**
     * 检查存储空间是否存在
     *
     * @param bucketName Bucket 名称
     * @return 是否存在
     */
    @Operation(summary = "检查存储空间是否存在", description = "检查指定 Bucket 是否存在")
    @GetMapping("/{bucketName}/exists")
    public Result doesBucketExist(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName) {

        Boolean exists = bucketService.doesBucketExist(bucketName);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return Result.success(response);
    }

    // ==================== 权限管理 ====================

    /**
     * 设置存储空间 ACL
     *
     * 设置 Bucket 的访问控制列表(ACL)。
     *
     * 支持的 ACL 类型：
     * - PRIVATE: 私有读写
     * - PUBLIC_READ: 公共读，私有写
     * - PUBLIC_READ_WRITE: 公共读写
     *
     * @param bucketName Bucket 名称
     * @param aclType ACL 类型
     * @return 设置结果
     */
    @Operation(summary = "设置存储空间ACL", description = "设置 Bucket 的访问权限")
    @PutMapping("/{bucketName}/acl")
    public Result setBucketAcl(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName,
        @Parameter(description = "ACL类型", required = true)
        @RequestParam com.duda.file.enums.AclType aclType) {

        bucketService.setBucketAcl(bucketName, aclType);

        return Result.success(null, "ACL 设置成功");
    }

    /**
     * 获取存储空间 ACL
     *
     * @param bucketName Bucket 名称
     * @return ACL 类型
     */
    @Operation(summary = "获取存储空间ACL", description = "查询 Bucket 的访问权限")
    @GetMapping("/{bucketName}/acl")
    public Result getBucketAcl(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName) {

        com.duda.file.enums.AclType aclType = bucketService.getBucketAcl(bucketName);

        return Result.success(aclType);
    }

    // ==================== 位置和区域 ====================

    /**
     * 获取存储空间所在区域
     *
     * 查询 Bucket 所在的地理区域。
     *
     * @param bucketName Bucket 名称
     * @return 区域信息
     */
    @Operation(summary = "获取存储空间区域", description = "查询 Bucket 所在的地理区域")
    @GetMapping("/{bucketName}/location")
    public Result getBucketLocation(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName) {

        String location = bucketService.getBucketLocation(bucketName);

        Map<String, String> response = new HashMap<>();
        response.put("bucketName", bucketName);
        response.put("location", location);

        return Result.success(response);
    }

    // ==================== 标签管理 ====================

    /**
     * 设置存储空间标签
     *
     * 为 Bucket 设置标签，用于分类和管理。
     *
     * @param bucketName Bucket 名称
     * @param tags 标签 Map
     * @return 设置结果
     */
    @Operation(summary = "设置存储空间标签", description = "为 Bucket 设置标签")
    @PutMapping("/{bucketName}/tags")
    public Result setBucketTags(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName,
        @Parameter(description = "标签Map", required = true)
        @RequestBody Map<String, String> tags) {

        bucketService.setBucketTags(bucketName, tags);

        return Result.success(null, "标签设置成功");
    }

    /**
     * 获取存储空间标签
     *
     * @param bucketName Bucket 名称
     * @return 标签 Map
     */
    @Operation(summary = "获取存储空间标签", description = "查询 Bucket 的标签")
    @GetMapping("/{bucketName}/tags")
    public Result getBucketTags(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName) {

        Map<String, String> tags = bucketService.getBucketTags(bucketName);

        return Result.success(tags);
    }

    // ==================== 配置管理 ====================

    /**
     * 更新存储空间配额
     *
     * 设置 Bucket 的存储容量和文件数量限制。
     *
     * @param bucketName Bucket 名称
     * @param maxSize 最大存储容量（字节）
     * @param maxCount 最大文件数量
     * @return 更新结果
     */
    @Operation(summary = "更新存储空间配额", description = "设置 Bucket 的容量和数量限制")
    @PutMapping("/{bucketName}/quota")
    public Result updateBucketQuota(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName,
        @Parameter(description = "最大存储容量(字节)", required = true)
        @RequestParam Long maxSize,
        @Parameter(description = "最大文件数量", required = true)
        @RequestParam Integer maxCount) {

        bucketService.updateBucketQuota(bucketName, maxSize, maxCount);

        return Result.success(null, "配额更新成功");
    }

    // ==================== 统计信息 ====================

    /**
     * 获取存储空间统计信息
     *
     * 查询 Bucket 的使用统计，包括：
     * - 文件总数
     * - 存储容量
     * - 图片/视频/文档/其他文件数量
     * - 流量统计
     * - 费用统计
     *
     * @param bucketName Bucket 名称
     * @return 统计信息
     */
    @Operation(summary = "获取存储空间统计信息", description = "查询 Bucket 的使用统计")
    @GetMapping("/{bucketName}/statistics")
    public Result getBucketStatistics(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName) {

        BucketStatisticsDTO statistics = bucketService.getBucketStatistics(bucketName);

        return Result.success(statistics);
    }

    /**
     * 获取存储空间容量信息
     *
     * 查询 Bucket 的容量使用情况。
     *
     * @param bucketName Bucket 名称
     * @return 容量信息
     */
    @Operation(summary = "获取存储空间容量信息", description = "查询 Bucket 的容量使用情况")
    @GetMapping("/{bucketName}/capacity")
    public Result getBucketCapacity(
        @Parameter(description = "Bucket名称", required = true)
        @PathVariable String bucketName) {

        BucketStatisticsDTO statistics = bucketService.getBucketStatistics(bucketName);

        Map<String, Object> capacity = new HashMap<>();
        capacity.put("bucketName", bucketName);
        capacity.put("storageUsed", statistics.getStorageUsed());
        capacity.put("fileCount", statistics.getFileCount());
        capacity.put("storageQuota", statistics.getStorageQuota());
        capacity.put("maxFileCount", null); // DTO 中没有此字段
        capacity.put("storageUsedPercent", statistics.getUsagePercentage());
        capacity.put("fileCountUsedPercent", null); // DTO 中没有此字段

        return Result.success(capacity);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算存储容量使用百分比
     */
    private Double calculateStorageUsedPercent(BucketStatisticsDTO statistics) {
        // DTO 中已经有 usagePercentage 字段，直接使用
        return statistics.getUsagePercentage() != null ? statistics.getUsagePercentage() : 0.0;
    }

    /**
     * 计算文件数量使用百分比
     */
    private Double calculateFileCountUsedPercent(BucketStatisticsDTO statistics) {
        // DTO 中没有此字段，返回 null
        return null;
    }
}
