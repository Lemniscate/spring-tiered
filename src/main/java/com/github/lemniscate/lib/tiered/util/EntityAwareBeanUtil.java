package com.github.lemniscate.lib.tiered.util;

import com.github.lemniscate.lib.tiered.repo.Model;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

// TODO this is the worst class ever. Let's get something better going...
public class EntityAwareBeanUtil extends BeanUtilsBean
        implements ApplicationContextAware{

    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

    @Override
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if(value==null)return;

        // If the property being copied should be an entity, so load it from Spring
        if( value != null && value instanceof Model.ModelImpl ){
            Model.ModelImpl placeHolder = (Model.ModelImpl) value;
            Class<Object> entityType = (Class<Object>) ReflectionUtils.findField(dest.getClass(), name).getType();
            value = loadUnrelatedEntity(placeHolder.getId(), entityType);
        }

        // copy the property
        super.copyProperty(dest, name, value);
    }

    public void easyCopyProperties(Object dest, Object orig) {
        try {
            super.copyProperties(dest, orig);
        } catch (Exception e){
            throw new RuntimeException("Failed while copying properties", e);
        }
    }

    public <T, K extends Serializable> T loadUnrelatedEntity(K id, Class<T> type){
        Map<String, CrudRepository> repos = ctx.getBeansOfType(CrudRepository.class);
        for(CrudRepository repo : repos.values()){
            Class<?>[] types = GenericTypeResolver.resolveTypeArguments(repo.getClass(), CrudRepository.class);
            if( types != null && types.length > 0 && type.equals( types[0]) ){
                return (T) repo.findOne(id);
            }
        }
        return null;
    }

}
