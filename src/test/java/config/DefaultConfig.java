package config;

import demo.model.User;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.dialect.HSQLDialect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * @Author dave 5/4/14 11:34 AM
 */
@Configuration
@EnableJpaRepositories
@EnableEntityLinks
public class DefaultConfig {

    @Bean
    public InitializingBean dataPopulator(){
        return new InitializingBean(){

            @Autowired
            private PlatformTransactionManager txMgr;

            @Autowired
            private EntityManagerFactory emf;

            @Override
            public void afterPropertiesSet() throws Exception {
                TransactionStatus tx = txMgr.getTransaction(new DefaultTransactionDefinition());
                EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(emf);
                for(int i = 0; i < 5; i++){
                    User user = new User();
                    user.setName("User " + i);
                    em.persist(user);
                }
                em.flush();
                txMgr.commit(tx);
            }
        };
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
//                 .addScript("classpath:test-data.sql")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaVendorAdapter vendorAdapter){
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(User.class.getPackage().getName());
        factory.setDataSource(dataSource);
        factory.setJpaProperties(getJpaProperties());
        return factory;
    }

    @Bean
    public HibernateJpaVendorAdapter jpaVendorAdapter(){
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setDatabasePlatform(HSQLDialect.class.getName());
        vendorAdapter.setShowSql(true);
        return vendorAdapter;
    }

    private Properties getJpaProperties() {
        Properties props = new Properties();
        props.put("hibernate.ejb.naming_strategy", ImprovedNamingStrategy.class.getName());
        props.put("hibernate.enable_lazy_load_no_trans", "true");
        return props;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean bean){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(bean.getObject());
        return transactionManager;
    }

    @Bean
    public ConversionService conversionService(){
        return new DefaultConversionService();
    }
}
