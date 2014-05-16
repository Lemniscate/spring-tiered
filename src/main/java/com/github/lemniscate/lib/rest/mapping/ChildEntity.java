package com.github.lemniscate.lib.rest.mapping;

import org.springframework.hateoas.Identifiable;

import java.io.Serializable;

/**
 * Designates that a nested entity should be responsible for associating itself with it's parent entity;
 * see {@link com.github.lemniscate.lib.sra.controller.ApiResourceNestedCollectionController#associate(org.springframework.hateoas.Identifiable, org.springframework.hateoas.Identifiable)}.
 */
public interface ChildEntity<PE, ID extends Serializable> extends Identifiable<ID> {
    void associate(PE parent);
}