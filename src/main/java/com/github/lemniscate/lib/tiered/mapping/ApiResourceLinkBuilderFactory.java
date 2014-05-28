package com.github.lemniscate.lib.tiered.mapping;

import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class ApiResourceLinkBuilderFactory extends ControllerLinkBuilderFactory{

    private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

    @Inject
    private ApiResourceControllerHandlerMapping handlerMapping;

    public ApiResourceLinkBuilder linkTo(Class<?> controller, Class<?> entity, Object... parameters) {

        ApiResourceLinkBuilder builder = new ApiResourceLinkBuilder(getBuilder());
        String mapping = DISCOVERER.getMapping(controller);

        String uri = mapping == null ? "" : mapping;
        uri += handlerMapping.getApiPrefix() + handlerMapping.getPaths().get(entity);

        UriTemplate template = new UriTemplate(uri);

        return builder.slash(template.expand(parameters));
    }

    static UriComponentsBuilder getBuilder() {

        HttpServletRequest request = getCurrentRequest();
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromServletMapping(request);

        String forwardedSsl = request.getHeader("X-Forwarded-Ssl");

        if (StringUtils.hasText(forwardedSsl) && forwardedSsl.equalsIgnoreCase("on")) {
            builder.scheme("https");
        }

        String header = request.getHeader("X-Forwarded-Host");

        if (!StringUtils.hasText(header)) {
            return builder;
        }

        String[] hosts = StringUtils.commaDelimitedListToStringArray(header);
        String hostToUse = hosts[0];

        if (hostToUse.contains(":")) {

            String[] hostAndPort = StringUtils.split(hostToUse, ":");

            builder.host(hostAndPort[0]);
            builder.port(Integer.parseInt(hostAndPort[1]));

        } else {
            builder.host(hostToUse);
        }

        return builder;
    }

    /**
     * Copy of {@link org.springframework.web.servlet.support.ServletUriComponentsBuilder#getCurrentRequest()} until SPR-10110 gets fixed.
     *
     * @return
     */
    @SuppressWarnings("null")
    private static HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
        HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
        return servletRequest;
    }
}