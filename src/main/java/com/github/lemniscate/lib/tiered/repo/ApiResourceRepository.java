package com.github.lemniscate.lib.tiered.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.hateoas.Identifiable;

import java.io.Serializable;

/**
 * Base interface that all repositories are generated from. Custom implementations should extend this
 * interface.
 *
 * @param <E>
 * @param <ID>
 */
@NoRepositoryBean
public interface ApiResourceRepository<E extends Identifiable<ID>, ID extends Serializable>
        extends JpaRepository<E, ID>, JpaSpecificationExecutor<E> {

}
