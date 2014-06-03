package com.github.lemniscate.lib.tiered.mapping;

import com.github.lemniscate.lib.tiered.annotation.ApiResourceDetails;
import com.github.lemniscate.lib.tiered.util.BeanLookupUtil;
import org.springframework.core.GenericTypeResolver;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class ApiResourceAssembler<E extends Identifiable<ID>, ID extends Serializable, B> extends ResourceAssemblerSupport<E, Resource<E>> {

    @Inject
    protected EntityLinks entityLinks;

    @Inject
    protected ApiResourceLinkBuilderFactory arLinkBuilder;

    @Inject
    private BeanLookupUtil beanLookup;

    public ApiResourceAssembler() {
        super( determineParam(3, 0), (Class<Resource<E>>) (Class<?>) Resource.class);
    }

    @Override
    public Resource<E> toResource(E entity) {
        Collection<Link> links = new ArrayList<Link>();
        doAddLinks(links, entity);
        entity = prepare(entity);
        return new Resource<E>(entity, links);
    }

    /**
     * Adds a reference to self, then calls {@link ApiResourceAssembler#addLinks(java.util.Collection, org.springframework.hateoas.Identifiable)}
     * so implementations can customize links.
     */
    private void doAddLinks(Collection<Link> links, E entity) {
        ApiResourceDetails details = ApiResourceDetails.from(entity.getClass());
        Link link;
        if( details.isNested() && !details.isNestedCollection()){
            link = entityLinks.linkToSingleResource(entity).withSelfRel();

            String[] href = link.getHref().split("/");
            StringBuilder sb = new StringBuilder();
            for( int i = 0; i < href.length - 1; i++ ){
                sb.append(href[i])
                    .append(i < href.length - 2 ? "/" : "");
            }
            link = new Link(sb.toString(), "self");
        }else{
            link = entityLinks.linkToSingleResource(entity).withSelfRel();
        }
        links.add(link);
        addLinks(links, entity);
    }


    public void addLinks(Collection<Link> links, E entity) {
        AssemblerFieldHelper helper = new AssemblerFieldHelper(entityLinks, arLinkBuilder, beanLookup, links, entity);
        ReflectionUtils.doWithFields(entity.getClass(), helper, helper);
    }

    public E prepare(E t){
        return t;
    }

    // Shield your eyes: this nastiness gets us to have default constructors for concrete Assemblers.
    private static Class<?>  determineParam(int callStackIndex, int paramIndex){
        try {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            Assert.isTrue(st.length >= callStackIndex, "CallStack didn't contain enough elements");
            // the fourth entry should be our concrete class (unless we have some base-classes... crap)
            String name = st[callStackIndex].getClassName();
            Class<?> clazz = Class.forName(name);
            Type result = GenericTypeResolver.resolveTypeArguments(clazz, ApiResourceAssembler.class)[paramIndex];
            return result.getClass();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}

