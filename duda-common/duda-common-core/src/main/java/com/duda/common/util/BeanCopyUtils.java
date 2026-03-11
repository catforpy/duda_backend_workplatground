package com.duda.common.util;

import org.springframework.beans.BeanInstantiationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean转换工具类
 * 基于Spring的BeanUtils实现对象属性拷贝
 * 用于PO、DTO、VO之间的转换
 *
 * 使用方法：
 * <pre>
 * // 单个对象转换
 * UserDTO userDTO = BeanCopyUtils.copy(userPO, UserDTO.class);
 *
 * // 列表转换
 * List&lt;UserDTO&gt; dtoList = BeanCopyUtils.copyList(userPOList, UserDTO.class);
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public class BeanCopyUtils {

    /**
     * 单个对象转换
     * 将源对象的属性拷贝到目标对象
     *
     * @param source 源对象
     * @param targetClass 目标对象类
     * @param <T> 目标对象类型
     * @return 转换后的目标对象
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            // 使用 Spring 的 BeanUtils 拷贝属性
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new BeanInstantiationException(targetClass, "instantiation error", e);
        }
    }

    /**
     * 列表对象转换
     * 将源对象列表批量转换为目标对象列表
     *
     * @param sourceList 源对象列表
     * @param targetClass 目标对象类
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 转换后的目标对象列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> targetList = new ArrayList<>((int) (sourceList.size() / 0.75) + 1);
        for (S source : sourceList) {
            targetList.add(copy(source, targetClass));
        }
        return targetList;
    }

    /**
     * 更新对象属性
     * 将源对象的非空属性拷贝到目标对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void update(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        // 使用 Spring 的 BeanUtils 拷贝属性
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }
}
