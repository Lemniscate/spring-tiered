package com.github.lemniscate.lib.rest.util;

import com.github.lemniscate.lib.rest.repo.Model;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

// TODO this is the worst class ever. Let's get something better going...
public class EntityAwareBeanUtil extends BeanUtilsBean
        implements ApplicationContextAware{

    @PersistenceContext
    private EntityManager em;

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
//        CrudRepository<?, K> repo = GenericUtil.lookup(ctx, CrudRepository.class, type, 0);
//        if( repo != null ){
//                return (T) repo.findOne(id);
//        }
//        return null;
        return em.find(type, id);
    }

}
