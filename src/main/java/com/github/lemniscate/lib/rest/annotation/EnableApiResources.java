package com.github.lemniscate.lib.rest.annotation;

import com.github.lemniscate.lib.rest.mapping.ApiResourceControllerHandlerMapping;
import com.github.lemniscate.lib.rest.mapping.ApiResourceEntityLinks;
import com.github.lemniscate.lib.rest.mapping.ApiResourceLinkBuilderFactory;
import com.github.lemniscate.lib.rest.processor.ApiResourcesPostProcessor;
import com.github.lemniscate.lib.rest.util.EntityAwareBeanUtil;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableApiResources.ApiResourceRegistrar.class)
public @interface EnableApiResources {
    Class<?> value() default EnableApiResources.class;


    public class ApiResourceRegistrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes attr = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableApiResources.class.getName(), false));

            Class<?> value = (Class<?>) attr.get("value");

            // if the value class was the default (EnableApiResources), use the calling class' package
            if( EnableApiResources.class.equals(value)){
                value = ((StandardAnnotationMetadata) metadata).getIntrospectedClass();
            }

            // register the annotation post processor
            String basePackage = value.getPackage().getName();

            AbstractBeanDefinition def = BeanDefinitionBuilder.rootBeanDefinition(ApiResourcesPostProcessor.class)
                    .addConstructorArgValue(basePackage)
                    .getBeanDefinition();
            registry.registerBeanDefinition("apiResourcesPostProcessor", def);
            registry.registerBeanDefinition("apiResourcesHandlerMapping", new RootBeanDefinition(ApiResourceControllerHandlerMapping.class));
            registry.registerBeanDefinition("apiResourcesLinkBuilderFactory", new RootBeanDefinition(ApiResourceLinkBuilderFactory.class));
            registry.registerBeanDefinition("entityAwareBeanUtil", new RootBeanDefinition(EntityAwareBeanUtil.class));
            registry.registerBeanDefinition("apiResourcesEntityLinks", new RootBeanDefinition(ApiResourceEntityLinks.class));
        }
    }
}
