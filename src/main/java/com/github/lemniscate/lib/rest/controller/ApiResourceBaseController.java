package com.github.lemniscate.lib.rest.controller;

import com.github.lemniscate.lib.rest.annotation.ApiResourceWrapper;
import com.github.lemniscate.lib.rest.mapping.ApiNestedResourceAssembler;
import com.github.lemniscate.lib.rest.repo.ApiResourceRepository;
import com.github.lemniscate.lib.rest.svc.AbstractApiResourceService;
import com.github.lemniscate.lib.rest.util.EntityAwareBeanUtil;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.hateoas.Identifiable;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.io.Serializable;


public abstract class ApiResourceBaseController<E extends Identifiable<ID>, ID extends Serializable, B>
        implements InitializingBean {

    @Getter
    protected final ApiResourceWrapper<E, ID, B> resource;

    @Inject
    protected EntityAwareBeanUtil beanUtil;

    protected final Class<E> domainClass;
    protected final Class<ID> idClass;
    protected final Class<B> beanClass;

    protected ApiResourceBaseController(ApiResourceWrapper<E, ID, B> resource) {
        Assert.notNull(resource, "Resource information required");
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


    public static abstract class ApiResourceBaseNestedController<E extends Identifiable<ID>, ID extends Serializable, B, PE extends Identifiable<ID>>
            extends ApiResourceBaseController<E, ID, B>{

        private final Class<?> parentClass;

        protected ApiResourceBaseNestedController(ApiResourceWrapper<E, ID, B> resource) {
            super(resource);
            this.parentClass = resource.getParentClass();
        }

        @Inject
        protected ApiNestedResourceAssembler<E, ID, B, PE> assembler;
        @Inject
        protected ApiResourceRepository<PE, ID> parentRepository;
        @Inject
        protected ApiResourceRepository<E, ID> repository;
        @Inject
        protected AbstractApiResourceService<E, ID> nestedEntityService;
        @Inject
        protected AbstractApiResourceService<PE, ID> parentEntityService;

    }
}
