package com.duda.common.interceptor;

import com.duda.common.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 租户拦截器单元测试
 *
 * @author Claude Code
 * @since 2026-03-28
 */
class TenantInterceptorTest {

    private TenantInterceptor tenantInterceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        tenantInterceptor = new TenantInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        // 清理TenantContext，防止影响其他测试
        TenantContext.clear();
    }

    /**
     * 测试从Header提取tenant_id
     */
    @Test
    void testExtractFromHeader() throws Exception {
        // 设置Header
        request.addHeader("X-Tenant-Id", "123");

        // 执行拦截器
        boolean result = tenantInterceptor.preHandle(request, response, null);

        // 验证
        assertTrue(result, "拦截器应该返回true，继续处理请求");
        assertEquals(123L, TenantContext.getTenantId(), "应该从Header提取到tenant_id=123");

        // 清理
        tenantInterceptor.afterCompletion(request, response, null, null);
        assertFalse(TenantContext.hasTenantId(), "afterCompletion后应该清除tenant_id");
    }

    /**
     * 测试从请求参数提取tenant_id
     */
    @Test
    void testExtractFromParameter() throws Exception {
        // 设置请求参数
        request.setParameter("tenant_id", "456");

        // 执行拦截器
        boolean result = tenantInterceptor.preHandle(request, response, null);

        // 验证
        assertTrue(result);
        assertEquals(456L, TenantContext.getTenantId(), "应该从参数提取到tenant_id=456");

        // 清理
        tenantInterceptor.afterCompletion(request, response, null, null);
        assertFalse(TenantContext.hasTenantId());
    }

    /**
     * 测试未提供tenant_id时使用默认值
     */
    @Test
    void testDefaultTenantId() throws Exception {
        // 不设置任何tenant_id

        // 执行拦截器
        boolean result = tenantInterceptor.preHandle(request, response, null);

        // 验证
        assertTrue(result);
        assertEquals(1L, TenantContext.getTenantId(), "应该使用默认租户tenant_id=1");

        // 清理
        tenantInterceptor.afterCompletion(request, response, null, null);
        assertFalse(TenantContext.hasTenantId());
    }

    /**
     * 测试Header格式错误时的降级处理
     */
    @Test
    void testInvalidHeaderFormat() throws Exception {
        // 设置无效的Header格式
        request.addHeader("X-Tenant-Id", "abc");

        // 执行拦截器
        boolean result = tenantInterceptor.preHandle(request, response, null);

        // 验证：应该降级到默认租户
        assertTrue(result);
        assertEquals(1L, TenantContext.getTenantId(), "Header格式错误时应该使用默认租户");

        // 清理
        tenantInterceptor.afterCompletion(request, response, null, null);
        assertFalse(TenantContext.hasTenantId());
    }

    /**
     * 测试多个请求的隔离性
     */
    @Test
    void testMultipleRequestsIsolation() throws Exception {
        // 请求1: tenant_id=100
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addHeader("X-Tenant-Id", "100");
        tenantInterceptor.preHandle(request1, response, null);
        assertEquals(100L, TenantContext.getTenantId());

        // 清理请求1
        tenantInterceptor.afterCompletion(request1, response, null, null);
        assertFalse(TenantContext.hasTenantId());

        // 请求2: tenant_id=200
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("X-Tenant-Id", "200");
        tenantInterceptor.preHandle(request2, response, null);
        assertEquals(200L, TenantContext.getTenantId());

        // 清理请求2
        tenantInterceptor.afterCompletion(request2, response, null, null);
        assertFalse(TenantContext.hasTenantId());
    }
}
