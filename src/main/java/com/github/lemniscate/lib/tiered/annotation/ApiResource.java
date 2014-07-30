package com.github.lemniscate.lib.tiered.annotation;

import java.io.Serializable;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResource {

    Class<?> beanClass() default Object.class;
    Class<? extends Serializable> idClass() default Long.class;
    String path() default "";
}
