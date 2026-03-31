package com.duda.tenant.rpc;

import com.duda.common.tenant.enums.TenantStatusEnum;
import com.duda.tenant.api.dto.TenantDTO;
import com.duda.tenant.api.dto.TenantCheckDTO;
import com.duda.tenant.entity.Tenant;
import com.duda.tenant.service.TenantService;
import com.duda.tenant.service.TenantStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 租户RPC实现测试
 *
 * @author Claude Code
 * @since 2026-03-28
 */
class TenantRpcImplTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private TenantStatisticsService tenantStatisticsService;

    @InjectMocks
    private TenantRpcImpl tenantRpc;

    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 准备测试数据
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setTenantCode("TEST001");
        testTenant.setTenantName("测试租户");
        testTenant.setTenantType("trial");
        testTenant.setTenantStatus(TenantStatusEnum.ACTIVE.getCode());
        testTenant.setMaxUsers(100);
        testTenant.setMaxStorageSize(1073741824L); // 1GB
        testTenant.setMaxApiCallsPerDay(10000);
        testTenant.setContactPerson("张三");
        testTenant.setContactPhone("13800138000");
        testTenant.setContactEmail("test@example.com");
        testTenant.setCreateTime(LocalDateTime.now());
        testTenant.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testGetTenantById() {
        // Given
        when(tenantService.getById(1L)).thenReturn(testTenant);

        // When
        TenantDTO result = tenantRpc.getTenantById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST001", result.getTenantCode());
        assertEquals("测试租户", result.getTenantName());
    }

    @Test
    void testGetTenantById_NotFound() {
        // Given
        when(tenantService.getById(1L)).thenReturn(null);

        // When
        TenantDTO result = tenantRpc.getTenantById(1L);

        // Then
        assertNull(result);
    }

    @Test
    void testCheckTenantValid() {
        // Given
        when(tenantService.getById(1L)).thenReturn(testTenant);
        when(tenantService.isValidTenant(1L)).thenReturn(true);

        // When
        TenantCheckDTO result = tenantRpc.checkTenantValid(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTenantId());
        assertEquals("TEST001", result.getTenantCode());
        assertEquals("测试租户", result.getTenantName());
        assertTrue(result.getIsValid());
        assertFalse(result.getIsExpired());
        assertFalse(result.getIsSuspended());
    }

    @Test
    void testCheckTenantValid_Expired() {
        // Given
        testTenant.setExpireTime(LocalDateTime.now().minusDays(1));
        when(tenantService.getById(1L)).thenReturn(testTenant);
        when(tenantService.isValidTenant(1L)).thenReturn(false);

        // When
        TenantCheckDTO result = tenantRpc.checkTenantValid(1L);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertTrue(result.getIsExpired());
        assertEquals("租户已过期", result.getErrorMessage());
    }

    @Test
    void testCheckTenantValid_Suspended() {
        // Given
        testTenant.setTenantStatus(TenantStatusEnum.SUSPENDED.getCode());
        when(tenantService.getById(1L)).thenReturn(testTenant);
        when(tenantService.isValidTenant(1L)).thenReturn(false);

        // When
        TenantCheckDTO result = tenantRpc.checkTenantValid(1L);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertTrue(result.getIsSuspended());
        assertEquals("租户已暂停", result.getErrorMessage());
    }

    @Test
    void testSuspendTenant() {
        // Given
        when(tenantService.suspendTenant(1L)).thenReturn(true);

        // When
        Boolean result = tenantRpc.suspendTenant(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void testActivateTenant() {
        // Given
        when(tenantService.activateTenant(1L)).thenReturn(true);

        // When
        Boolean result = tenantRpc.activateTenant(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void testUpdatePackage() {
        // Given
        when(tenantService.updatePackage(1L, 2L)).thenReturn(true);

        // When
        Boolean result = tenantRpc.updatePackage(1L, 2L);

        // Then
        assertTrue(result);
    }
}
