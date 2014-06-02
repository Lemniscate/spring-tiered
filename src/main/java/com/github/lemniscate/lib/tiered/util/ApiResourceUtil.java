package com.github.lemniscate.lib.tiered.util;

import com.github.lemniscate.lib.tiered.annotation.ApiNestedResource;
import com.github.lemniscate.lib.tiered.annotation.ApiResource;
import org.reflections.Reflections;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Utility class for working with ApiResource models.
 *
 * @Author dave 5/8/14 5:18 PM
 */
public class ApiResourceUtil {

    public static <T> T getParentEntity(Object child){
        ApiNestedResource anr = child.getClass().getAnnotation(ApiNestedResource.class);
        Assert.notNull( anr, "Not a nested resource");

        Field field = ReflectionUtils.findField(child.getClass(), anr.parentProperty());
        field.setAccessible(true);
        Object parent = ReflectionUtils.getField(field, child);
        return (T) parent;
    }

    public static Set<Class<?>> getAllTaggedClasses(String basePackage){
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(ApiResource.class);
        entities.addAll(reflections.getTypesAnnotatedWith(ApiNestedResource.class));

        return entities;
    }

}
