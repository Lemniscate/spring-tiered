package com.github.lemniscate.lib.tiered.mapping;

import com.github.lemniscate.lib.tiered.controller.ApiResourceBaseController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public  class ApiResourceMapping<E, ID extends Serializable>{
    private final String path;
    private final ApiResourceBaseController controller;
}