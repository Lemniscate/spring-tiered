package com.github.lemniscate.lib.tiered.repo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.Identifiable;

import java.io.Serializable;

public interface Model<ID extends Serializable> extends Serializable, Identifiable<ID>{

    @Getter @Setter
    public static class ModelImpl<ID extends Serializable> implements Model<ID> {
        private ID id;
    }
}
