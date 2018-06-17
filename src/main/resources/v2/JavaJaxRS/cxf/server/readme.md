
# JAX-RS CXF 3 server application

## Supported features
* Bean-Validation-API support
* Spring-configuration of Swagger, WADL and Endpoints
* OpenAPI v2/v3 is accessible (CXF3 Swagger2Feature/OpenApiFeature)
* Swagger-UI can be included as Web-Jar automatically
* WADL is accessible (CXF WADL-Generator)
* Unit-tests include Gzip-Interceptors for demonstration


## Urls to access the REST API
### Urls for Spring Boot

* Available services listing
http://localhost:8080/

* OpenAPI v3
 http://localhost:8080/services/openapi.json

* Swagger API (OpenAPI v2)
 http://localhost:8080/services/swagger.json
 
* CXF WADL
 http://localhost:8080/services?_wadl


### Urls if deployed to an AS 
* Available services listing
http://localhost:8080/swagger-cxf-server/rest/services/

* OpenAPI v3
 http://localhost:8080/swagger-cxf-server/rest/services/openapi.json

* Swagger API (OpenAPI v2)
 http://localhost:8080/swagger-cxf-server/rest/services/swagger.json

* CXF WADL
 http://localhost:8080/swagger-cxf-server/rest/services?_wadl
 
 
