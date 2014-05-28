package com.github.lemniscate.lib.tiered.svc;

import com.github.lemniscate.lib.tiered.repo.ApiResourceRepository;
import com.github.lemniscate.lib.tiered.repo.specification.GenericMapPathSpecification;
import com.github.lemniscate.lib.tiered.util.EntityAwareBeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.Identifiable;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * The service tier, where business logic should exist.
 *
 * @param <E> - The Entity type.
 * @param <ID> - The Key type.
 *
 * @Author dave 2/6/14 7:56 PM
 */
@Transactional
@RequiredArgsConstructor
public class ApiResourceService<E extends Identifiable<ID>, ID extends Serializable> {

    public ApiResourceService(Class<E> domainClass, Class<ID> idClass) {
        this.idClass = idClass;
        this.domainClass = domainClass;
    }

    @Setter
    protected Class<E> domainClass;
    @Setter
    protected Class<ID> idClass;

    @Inject
    protected ApiResourceRepository<E, ID> repo;

    @Inject
    private ApplicationContext ctx;

    @Inject
    private ConversionService conversionService;

    @Inject
    private EntityAwareBeanUtil beanUtil;

    public Page<E> query(MultiValueMap<String, String> params, Pageable pageable){
        Specification<E> spec = new GenericMapPathSpecification<E>(params, conversionService);
        Page<E> result = repo.findAll(spec, pageable);
        return result;
    }

    public Page<E> getAll(Pageable p){
        return repo.findAll(p);
    }

    public E getOne(ID id){
        return repo.findOne(id);
    }
    
    public void deleteOne(ID id){
    	repo.delete(id);
    }

    public E save(E entity){
        return repo.save(entity);
    }

    public E update(ID id, E clone){
        Assert.notNull(clone, "Parameter 'clone' must be provided");

        E existing = repo.findOne(id);
        Assert.notNull(existing, "Could not find existing entity with id " + id);

        beanUtil.easyCopyProperties(existing, clone);

        return repo.save(existing);
    }

}