package com.github.lemniscate.lib.rest.repo.specification.specification;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


@RequiredArgsConstructor
public class GenericMapPathSpecification<E> implements Specification<E> {

    private final MultiValueMap<String, String> params;
    private final ConversionService conversionService;

    @Override
    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<Predicate>();
        for(String key : params.keySet()){
            String[] keys = key.split("\\.");
            Path<Object> prop = root.get(keys[0]);

            // if there's multiple paths (booking.address.city), traverse the properties
            if( keys.length > 1 ){
                Iterator<String> itr = Arrays.asList(keys).iterator();
                itr.next();
                while(itr.hasNext()){
                    prop = prop.get(itr.next());
                }
            }

            // convert our String parameters to their actual requested types
            List<String> values = params.get(key);
            List<Object> typedValues = new ArrayList<Object>();
            for( String value : values){
                Object typedValue = conversionService.convert( value, prop.getJavaType() );
                typedValues.add(typedValue);
            }

            // create our actual predicate
            Predicate pred;
            if( values.size() == 1){
                pred = cb.equal(prop, typedValues.get(0));
            }else{
                pred = cb.in( prop.in(typedValues) );
            }
            predicates.add(pred);
        }

        Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
        return cb.and(predicatesArray);
    }
}
