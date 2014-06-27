package com.github.lemniscate.lib.tiered.controller;

import com.github.lemniscate.lib.tiered.annotation.ApiResourceDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
* Used to represent nested entities, such as "/person/1/address/5". It is
* assumed that the nested entities will always be collections, as
* one-to-one relationships should be linked according to HATEOAS.
*
* @param <E> the nested entity.
* @param <ID> the identifier for the entities
* @param <B> the "bean" type that matches data submission for this entity.
* @param <PE> the "parentClass" entity.
*/
public class ApiResourceNestedCollectionController<E extends Identifiable<ID>, ID extends Serializable, B, PE extends Identifiable<PID>, PID extends Serializable>
        extends ApiResourceBaseController.ApiResourceBaseNestedController<E, ID, B, PE, PID>  {

    public ApiResourceNestedCollectionController(ApiResourceDetails<E, ID, B> resource) {
        super(resource);
    }

    /**
     * Used to associate a child entity to it's parentClass.
     * @param entity
     * @param parent
     */
    protected void associate(final E entity, final PE parent){
        final String parentProperty = resource.getParentProperty();
        Field field = ReflectionUtils.findField(entity.getClass(), parentProperty);
        field.setAccessible(true);
        ReflectionUtils.setField(field, entity, parent);

        ReflectionUtils.doWithFields(parent.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                if( oneToMany != null && oneToMany.mappedBy().equals(parentProperty) ){
                    associate(field, entity, parent);
                    return;
                }

                ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                if( manyToMany != null && manyToMany.mappedBy().equals(parentProperty) ){
                    associate(field, entity, parent);
                }
            }
        });
    }

    protected void associate(Field field, E entity,  PE parent){
        field.setAccessible(true);
        Collection<E> value = (Collection<E>) ReflectionUtils.getField(field, parent);
        value.add(entity);
        ReflectionUtils.setField(field, parent, value);
    }

    protected Page<E> loadFromEntity(final PE parent, MultiValueMap<String, String> params, Pageable pageable){
        Iterator<String> itr = params.keySet().iterator();
        while( itr.hasNext()){
            String key = itr.next();
            if (key.startsWith("_")){
                itr.remove();
            }
        }

        params.add(resource.getParentProperty() + ".id", conversionService.convert(parent.getId(), String.class));

        return nestedEntityService.query(params, pageable);
    };

    protected ResponseEntity<Page<Resource<E>>> getResponseEntity(Page<E> entities, PE parent, Pageable p){
        List<Resource<E>> resources = assembler.toResources(entities.getContent(), parent);
        Page<Resource<E>> pagedResources = new PageImpl<Resource<E>>(resources, p, entities.getTotalElements());
        return new ResponseEntity<Page<Resource<E>>>(pagedResources, HttpStatus.OK);
    }

    protected ResponseEntity<Resource<E>> getResponseEntity(E entity, PE parent, boolean created){
        HttpStatus status = HttpStatus.OK;
        Resource<E> resource = assembler.toResource(entity, parent);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        if( created ){
            headers.add(X_SELF_HREF, resource.getLink("self").getHref() );
            status = HttpStatus.CREATED;
        }

        ResponseEntity<Resource<E>> response = new ResponseEntity<Resource<E>>(resource, headers, status);
        return response;
    }

    @RequestMapping(value="", method= RequestMethod.GET)
    public ResponseEntity<Page<Resource<E>>> getAll(@PathVariable PID peId, @RequestParam MultiValueMap<String, String> params, Pageable p){
        PE pe = parentEntityService.getOne(peId);
        Page<E> entities = loadFromEntity(pe, params, p);
        return getResponseEntity(entities, pe, p);
    }

    @RequestMapping(value="/{neId}", method= RequestMethod.GET)
    public ResponseEntity<Resource<E>> getOne(@PathVariable PID peId, @PathVariable ID neId){
    	PE parent = parentEntityService.getOne(peId);
        E entity = nestedEntityService.getOne(neId);

        // TODO enforce that the parentClass matches the nested's parentClass...
        // PE parentClass = parentEntityService.getOne(peId);

        return getResponseEntity(entity, parent, false);
    }

    @RequestMapping(value="", method= RequestMethod.POST)
    public ResponseEntity<Resource<E>> postOne(@PathVariable PID peId, @RequestBody B bean){
        PE parent = parentEntityService.getOne(peId);

        E entity = createInstance();
        copyProperties(entity, bean);

        associate(entity, parent);
        nestedEntityService.save(entity);
        parentEntityService.save(parent);

        return getResponseEntity(entity, parent, true);
    }

    @RequestMapping(value="/{neId}", method= RequestMethod.PUT)
    public ResponseEntity<Resource<E>> putOne(@PathVariable PID peId, @PathVariable ID neId, @RequestBody B payload){
    	PE parent = parentEntityService.getOne(peId);
        E clone = createInstance();
        copyProperties(clone, payload);

        E entity = nestedEntityService.update(neId, clone);

        return getResponseEntity(entity, parent, false);
    }

}