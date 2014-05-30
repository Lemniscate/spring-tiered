package com.github.lemniscate.lib.tiered.controller;

import com.github.lemniscate.lib.tiered.mapping.ApiResourceAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;

// TODO we need a waaaay better strategy here
public class HateoasControllerSupport<E extends Identifiable<ID>, ID extends Serializable, B> {

    private ApiResourceAssembler<E, ID, B> assembler;

    public ResponseEntity<Resource<E>> toResponse(E entity) {
        if( entity == null ){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Resource<E> resource = assembler.toResource(entity);
        ResponseEntity<Resource<E>> response = new ResponseEntity<Resource<E>>(resource, HttpStatus.OK);
        return response;
    };

    public ResponseEntity<Page<Resource<E>>> toResponse(Page<E> entities, Pageable pageInfo) {
        if( entities == null || entities.getContent().isEmpty() ){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        List<E> content = entities.getContent();
        List<Resource<E>> resources = assembler.toResources(content);
        Page<Resource<E>> pagedResources = new PageImpl<Resource<E>>(resources, pageInfo, entities.getTotalElements());
        ResponseEntity<Page<Resource<E>>> response = new ResponseEntity<Page<Resource<E>>>(pagedResources, HttpStatus.OK);
        return response;
    };

    public static <E extends Identifiable<ID>, ID extends Serializable, B> ResponseEntity<Resource<E>> toResponse(ApiResourceAssembler<E, ID, B> assembler, E entity) {
        if( entity == null ){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Resource<E> resource = assembler.toResource(entity);
        ResponseEntity<Resource<E>> response = new ResponseEntity<Resource<E>>(resource, HttpStatus.OK);
        return response;
    };

    public static <E extends Identifiable<ID>, ID extends Serializable, B> ResponseEntity<Page<Resource<E>>> toResponse(ApiResourceAssembler<E, ID, B> assembler, Page<E> entities, Pageable pageInfo) {
        if( entities == null || entities.getContent().isEmpty() ){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        List<E> content = entities.getContent();
        List<Resource<E>> resources = assembler.toResources(content);
        Page<Resource<E>> pagedResources = new PageImpl<Resource<E>>(resources, pageInfo, entities.getTotalElements());
        ResponseEntity<Page<Resource<E>>> response = new ResponseEntity<Page<Resource<E>>>(pagedResources, HttpStatus.OK);
        return response;
    };

}
