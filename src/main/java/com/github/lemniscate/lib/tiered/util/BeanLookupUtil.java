package com.github.lemniscate.lib.tiered.util;


import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;

import javax.inject.Inject;

public class BeanLookupUtil {

    @Inject
    private ApplicationContext ctx;

    public <B> B lookupByTypeAndParameters(Class<?> type, Class<?>[] params){
        for(Object bean : ctx.getBeansOfType(type).values() ){
            Class<?>[] implTypes = GenericTypeResolver.resolveTypeArguments(bean.getClass(), type);
            if( implTypes == null || implTypes.length != params.length ){
                continue;
            }
            for(int i = 0; i < implTypes.length; i++){
                if( !implTypes[i].equals( params[i]) ){
                    continue;
                }
            }

            return (B) bean;
        }

        return null;
    }
}
