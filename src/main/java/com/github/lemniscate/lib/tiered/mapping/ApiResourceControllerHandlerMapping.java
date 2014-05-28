package com.github.lemniscate.lib.tiered.mapping;

import com.github.lemniscate.lib.tiered.controller.ApiResourceBaseController;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApiResourceControllerHandlerMapping extends RequestMappingHandlerMapping implements
        ApplicationContextAware {

    private Collection<? extends ApiResourceMapping> endpoints;

    @Getter
    private Map<Class<?>, String> paths = new HashMap<Class<?>, String>();

    @Getter
    private final String apiPrefix;

    @Setter @Getter
    private boolean disabled = false;

    public ApiResourceControllerHandlerMapping(String apiPrefix){
        this.apiPrefix = apiPrefix;
        setOrder(LOWEST_PRECEDENCE - 2);

    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.endpoints = getApplicationContext().getBeansOfType(ApiResourceMapping.class).values();

        if (!this.disabled) {
            for (ApiResourceMapping endpoint : this.endpoints) {
                detectHandlerMethods(endpoint.getController());
            }
        }
    }

    /**
     * Since all handler beans are passed into the constructor there is no need to detect
     * anything here
     */
    @Override
    protected boolean isHandler(Class<?> beanType) {
        return false;
    }

    @Override
    protected void registerHandlerMethod(Object handler, Method method,
                                         RequestMappingInfo mapping) {

        if (mapping == null) {
            return;
        }

        Set<String> defaultPatterns = mapping.getPatternsCondition().getPatterns();
        String[] patterns = new String[defaultPatterns.isEmpty() ? 1 : defaultPatterns.size()];

        String path = "";
        Object bean = handler;
        if (bean instanceof String) {
            bean = getApplicationContext().getBean((String) handler);
        }
        if (bean instanceof ApiResourceBaseController) {
            ApiResourceBaseController endpoint = (ApiResourceBaseController) bean;
            path = endpoint.getResource().getPath();
            paths.put( endpoint.getResource().getDomainClass(), path );
        }

        int i = 0;
        String prefix = StringUtils.hasText(this.apiPrefix) ? this.apiPrefix + path : path;
        if (defaultPatterns.isEmpty()) {
            patterns[0] = prefix;
        }
        else {
            for (String pattern : defaultPatterns) {
                patterns[i] = prefix + pattern;
                i++;
            }
        }
        PatternsRequestCondition patternsInfo = new PatternsRequestCondition(patterns);

        RequestMappingInfo modified = new RequestMappingInfo(patternsInfo,
                mapping.getMethodsCondition(), mapping.getParamsCondition(),
                mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                mapping.getProducesCondition(), mapping.getCustomCondition());

        super.registerHandlerMethod(handler, method, modified);
    }

}
