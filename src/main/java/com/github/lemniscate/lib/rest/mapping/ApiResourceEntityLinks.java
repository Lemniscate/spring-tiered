package com.github.lemniscate.lib.rest.mapping;

import com.github.lemniscate.lib.rest.annotation.ApiNestedResource;
import com.github.lemniscate.lib.rest.annotation.ApiResource;
import com.github.lemniscate.lib.rest.controller.ApiResourceController;
import com.github.lemniscate.lib.rest.util.ApiResourceUtil;
import com.github.lemniscate.lib.rest.util.EntityAwareBeanUtil;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.core.AbstractEntityLinks;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @Author dave 5/5/14 4:39 PM
 */
public class ApiResourceEntityLinks extends AbstractEntityLinks{

    @Inject
    private ApiResourceLinkBuilderFactory linkBuilderFactory;

    @Inject
    private EntityAwareBeanUtil entityAwareBeanUtil;

    @Override
    public LinkBuilder linkFor(Class<?> entity) {
        return linkFor(entity, new Object[0]);
    }

    @Override
    public LinkBuilder linkFor(Class<?> entity, Object... parameters) {
        Assert.notNull(entity);
        return linkBuilderFactory.linkTo(ApiResourceController.class, entity, parameters);
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#getLinkToCollectionResource(java.lang.Class)
	 */
    @Override
    public Link linkToCollectionResource(Class<?> entity) {
        return linkFor(entity).withSelfRel();
    }

    @Override
    public Link linkToSingleResource(Class<?> entityClass, Object id) {
        Assert.notNull(entityClass);
        ApiNestedResource anr = entityClass.getAnnotation(ApiNestedResource.class);
        if( anr != null ){
            Identifiable<?> entity = (Identifiable<?>) entityAwareBeanUtil.loadUnrelatedEntity((Serializable) id, entityClass);
            return linkToSingleResource(entity);
        }else{
            return linkFor(entityClass).slash(id).withSelfRel();
        }
    }

    @Override
    public Link linkToSingleResource(Identifiable<?> entity) {
        Assert.notNull(entity);
        ApiNestedResource anr = entity.getClass().getAnnotation(ApiNestedResource.class);
        if( anr != null ){
            Identifiable<?> parent = ApiResourceUtil.getParentEntity(entity);

            return linkFor( parent.getClass() ).slash( parent.getId() )
                    .slash( anr.path() ).slash( entity.getId())
                    .withSelfRel();
        }else{
            return linkFor( entity.getClass() ).slash( entity.getId()).withSelfRel();
        }
    }

    @Override
    public boolean supports(Class<?> entity) {
        return AnnotationUtils.isAnnotationDeclaredLocally(ApiResource.class, entity)
                || AnnotationUtils.isAnnotationDeclaredLocally(ApiNestedResource.class, entity);
    }

}