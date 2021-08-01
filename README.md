## User process management service

##### The main purpose of the user process management service is to provide REST API low-code platform:

* `The application brings the following functionality:`
    * access to the list of user business processes based on roles and permissions;
    * initiation of business processes.

##### Spring Actuator configured with Micrometer extension for exporting data in prometheus-compatible format.
*End-point:* <service>:<port>/actuator/prometheus

*Prometheus configuration example (prometheus.yml):*

```
global:
  scrape_interval: 10s
scrape_configs:
  - job_name: 'spring_micrometer'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['< service >:< port >']
```

##### Spring Sleuth configured for Istio http headers propagation:

- x-access-token
- x-request-id
- x-b3-traceid
- x-b3-spanid
- x-b3-parentspanid
- x-b3-sampled
- x-b3-flags
- b3

##### Running the tests:

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### Local development

1. `application-local.yml` is configuration file for local development;
2. to interact with `business process management` service, set `bpms.url` variable as environment
   variable or specify it in the configuration file:
    * by default http://localhost:8090;
3. logging settings (*level,pattern,output file*) specified in the configuration file;
4. run spring boot application using 'local' profile:
    * `mvn spring-boot:run -Drun.profiles=local` OR using appropriate functions of your IDE;
5. the application will be available on: http://localhost:8080/user-process-management/swagger

##### Logging:

* `Default:`
    * For classes with annotation RestController/Service, logging is enabled by default for all
      public methods of a class;
* `To set up logging:`
    * *@Logging* - can annotate a class or method to enable logging;
    * *@Confidential* - can annotate method or method parameters to exclude confidential data from
      logs:
        - For a method - exclude the result of execution;
        - For method parameters - exclude method parameters;