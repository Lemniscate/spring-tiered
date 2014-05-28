package com.github.lemniscate.lib.tiered.controller;

import com.github.lemniscate.lib.tiered.annotation.ApiResourceDetails;
import com.github.lemniscate.lib.tiered.mapping.ApiNestedResourceAssembler;
import com.github.lemniscate.lib.tiered.repo.ApiResourceRepository;
import com.github.lemniscate.lib.tiered.svc.ApiResourceService;
import com.github.lemniscate.lib.tiered.util.EntityAwareBeanUtil;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.hateoas.Identifiable;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.io.Serializable;


public abstract class ApiResourceBaseController<E extends Identifiable<ID>, ID extends Serializable, B>
        implements InitializingBean {

    @Getter
    protected final ApiResourceDetails<E, ID, B> resource;

    @Inject
    protected EntityAwareBeanUtil beanUtil;

    protected final Class<E> domainClass;
    protected final Class<ID> idClass;
    protected final Class<B> beanClass;

    protected ApiResourceBaseController(ApiResourceDetails<E, ID, B> resource) {
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

        protected ApiResourceBaseNestedController(ApiResourceDetails<E, ID, B> resource) {
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
        protected ApiResourceService<E, ID> nestedEntityService;
        @Inject
        protected ApiResourceService<PE, ID> parentEntityService;

    }
}
