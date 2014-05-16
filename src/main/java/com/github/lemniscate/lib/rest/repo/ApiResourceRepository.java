package com.github.lemniscate.lib.rest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.hateoas.Identifiable;

import java.io.Serializable;

@NoRepositoryBean
public interface ApiResourceRepository<E extends Identifiable<ID>, ID extends Serializable>
        extends JpaRepository<E, ID>, JpaSpecificationExecutor<E> {

}
