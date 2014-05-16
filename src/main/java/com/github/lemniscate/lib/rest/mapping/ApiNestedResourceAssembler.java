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
import java.util.List;


public class ApiNestedResourceAssembler<T extends Identifiable<ID>, ID extends Serializable, B, P extends Identifiable<ID>> extends ResourceAssemblerSupport<T, Resource<T>> {

    private final Class<T> domainClass;
    private final Class<P> parentClass;

    @Inject
    protected EntityLinks el;

    private static Class<?> resourceClass(){
        return  (Class<?>) Resource.class;
    }
//
//    public ApiNestedResourceAssembler(Class<? extends ApiResourceBaseController.ApiResourceBaseNestedController<T, ID, B, P>> controllerClass) {
//        super( controllerClass, (Class<Resource<T>>) resourceClass());
//        Class<?>[] types = GenericTypeResolver.resolveTypeArguments(controllerClass, ApiResourceBaseController.ApiResourceBaseNestedController.class);
//        this.domainClass = (Class<T>) types[0];
//        this.parentClass = (Class<P>) types[3];
//    }

    public ApiNestedResourceAssembler() {
        super( determineParam(3, 0), (Class<Resource<T>>) resourceClass());
//        Class<?>[] types = GenericTypeResolver.resolveTypeArguments(determineParam(3, 0), ApiResourceBaseController.ApiResourceBaseNestedController.class);
        this.domainClass = (Class<T>) determineParam(3, 0);
        this.parentClass = (Class<P>) determineParam(3, 3);
    }
	
	public List<Resource<T>> toResources(Iterable<? extends T> entities, P parent){
		Assert.notNull(entities);
		List<Resource<T>> result = new ArrayList<Resource<T>>();
		for (T entity : entities){
			result.add(toResource(entity, parent));
		}
		
		return result;
	}
	
	//NOTE: Shouldn't be called on a nested resource
	@Override
	public Resource<T> toResource(T entity) {
		throw new UnsupportedOperationException();
	}	
	
	public Resource<T> toResource(T entity, P parent){
		Collection<Link> links = new ArrayList<Link>();
		doAddLinks(links, entity, parent);
		return new Resource<T>(entity, links);
	}

	protected void doAddLinks(Collection<Link> links, T entity, P parent){
		links.add(el.linkToSingleResource(entity).withSelfRel());
		addLinks(links, entity, parent );
	}
	
	public void addLinks(Collection<Link> links, T entity, P parent) {}

    public T prepare(T t){
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
            Type result = GenericTypeResolver.resolveTypeArguments(clazz, ApiNestedResourceAssembler.class)[paramIndex];
            return result.getClass();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
