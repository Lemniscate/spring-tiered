package com.github.lemniscate.lib.rest.mapping;

import com.github.lemniscate.lib.rest.controller.ApiResourceBaseController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public  class ApiResourceMapping<E, ID extends Serializable>{
    private final String path;
    private final ApiResourceBaseController controller;
}