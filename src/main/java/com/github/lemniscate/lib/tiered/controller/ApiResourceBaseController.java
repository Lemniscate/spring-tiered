package com.github.lemniscate.lib.tiered.controller;

import com.github.lemniscate.lib.tiered.annotation.ApiResourceDetails;
import com.github.lemniscate.lib.tiered.mapping.ApiNestedResourceAssembler;
import com.github.lemniscate.lib.tiered.repo.ApiResourceRepository;
import com.github.lemniscate.lib.tiered.svc.ApiResourceService;
import com.github.lemniscate.lib.tiered.util.EntityAwareBeanUtil;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.hateoas.Identifiable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

@Transactional(propagation = Propagation.REQUIRED)
public abstract class ApiResourceBaseController<E extends Identifiable<ID>, ID extends Serializable, B>
        implements InitializingBean {

    public static final String X_SELF_HREF = "X-SELF-HREF";

    @Getter
    protected final ApiResourceDetails<E, ID, B> resource;

    @Inject
    protected EntityAwareBeanUtil beanUtil;

    @Inject
    protected ConversionService conversionService;

    protected final Class<E> domainClass;
    protected final Class<ID> idClass;
    protected final Class<B> beanClass;

    protected ApiResourceBaseController() {
        Class<E> persistentClass = (Class<E>) GenericTypeResolver.resolveTypeArguments(getClass(), ApiResourceBaseController.class)[0];
        ApiResourceDetails<E, ID, B> resource = ApiResourceDetails.from(persistentClass);
        this.resource = resource;
        this.domainClass = resource.getDomainClass();
        this.idClass = resource.getIdClass();
        this.beanClass = (Class<B>) resource.getDomainClass();
    }

    protected E createInstance(){
        try{
            return resource.getDomainClass().newInstance();
        }catch(Exception e){
            throw new RuntimeException("Generically failed creating a new instance generically", e);
        }
    }

    protected void copyProperties(E entity, B bean){
        beanUtil.easyCopyProperties(entity, bean);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(resource, "Resource must be supplied");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public static abstract class ApiResourceBaseNestedController<E extends Identifiable<ID>, ID extends Serializable, B, PE extends Identifiable<PID>, PID extends Serializable>
            extends ApiResourceBaseController<E, ID, B>{

        private final Class<?> parentClass;

        protected ApiResourceBaseNestedController() {
            super();
            this.parentClass = resource.getParentClass();
        }

        @Inject
        protected ApiNestedResourceAssembler<E, ID, B, PE, PID> assembler;
        @Inject
        protected ApiResourceRepository<E, ID> repository;
        @Inject
        protected ApiResourceService<E, ID> nestedEntityService;
        @Inject
        protected ApiResourceService<PE, PID> parentEntityService;

    }
}
