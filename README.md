# SPRING REST API

This project has similar goals to [Spring Data Rest](https://github.com/spring-projects/spring-data-rest/), but with the focus being on exposing Entities rather than Repositories. 

The basic premise is that most applications consist of `Controller`, `Service` and `Repository` tiers, many of which contain boilerplate pass through logic. This library looks for any entities annotated with `@ApiResource` and generates controllers for the basic CRUD methods, services with appropriate methods, and repositories for each entity (unless a user-defined instance is present).  

## Getting Started
Getting started is simple: annotation a configuration class with `@EnableApiResources` and then tag your entities with `@ApiResource`. 


## References
See [this repo](https://github.com/Lemniscate/boot-stack-demo) for an example implementation. 