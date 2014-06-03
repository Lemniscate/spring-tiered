package com.github.lemniscate.lib.tiered.mapping;

import com.github.lemniscate.lib.tiered.annotation.ApiResourceAssemblerIgnore;
import com.github.lemniscate.lib.tiered.annotation.ApiResourceDetails;
import com.github.lemniscate.lib.tiered.controller.ApiResourceBaseController;
import com.github.lemniscate.lib.tiered.controller.ApiResourceController;
import com.github.lemniscate.lib.tiered.controller.ApiResourceNestedCollectionController;
import com.github.lemniscate.lib.tiered.util.BeanLookupUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

@RequiredArgsConstructor
final class AssemblerFieldHelper<E extends Identifiable<ID>, ID extends Serializable, B>
        implements ReflectionUtils.FieldCallback, ReflectionUtils.FieldFilter {

    private final EntityLinks entityLinks;
    private final ApiResourceLinkBuilderFactory arLinkBuilder;
    private final BeanLookupUtil beanLookup;

    private final Collection<Link> links;
    private final E entity;

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        ApiResourceDetails<E, ID, B> details = ApiResourceDetails.from(field);
        Assert.notNull(details, "Not a ApiResource...");


        if( Collection.class.isAssignableFrom(field.getType()) ){
            ReflectionUtils.makeAccessible(field);
            Collection<?> value = (Collection<?>) field.get(entity);
            if( value != null && !value.isEmpty() ){

                Class<?>[] types;
                ApiResourceBaseController controller;
                if( details.isNested() ){
                    types = new Class<?>[]{details.getDomainClass(), details.getIdClass(), details.getBeanClass(), details.getParentClass(), details.getParentIdClass()};
                    controller = beanLookup.lookupByTypeAndParameters(ApiResourceNestedCollectionController.class, types);
                }else{
                    types = new Class<?>[]{details.getDomainClass(), details.getIdClass(), details.getBeanClass()};
                    controller = beanLookup.lookupByTypeAndParameters(ApiResourceController.class, types);
                }
                links.add(arLinkBuilder.linkTo(controller.getClass(), details.getDomainClass(), entity.getId()).withRel( field.getName() ));
            }
        }else{
            ReflectionUtils.makeAccessible(field);
            Identifiable<ID> value = (Identifiable<ID>) field.get(entity);
            if( value != null ){
                links.add(entityLinks.linkToSingleResource(details.getDomainClass(), value.getId()).withRel(field.getName()));
            }
        }
    }

    @Override
    public boolean matches(Field field) {
        return ApiResourceDetails.from(field) != null
                && field.getAnnotation(ApiResourceAssemblerIgnore.class) == null;
    }

}
