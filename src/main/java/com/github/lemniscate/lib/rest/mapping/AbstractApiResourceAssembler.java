package com.github.lemniscate.lib.rest.mapping;

import org.springframework.core.GenericTypeResolver;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractApiResourceAssembler<E extends Identifiable<ID>, ID extends Serializable, B> extends ResourceAssemblerSupport<E, Resource<E>> {

    @Inject
    protected EntityLinks el;

    private static Class<?> resourceClass(){
        return  (Class<?>) Resource.class;
    }
//
//    @Inject
//    public AbstractApiResourceAssembler(Class<? extends ApiResourceController<E, ID, B>> controllerClass) {
//        super( controllerClass, (Class<Resource<E>>) resourceClass());
//    }

    public AbstractApiResourceAssembler() {
        super( determineParam(3, 0), (Class<Resource<E>>) resourceClass());
    }

    @Override
    public Resource<E> toResource(E entity) {
        Collection<Link> links = new ArrayList<Link>();
        doAddLinks(links, entity);
        entity = prepare(entity);
        return new Resource<E>(entity, links);
    }

    /**
     * Adds a reference to self, then calls {@link AbstractApiResourceAssembler#addLinks(java.util.Collection, org.springframework.hateoas.Identifiable)}
     * so implementations can customize links.
     */
    protected void doAddLinks(Collection<Link> links, E entity) {
        links.add( el.linkToSingleResource(entity).withSelfRel() );
        addLinks(links, entity);
    }


    public void addLinks(Collection<Link> links, E entity) {}

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
            Type result = GenericTypeResolver.resolveTypeArguments(clazz, AbstractApiResourceAssembler.class)[paramIndex];
            return result.getClass();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}

