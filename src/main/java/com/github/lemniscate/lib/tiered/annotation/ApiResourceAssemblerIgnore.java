package com.github.lemniscate.lib.tiered.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResourceAssemblerIgnore {}
