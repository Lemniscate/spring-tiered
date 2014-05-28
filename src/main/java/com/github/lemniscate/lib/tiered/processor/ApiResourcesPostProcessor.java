package com.github.lemniscate.lib.tiered.processor;

import com.github.lemniscate.lib.tiered.annotation.ApiResourceDetails;
import com.github.lemniscate.lib.tiered.controller.ApiResourceController;
import com.github.lemniscate.lib.tiered.controller.ApiResourceNestedCollectionController;
import com.github.lemniscate.lib.tiered.controller.ApiResourceNestedPropertyController;
import com.github.lemniscate.lib.tiered.mapping.ApiResourceAssembler;
import com.github.lemniscate.lib.tiered.mapping.ApiNestedResourceAssembler;
import com.github.lemniscate.lib.tiered.mapping.ApiResourceMapping;
import com.github.lemniscate.lib.tiered.repo.ApiResourceRepository;
import com.github.lemniscate.lib.tiered.svc.ApiResourceService;
import com.github.lemniscate.lib.tiered.util.ApiResourceUtil;
import com.github.lemniscate.util.bytecode.JavassistUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.PriorityOrdered;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class ApiResourcesPostProcessor implements
        BeanDefinitionRegistryPostProcessor,
        InitializingBean,
        PriorityOrdered{

    private final String basePackage;
    private Set<Class<?>> entities;
    private Map<Class<?>, Deets> map = new HashMap<Class<?>, Deets>();


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        populateMap(registry);
        doIt(registry);
    }

    @SneakyThrows
    public void doIt(BeanDefinitionRegistry registry){
        for( Class<?> entity : entities ){
            ApiResourceDetails wrapper = ApiResourceDetails.from(entity);
            Deets details = map.get(entity);



            if( details.service == null){
                Class<?> serviceClass = JavassistUtil.generateTypedSubclass(wrapper.getName() + "Service", ApiResourceService.class, wrapper.getDomainClass(), wrapper.getIdClass());

                AbstractBeanDefinition def = BeanDefinitionBuilder.rootBeanDefinition(serviceClass)
                        .addPropertyValue("domainClass", entity)
                        .addPropertyValue("idClass", wrapper.getIdClass())
                        .getBeanDefinition();
                registry.registerBeanDefinition( wrapper.getName() + "Service", def);

            }else{
                log.info("Found user defined service for {}", entity.getSimpleName());
            }

            if( details.repository == null ){
                Class<?> repoClass = JavassistUtil.generateTypedInterface( wrapper.getName() + "Repository", ApiResourceRepository.class, wrapper.getDomainClass(), wrapper.getIdClass());

                AbstractBeanDefinition def = BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
                        .addPropertyValue("repositoryInterface", repoClass)
                        .getBeanDefinition();
                registry.registerBeanDefinition( wrapper.getName() + "Repository", def);
            }else{
                log.info("Found user defined repository for {}", entity.getSimpleName());
            }

            if( details.controller == null ){
                String name = wrapper.getName() + "Controller";
                Class<?> serviceClass;
                if( wrapper.isNested() ){
                    Class<?> baseClass = wrapper.isNestedCollection()
                            ? ApiResourceNestedCollectionController.class
                            : ApiResourceNestedPropertyController.class;

                    serviceClass = JavassistUtil.generateTypedSubclass(name, baseClass, wrapper.getDomainClass(), wrapper.getIdClass(), wrapper.getDomainClass(), wrapper.getParentClass());
                }else{
                    serviceClass = JavassistUtil.generateTypedSubclass(name, ApiResourceController.class, wrapper.getDomainClass(), wrapper.getIdClass(), wrapper.getDomainClass());
                }

                AbstractBeanDefinition def = BeanDefinitionBuilder.rootBeanDefinition(serviceClass)
                        .addConstructorArgValue(wrapper)
                        .getBeanDefinition();
                registry.registerBeanDefinition( name, def);
                details.controller = def;
            }else{
                log.info("Found user defined controller for {}", entity.getSimpleName());
                String name = wrapper.getName() + "Controller";
                registry.registerBeanDefinition(name, details.controller);
            }

            if( details.assembler == null){

                String name = wrapper.getName() + "Assembler";
                Class<?> serviceClass;
                if( wrapper.isNested()){
                    serviceClass = JavassistUtil.generateTypedSubclass(name, ApiNestedResourceAssembler.class, wrapper.getDomainClass(), wrapper.getIdClass(), wrapper.getBeanClass(), wrapper.getParentClass());
                }else{
                    serviceClass = JavassistUtil.generateTypedSubclass(name, ApiResourceAssembler.class, wrapper.getDomainClass(), wrapper.getIdClass(), wrapper.getBeanClass());
                }

                AbstractBeanDefinition def = BeanDefinitionBuilder.rootBeanDefinition(serviceClass)
                        .getBeanDefinition();
                registry.registerBeanDefinition( name, def);
                details.assembler = def;
            }else{
                log.info("Found user defined assembler for {}", entity.getSimpleName());
            }

            AbstractBeanDefinition def = BeanDefinitionBuilder.rootBeanDefinition(ApiResourceMapping.class)
                        .addConstructorArgValue(wrapper.getPath())
                        .addConstructorArgReference(wrapper.getName() + "Controller")
                    .getBeanDefinition();
            registry.registerBeanDefinition( wrapper.getName() + "Mapping", def);
        }
    }


    // TODO this needs a lot of love!
    @SneakyThrows
    public void populateMap(BeanDefinitionRegistry registry){
        for( String name : registry.getBeanDefinitionNames() ){
            BeanDefinition d = registry.getBeanDefinition(name);

            if( d instanceof AbstractBeanDefinition ){
                AbstractBeanDefinition def = (AbstractBeanDefinition) d;

                if( isBeanType(def, ApiResourceController.class)){
                    Class<?> entity = getEntityType(def, ApiResourceService.class);
                    map.get(entity).service = def;
                }

                if( isBeanType( def, ApiResourceAssembler.class )){
                    Class<?> entity = getEntityType(def, ApiResourceAssembler.class);
                    map.get(entity).assembler = def;
                }else if( isBeanType( def, ApiResourceNestedCollectionController.class )){
                    Class<?> entity = getEntityType(def, ApiResourceNestedCollectionController.class);
                    map.get(entity).assembler = def;
                }else if( isBeanType( def, ApiResourceNestedPropertyController.class )){
                    Class<?> entity = getEntityType(def, ApiResourceNestedPropertyController.class);
                    map.get(entity).assembler = def;
                }

                if( isBeanType(def, ApiResourceService.class)){
                    Class<?> entity = getEntityType(def, ApiResourceService.class);
                    map.get(entity).service = def;
                }


                if( isBeanType(def, JpaRepositoryFactoryBean.class)){
                    String repoName = (String) def.getPropertyValues().get("repositoryInterface");
                    Class<?> entity = GenericTypeResolver.resolveTypeArguments( Class.forName(repoName), ApiResourceRepository.class)[0];
                    map.get(entity).repository = def;
                }
            }

        }
    }

    @SneakyThrows
    private boolean isBeanType( AbstractBeanDefinition beanDef, Class<?> targetType){
        if( beanDef.getBeanClassName() != null ){
            Class<?> beanClass = Class.forName( beanDef.getBeanClassName() );
            return targetType.isAssignableFrom( beanClass );
        }
        return false;
    }

    @SneakyThrows
    private Class<?> getEntityType(AbstractBeanDefinition def, Class<?> gen){
        Class<?> c = Class.forName(def.getBeanClassName());
        Class<?>[] types = GenericTypeResolver.resolveTypeArguments(c, gen);
        if( types != null && types.length >= 1 ){
            return types[0];
        }

        MutablePropertyValues pv = def.getPropertyValues();
        Object propValue = pv.get("domainClass");
        if( propValue != null && Class.class.isAssignableFrom(propValue.getClass()) ){
            return (Class<?>) propValue;
        }

        throw new IllegalStateException("Couldn't determine entity type");
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}

    @Override
    public void afterPropertiesSet() throws Exception {
        entities = ApiResourceUtil.getAllTaggedClasses(basePackage);
        for( Class<?> entity : entities ){
            map.put(entity, new Deets());
        }
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.LOWEST_PRECEDENCE - 3;
    }

    private class Deets {
        AbstractBeanDefinition repository, service, controller, assembler;
    }

}
