package com.github.lemniscate.lib.rest.controller;

import com.github.lemniscate.lib.rest.annotation.ApiResourceWrapper;
import com.github.lemniscate.lib.rest.mapping.AbstractApiResourceAssembler;
import com.github.lemniscate.lib.rest.repo.ApiResourceRepository;
import com.github.lemniscate.lib.rest.svc.AbstractApiResourceService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Getter @Setter
public class ApiResourceController<E extends Identifiable<ID>, ID extends Serializable, B>
        extends ApiResourceBaseController<E, ID, B>
        implements InitializingBean {

    @Inject
    private AbstractApiResourceAssembler<E, ID, B> assembler;

    @Inject
    private ApiResourceRepository<E, ID> repository;

    @Inject
    private AbstractApiResourceService<E, ID> service;

    @Inject
    private ConversionService conversionService;
    @Inject
    private ApplicationContext ctx;

    @PersistenceContext
    private EntityManager em;

    public ApiResourceController(ApiResourceWrapper<E, ID, B> resource) {
        super(resource);
    }

    @RequestMapping(value="", method=RequestMethod.GET)
    public ResponseEntity<Page<Resource<E>>> getAll(@RequestParam MultiValueMap<String, String> params, Pageable p){
        //Need to remove pageable/sortable stuff
        Iterator<String> itr = params.keySet().iterator();
        while( itr.hasNext()){
            String key = itr.next();
            if (key.startsWith("_")){
                itr.remove();
            }
        }

        Page<E> entities = service.query(params, p);
        List<Resource<E>> resources = assembler.toResources(entities.getContent());
        Page<Resource<E>> pagedResources = new PageImpl<Resource<E>>(resources, p, entities.getTotalElements());
        ResponseEntity<Page<Resource<E>>> response = new ResponseEntity<Page<Resource<E>>>(pagedResources, HttpStatus.OK);
        return response;
    }

    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public ResponseEntity<Resource<E>> getOne(@PathVariable ID id){
        E entity = service.getOne( conversionService.convert(id, resource.getIdClass()) );
        Resource<E> resource = assembler.toResource(entity);
        ResponseEntity<Resource<E>> response = new ResponseEntity<Resource<E>>(resource, HttpStatus.OK);
        return response;
    }

    @RequestMapping(value="/{id}", method=RequestMethod.DELETE)
    public HttpStatus deleteOne(@PathVariable ID id){
        service.deleteOne( conversionService.convert(id, resource.getIdClass() ) );
        return HttpStatus.OK;
    }

    @RequestMapping(value="", method=RequestMethod.POST)
    public ResponseEntity<Resource<E>> postOne(@RequestBody B bean){
        E entity = createInstance();
        copyProperties(entity, bean);

        entity = service.save(entity);
        Resource<E> resource = assembler.toResource(entity);
        ResponseEntity<Resource<E>> response = new ResponseEntity<Resource<E>>(resource, HttpStatus.OK);
        return response;
    }

    @RequestMapping(value="/{id}", method=RequestMethod.PUT)
    public ResponseEntity<Resource<E>> putOne(@PathVariable ID id, @RequestBody B bean){
        Assert.notNull(bean, "Parameter 'bean' must be provided");

        E clone = createInstance();
        copyProperties(clone, bean);

        E entity = service.update( conversionService.convert(id, resource.getIdClass()) , clone);
        Resource<E> resource = assembler.toResource(entity);
        ResponseEntity<Resource<E>> response = new ResponseEntity<Resource<E>>(resource, HttpStatus.OK);
        return response;
    }

}
