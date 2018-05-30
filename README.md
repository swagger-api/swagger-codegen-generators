# swagger-codegen-generators

- Master: [![Build Status](https://img.shields.io/travis/swagger-api/swagger-codegen-generators/master.svg?label=Build%20Status)](https://travis-ci.org/swagger-api/swagger-codegen-generators)

## Overview
**Swagger codegen generator** project is a set of classes and templates ([Handlebars](https://jknack.github.io/handlebars.java)) used by [swagger codegen 3.0.0 project](https://github.com/swagger-api/swagger-codegen/tree/3.0.0) in its code generation process for a specific language or language framework. The main differents with **swagger codegen 2.x.x** are:

- **Handlebars as template engine:** with Handelbars feature is possible to create more logic-less templates.
- **OAS 3 support:** generator classes work with OpenAPI Specificacion V3.

More details about these and more differences are referenced at [https://github.com/swagger-api/swagger-codegen/releases/tag/v3.0.0-SNAPSHOT](https://github.com/swagger-api/swagger-codegen/releases/tag/v3.0.0-SNAPSHOT)

### Prerequisites
You need the following installed and available in your $PATH:

* Java 8 (http://java.oracle.com)
* Apache maven 3.0.4 or greater (http://maven.apache.org/)

## How to contribute.
Right now the templates and generators classes are migrated from  [swagger codegen](https://github.com/swagger-api/swagger-codegen) **3.0.0** branch. 
If you want to migrate an existing language/framework, you can follow this [guide](https://github.com/swagger-api/swagger-codegen/wiki/Swagger-Codegen-migration-(swagger-codegen-generators-repository)).
Also you need to keep in mind that **Handlebars** is used as template engines and besides it's pretty similar to **Mustache** there are different that can not be ignored. So you can follow this [guide](https://github.com/swagger-api/swagger-codegen/wiki/Swagger-Codegen-migration-from-Mustache-and-Handlebars-templates.) which explains steps to migrate templates from **Mustaches** to **Handelbars**.
 
License
-------
 
Copyright 2018 SmartBear Software

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 
---
<img src="http://swagger.io/wp-content/uploads/2016/02/logo.jpg"/>