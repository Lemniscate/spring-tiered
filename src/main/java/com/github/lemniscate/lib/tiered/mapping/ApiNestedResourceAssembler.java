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
import java.util.List;


public class ApiNestedResourceAssembler<E extends Identifiable<ID>, ID extends Serializable, B, P extends Identifiable<ID>> extends ResourceAssemblerSupport<E, Resource<E>> {

    @Inject
    protected EntityLinks entityLinks;

    @Inject
    protected ApiResourceLinkBuilderFactory arLinkBuilder;

    @Inject
    private BeanLookupUtil beanLookup;

    public ApiNestedResourceAssembler() {
        super( determineParam(3, 0), (Class<Resource<E>>)(Class<?>) Resource.class);
    }
	
	public List<Resource<E>> toResources(Iterable<? extends E> entities, P parent){
		Assert.notNull(entities);
		List<Resource<E>> result = new ArrayList<Resource<E>>();
		for (E entity : entities){
			result.add(toResource(entity, parent));
		}
		
		return result;
	}
	
	//NOTE: Shouldn't be called on a nested resource
	@Override
	public Resource<E> toResource(E entity) {
		throw new UnsupportedOperationException();
	}	
	
	public Resource<E> toResource(E entity, P parent){
		Collection<Link> links = new ArrayList<Link>();
		doAddLinks(links, entity, parent);
		return new Resource<E>(entity, links);
	}

	private void doAddLinks(Collection<Link> links, E entity, final P parent){
        ApiResourceDetails details = ApiResourceDetails.from(entity.getClass());
        String parentHref = entityLinks.linkForSingleResource(parent).toString();
        String[] pathSplit = details.getPath().split("/");
        String base = parentHref + "/" + pathSplit[pathSplit.length - 1];
        Link result = new Link( base + "/" + entity.getId(), "self");
		links.add(result);
		addLinks(links, entity, parent );
	}
	
	public void addLinks(final Collection<Link> links, final E entity, final P parent) {
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
            Type result = GenericTypeResolver.resolveTypeArguments(clazz, ApiNestedResourceAssembler.class)[paramIndex];
            return result.getClass();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
