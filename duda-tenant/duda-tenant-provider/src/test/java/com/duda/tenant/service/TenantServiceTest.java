package com.duda.tenant.service;

import com.duda.common.tenant.enums.TenantStatusEnum;
import com.duda.tenant.entity.Tenant;
import com.duda.tenant.entity.TenantPackage;
import com.duda.tenant.mapper.TenantMapper;
import com.duda.tenant.mapper.TenantPackageMapper;
import com.duda.tenant.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 租户服务测试
 *
 * @author Claude Code
 * @since 2026-03-28
 */
class TenantServiceTest {

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private TenantPackageService tenantPackageService;

    @InjectMocks
    private TenantServiceImpl tenantService;

    private Tenant testTenant;
    private TenantPackage testPackage;

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

        testPackage = new TenantPackage();
        testPackage.setId(1L);
        testPackage.setPackageCode("TRIAL");
        testPackage.setPackageName("试用版");
        testPackage.setPackageType("trial");
        testPackage.setMaxUsers(100);
        testPackage.setMaxStorageSize(1073741824L); // 1GB
        testPackage.setMaxApiCallsPerDay(10000);
    }

    @Test
    void testCreateTenant() {
        // Given
        when(tenantMapper.insert(any(Tenant.class))).thenReturn(1);

        // When
        Tenant result = tenantService.createTenant(testTenant);

        // Then
        assertNotNull(result);
        assertEquals("TEST001", result.getTenantCode());
        assertEquals("测试租户", result.getTenantName());
        verify(tenantMapper, times(1)).insert(any(Tenant.class));
    }

    @Test
    void testUpdateTenant() {
        // Given
        when(tenantMapper.updateById(any(Tenant.class))).thenReturn(1);

        // When
        testTenant.setTenantName("更新后的租户名称");
        Tenant result = tenantService.updateTenant(testTenant);

        // Then
        assertNotNull(result);
        assertEquals("更新后的租户名称", result.getTenantName());
        verify(tenantMapper, times(1)).updateById(any(Tenant.class));
    }

    @Test
    void testSuspendTenant() {
        // Given
        when(tenantMapper.selectById(1L)).thenReturn(testTenant);
        when(tenantMapper.updateById(any(Tenant.class))).thenReturn(1);

        // When
        Boolean result = tenantService.suspendTenant(1L);

        // Then
        assertTrue(result);
        assertEquals(TenantStatusEnum.SUSPENDED.getCode(), testTenant.getTenantStatus());
        verify(tenantMapper, times(1)).updateById(any(Tenant.class));
    }

    @Test
    void testActivateTenant() {
        // Given
        testTenant.setTenantStatus(TenantStatusEnum.SUSPENDED.getCode());
        when(tenantMapper.selectById(1L)).thenReturn(testTenant);
        when(tenantMapper.updateById(any(Tenant.class))).thenReturn(1);

        // When
        Boolean result = tenantService.activateTenant(1L);

        // Then
        assertTrue(result);
        assertEquals(TenantStatusEnum.ACTIVE.getCode(), testTenant.getTenantStatus());
        verify(tenantMapper, times(1)).updateById(any(Tenant.class));
    }

    @Test
    void testIsValidTenant() {
        // Given
        when(tenantMapper.selectById(1L)).thenReturn(testTenant);

        // When
        Boolean result = tenantService.isValidTenant(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsValidTenant_Expired() {
        // Given
        testTenant.setExpireTime(LocalDateTime.now().minusDays(1));
        when(tenantMapper.selectById(1L)).thenReturn(testTenant);

        // When
        Boolean result = tenantService.isValidTenant(1L);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdatePackage() {
        // Given
        when(tenantMapper.selectById(1L)).thenReturn(testTenant);
        when(tenantPackageService.getById(1L)).thenReturn(testPackage);
        when(tenantMapper.updateById(any(Tenant.class))).thenReturn(1);

        // When
        Boolean result = tenantService.updatePackage(1L, 1L);

        // Then
        assertTrue(result);
        assertEquals(1L, testTenant.getPackageId());
        assertEquals(100, testTenant.getMaxUsers());
        verify(tenantMapper, times(1)).updateById(any(Tenant.class));
    }
}
