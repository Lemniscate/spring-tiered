# SPRING TIERED
> #### tiered adjective \ˈtird\
> arranged in layers or tiers


### TL;DR
This project generates `Assembler`, `Controller`, `Service` and `Repository` tiers for your application, unless of course you provide your own. Think of it like Rails, Roo, etc... without boxing you into a specific way of life. 

---
## About this Project

Inspired by [Spring Data Rest](https://github.com/spring-projects/spring-data-rest/), but with a focus on a *tiered* architecture centered around your model rather than just Repositories, this project aims to reduce boilerplate logic while providing a strong foundation that is easily replaced by **your** code if / when your application grows. 

The basic premise is that most modern web applications consist (or should consist) of `Controller`, `Service` and `Repository` tiers, many of which contain boilerplate pass through logic to be consistent with the parts of the app that actually require this level of separation. Further more, when you start to introduce translation services on outbound data (for things like HATEOAS or communication protocols) you end up with yet-another tier of similar yet slightly-different logic. 

This library allows you to rapidly develop a RESTful HATEOAS-enabled API by simply adding `@EnableApiResources` to your SpringBoot configuration file and tagging your models with `@ApiResource`. At startup, you application will now generate a `Controller` (and corresponding HATEOAS `Assembler`), `Service` and `Repository` for each tagged model. Your newly generated API will have support for searching, CRUD operations, paging and sorting out of the box. As your logic grows more sophisticated, simply implement your own component by extending a base class and provide the logic that is specific to your domain; the framework will recognize your implementation and use that instead of the default generated component! 


## Getting Started
Getting started is simple: 

##### 1. Add the lemniscate maven snapshot repository to your project's pom.xml:

```
      <repositories>
        <repository>
            <id>lemniscate-repo</id>
            <url>https://github.com/lemniscate/maven-repo/raw/master/releases</url>
        </repository>
        <repository>
            <id>lemniscate-snapshot-repo</id>
            <url>https://github.com/lemniscate/maven-repo/raw/master/snapshots</url>
            <snapshots>
            </snapshots>
        </repository>
      </repositories>
```

##### 2. Annotate a configuration class with `@EnableApiResources` (preferably on an awesome SpringBoot app):

```
@Configuration
@ComponentScan
@EnableApiResources
@EnableAutoConfiguration
@EnableSpringDataWebSupport
public class DemoApplication extends SpringBootServletInitializer {

    /**
     * Entry point for Servlet 3 initialization.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources( getClass() );
    }

    /**
     * Entry point for embedded / command line initialization.
     */
    public static void main(String[] args) {
        SpringApplicationBuilder sb = new SpringApplicationBuilder(DemoApplication.class);
        sb.run(args);
    }
}
```

##### 3. Tag your entities with `@ApiResource`:

```
@Entity
@ApiResource
public class Organization implements Identifiable<Long>{

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "organization", cascade=CascadeType.ALL)
    private List<UserAccount> users = new ArrayList<UserAccount>();
    
    // Getters & Setters
}
```




## References
See [this repo](https://github.com/Lemniscate/boot-stack-demo) for an example implementation. 


## Fine Print
This project is very much in it's infancy, but so far it has proven to be very stable and useful. We welcome comments, criticism, pull requests, warm fuzzy feelings and anything else you'd like to contribute. 




### Authors:
* [David Welch](https://twitter.com/david_welch)
* Your name here!