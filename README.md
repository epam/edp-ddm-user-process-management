## User process management service

### Overview

* The main purpose of the user process management service is to provide REST API low-code platform
  to interact with process instances/definitions;
* access to the list of user business processes based on roles and permissions;
* initiation of business processes.

### Usage

#### Prerequisites:

* Ceph-storage is configured and running;
* business-process-management service is configured and running;
* form-management-provider service is configured and running.

#### Configuration

Available properties are following:

* `bpms.url` - business process management service base url;
* `form-management-provider.url` - form management service base url;
* `ceph.http-endpoint` - ceph base url;
* `ceph.access-key` - ceph access key;
* `ceph.secret-key` - ceph secret key;
* `ceph.bucket` - ceph bucket name.

#### Run application:

* `java -jar <file-name>.jar`

### Local development

1. Run spring boot application using 'local' profile:
    * `mvn spring-boot:run -Drun.profiles=local` OR using appropriate functions of your IDE;
    * `application-local.yml` - configuration file for local profile.
2. The application will be available on: http://localhost:8085/user-process-management/swagger

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.
    
### License

The user-process-management is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).