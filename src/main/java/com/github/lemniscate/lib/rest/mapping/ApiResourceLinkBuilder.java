package com.github.lemniscate.lib.rest.mapping;

import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.web.util.UriComponentsBuilder;

public class ApiResourceLinkBuilder extends LinkBuilderSupport<ApiResourceLinkBuilder> {

    /**
     * Creates a new {@link ApiResourceLinkBuilder} using the given {@link org.springframework.web.util.UriComponentsBuilder}.
     *
     * @param builder must not be {@literal null}.
     */
    ApiResourceLinkBuilder(UriComponentsBuilder builder) {
        super(builder);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.hateoas.UriComponentsLinkBuilder#getThis()
     */
    @Override
    protected ApiResourceLinkBuilder getThis() {
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.hateoas.UriComponentsLinkBuilder#createNewInstance(org.springframework.web.util.UriComponentsBuilder)
     */
    @Override
    protected ApiResourceLinkBuilder createNewInstance(UriComponentsBuilder builder) {
        return new ApiResourceLinkBuilder(builder);
    }

}
