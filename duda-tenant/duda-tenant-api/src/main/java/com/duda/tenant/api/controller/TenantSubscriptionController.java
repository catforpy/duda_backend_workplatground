package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.TenantSubscriptionDTO;
import com.duda.tenant.api.dto.TenantSubscriptionLimitDTO;
import com.duda.tenant.api.service.TenantSubscriptionService;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户订阅Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/subscription")
public class TenantSubscriptionController {

    @Autowired
    private TenantSubscriptionService tenantSubscriptionService;

    /**
     * 根据ID查询订阅
     *
     * @param id 订阅ID
     * @return 订阅DTO
     */
    @GetMapping("/{id}")
    public ResultVO<TenantSubscriptionDTO> getById(@PathVariable Long id) {
        log.info("查询订阅: id={}", id);
        TenantSubscriptionDTO subscription = tenantSubscriptionService.getById(id);
        if (subscription == null) {
            return ResultVO.error("订阅不存在");
        }
        return ResultVO.success(subscription);
    }

    /**
     * 根据订阅编号查询
     *
     * @param subscriptionCode 订阅编号
     * @return 订阅DTO
     */
    @GetMapping("/code/{subscriptionCode}")
    public ResultVO<TenantSubscriptionDTO> getBySubscriptionCode(@PathVariable String subscriptionCode) {
        log.info("根据订阅编号查询: subscriptionCode={}", subscriptionCode);
        TenantSubscriptionDTO subscription = tenantSubscriptionService.getBySubscriptionCode(subscriptionCode);
        if (subscription == null) {
            return ResultVO.error("订阅不存在");
        }
        return ResultVO.success(subscription);
    }

    /**
     * 查询租户的所有订阅
     *
     * @param tenantId 租户ID
     * @return 订阅列表
     */
    @GetMapping("/list/tenant/{tenantId}")
    public ResultVO<List<TenantSubscriptionDTO>> listByTenantId(@PathVariable Long tenantId) {
        log.info("查询租户的所有订阅: tenantId={}", tenantId);
        List<TenantSubscriptionDTO> subscriptions = tenantSubscriptionService.listByTenantId(tenantId);
        return ResultVO.success(subscriptions);
    }

    /**
     * 查询用户的所有订阅
     *
     * @param userId 用户ID
     * @return 订阅列表
     */
    @GetMapping("/list/user/{userId}")
    public ResultVO<List<TenantSubscriptionDTO>> listByUserId(@PathVariable Long userId) {
        log.info("查询用户的所有订阅: userId={}", userId);
        List<TenantSubscriptionDTO> subscriptions = tenantSubscriptionService.listByUserId(userId);
        return ResultVO.success(subscriptions);
    }

    /**
     * 查询生效中的订阅
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 生效中的订阅
     */
    @GetMapping("/active")
    public ResultVO<TenantSubscriptionDTO> getActiveSubscription(
            @RequestParam Long tenantId,
            @RequestParam Long userId) {
        log.info("查询生效中的订阅: tenantId={}, userId={}", tenantId, userId);
        TenantSubscriptionDTO subscription = tenantSubscriptionService.getActiveSubscription(tenantId, userId);
        if (subscription == null) {
            return ResultVO.error("未找到生效中的订阅");
        }
        return ResultVO.success(subscription);
    }

    /**
     * 创建订阅
     *
     * @param subscriptionDTO 订阅DTO
     * @return 创建的订阅
     */
    @PostMapping("/create")
    public ResultVO<TenantSubscriptionDTO> create(@RequestBody TenantSubscriptionDTO subscriptionDTO) {
        log.info("创建订阅: subscriptionCode={}, tenantId={}, userId={}",
                subscriptionDTO.getSubscriptionCode(),
                subscriptionDTO.getTenantId(),
                subscriptionDTO.getUserId());

        // 参数校验
        if (subscriptionDTO.getSubscriptionCode() == null || subscriptionDTO.getSubscriptionCode().isEmpty()) {
            return ResultVO.error("订阅编号不能为空");
        }
        if (subscriptionDTO.getTenantId() == null) {
            return ResultVO.error("租户ID不能为空");
        }
        if (subscriptionDTO.getUserId() == null) {
            return ResultVO.error("用户ID不能为空");
        }
        if (subscriptionDTO.getPackageId() == null) {
            return ResultVO.error("套餐ID不能为空");
        }
        if (subscriptionDTO.getStartTime() == null) {
            return ResultVO.error("订阅开始时间不能为空");
        }

        TenantSubscriptionDTO created = tenantSubscriptionService.create(subscriptionDTO);
        return ResultVO.success("创建订阅成功", created);
    }

    /**
     * 更新订阅
     *
     * @param subscriptionDTO 订阅DTO
     * @return 更新后的订阅
     */
    @PostMapping("/update")
    public ResultVO<TenantSubscriptionDTO> update(@RequestBody TenantSubscriptionDTO subscriptionDTO) {
        log.info("更新订阅: id={}", subscriptionDTO.getId());

        if (subscriptionDTO.getId() == null) {
            return ResultVO.error("订阅ID不能为空");
        }

        TenantSubscriptionDTO updated = tenantSubscriptionService.update(subscriptionDTO);
        return ResultVO.success("更新订阅成功", updated);
    }

    /**
     * 取消订阅
     *
     * @param id 订阅ID
     * @param cancelReason 取消原因
     * @param cancelBy 取消操作人
     * @return 是否成功
     */
    @PostMapping("/cancel")
    public ResultVO<Boolean> cancel(
            @RequestParam Long id,
            @RequestParam(required = false) String cancelReason,
            @RequestParam(required = false) Long cancelBy) {
        log.info("取消订阅: id={}, cancelBy={}", id, cancelBy);
        Boolean result = tenantSubscriptionService.cancel(id, cancelReason, cancelBy);
        return ResultVO.success("取消订阅成功", result);
    }

    /**
     * 暂停订阅
     *
     * @param id 订阅ID
     * @return 是否成功
     */
    @PostMapping("/suspend")
    public ResultVO<Boolean> suspend(@RequestParam Long id) {
        log.info("暂停订阅: id={}", id);
        Boolean result = tenantSubscriptionService.suspend(id);
        return ResultVO.success("暂停订阅成功", result);
    }

    /**
     * 激活订阅
     *
     * @param id 订阅ID
     * @return 是否成功
     */
    @PostMapping("/activate")
    public ResultVO<Boolean> activate(@RequestParam Long id) {
        log.info("激活订阅: id={}", id);
        Boolean result = tenantSubscriptionService.activate(id);
        return ResultVO.success("激活订阅成功", result);
    }

    /**
     * 续费订阅
     *
     * @param id 订阅ID
     * @param months 续费月数
     * @return 是否成功
     */
    @PostMapping("/renew")
    public ResultVO<Boolean> renew(
            @RequestParam Long id,
            @RequestParam Integer months) {
        log.info("续费订阅: id={}, months={}", id, months);
        if (months == null || months <= 0) {
            return ResultVO.error("续费月数必须大于0");
        }
        Boolean result = tenantSubscriptionService.renew(id, months);
        return ResultVO.success("续费订阅成功", result);
    }

    /**
     * 查询订阅的所有限制条件
     *
     * @param subscriptionId 订阅ID
     * @return 限制条件列表
     */
    @GetMapping("/limits/{subscriptionId}")
    public ResultVO<List<TenantSubscriptionLimitDTO>> listLimits(@PathVariable Long subscriptionId) {
        log.info("查询订阅的所有限制条件: subscriptionId={}", subscriptionId);
        List<TenantSubscriptionLimitDTO> limits = tenantSubscriptionService.listLimits(subscriptionId);
        return ResultVO.success(limits);
    }

    /**
     * 查询即将到期的订阅（7天内）
     *
     * @return 订阅列表
     */
    @GetMapping("/list/expiring")
    public ResultVO<List<TenantSubscriptionDTO>> listExpiringSoon() {
        log.info("查询即将到期的订阅（7天内）");
        List<TenantSubscriptionDTO> subscriptions = tenantSubscriptionService.listExpiringSoon();
        return ResultVO.success(subscriptions);
    }
}
