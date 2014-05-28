package com.github.lemniscate.lib.tiered.controller;

import com.github.lemniscate.lib.tiered.annotation.ApiResourceDetails;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.OneToOne;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
* @Author dave 5/9/14 9:20 PM
*/
public class ApiResourceNestedPropertyController<E extends Identifiable<ID>, ID extends Serializable, B, PE extends Identifiable<ID>>
        extends ApiResourceBaseController.ApiResourceBaseNestedController<E, ID, B, PE> {

    public ApiResourceNestedPropertyController(ApiResourceDetails<E, ID, B> resource) {
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
            OneToOne oneToMany = field.getAnnotation(OneToOne.class);
            if( oneToMany != null && oneToMany.mappedBy().equals(parentProperty) ){
                field.setAccessible(true);
                field.set(parent, entity);
            }
            }
        });
    }

    /**
     * Defines how to load nested entities from a given entity.
     *
     * @param parent the parentClass entity
     * @return
     */
    protected E loadFromEntity(final PE parent){
        Specification<E> spec = new Specification<E>() {
            @Override
            public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal( root.get( resource.getParentProperty() ).get("id"), parent.getId() );
            }
        };
        return repository.findOne(spec);
    };

    protected ResponseEntity<Resource<E>> getResponseEntity(E entity, PE parent){
        if( entity == null ){
            return new ResponseEntity<Resource<E>>(HttpStatus.NOT_FOUND);
        }else{
            Resource<E> resource = assembler.toResource(entity, parent);
            return new ResponseEntity<Resource<E>>(resource, HttpStatus.OK);
        }
    }

    // ************************************************************************************************

    @RequestMapping(value="", method= RequestMethod.GET)
    public ResponseEntity<Resource<E>> getOne(@PathVariable ID peId){
        PE pe = parentEntityService.getOne(peId);
        E entity = loadFromEntity(pe);
        return getResponseEntity(entity, pe);
    }

    @RequestMapping(value="", method= RequestMethod.PUT)
    public ResponseEntity<Resource<E>> putOne(@PathVariable ID peId, @RequestBody B payload){
        PE parent = parentEntityService.getOne(peId);
        E entity = loadFromEntity(parent);
        Assert.notNull(entity);
        copyProperties(entity, payload);
        nestedEntityService.save(entity);
        return getResponseEntity(entity, parent);
    }

    @RequestMapping(value="", method= RequestMethod.POST)
    public ResponseEntity<Resource<E>> postOne(@PathVariable ID peId, @RequestBody B bean){
        PE parent = parentEntityService.getOne(peId);

        E entity = createInstance();
        copyProperties(entity, bean);

        associate(entity, parent);
        nestedEntityService.save(entity);
        parentEntityService.save(parent);

        return getResponseEntity(entity, parent);
    }

}
