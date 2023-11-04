# <img src="https://raw.githubusercontent.com/swagger-api/swagger.io/wordpress/images/assets/SWC-logo-clr.png" height="80">

- Master: [![Build Status](https://img.shields.io/jenkins/build.svg?jobUrl=https://jenkins.swagger.io/job/oss-swagger-codegen-generators-master-java-8)](https://jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-codegen-generators-master-java-8)

[![Build Status](https://jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-codegen-generators-master-java-8/badge/icon?subject=jenkins%20build%20-%20master)](https://jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-codegen-generators-master-java-8/)

## Overview
**Swagger Codegen Generators** project is a set of classes and templates ([Handlebars](https://jknack.github.io/handlebars.java)) used by [Swagger Codegen 3.0.0 project](https://github.com/swagger-api/swagger-codegen/tree/3.0.0) in its code generation process for a specific language or language framework. The main differents with **Swagger Codegen 2.x.x** are:

- **Handlebars as template engine:** with Handlebars feature is possible to create more logic-less templates.
- **OAS 3 support:** generator classes work with OpenAPI Specification V3.

More details about these and more differences are referenced at [https://github.com/swagger-api/swagger-codegen/releases/tag/v3.0.0](https://github.com/swagger-api/swagger-codegen/releases/tag/v3.0.0)

### Prerequisites
You need the following installed and available in your $PATH:

* Java 8 (http://java.oracle.com)
* Apache maven 3.0.4 or greater (http://maven.apache.org/)

## How to Contribute.
Right now the templates and generators classes are migrated from  [Swagger Codegen](https://github.com/swagger-api/swagger-codegen) **3.0.0** branch.
If you want to migrate an existing language/framework, you can follow this [guide](https://github.com/swagger-api/swagger-codegen/wiki/Swagger-Codegen-migration-(swagger-codegen-generators-repository)).
Also you need to keep in mind that **Handlebars** is used as the template engine. It's pretty similar to **Mustache**, but there are differences that can not be ignored. So you can follow this [guide](https://github.com/swagger-api/swagger-codegen/wiki/Swagger-Codegen-migration-from-Mustache-and-Handlebars-templates.) which explains steps to migrate templates from **Mustaches** to **Handlebars**.

## Security Contact

Please disclose any security-related issues or vulnerabilities by emailing [security@swagger.io](mailto:security@swagger.io), instead of using the public issue tracker.

## License Information on Generated Code

The Swagger Codegen project is intended as a benefit for users of the Swagger / Open API Specification.  The project itself has the [License](#license) as specified.  In addition, please understand the following points:

* The templates included with this project are subject to the [License](#license).
* Generated code is intentionally _not_ subject to the parent project license

When code is generated from this project, it shall be considered **AS IS** and owned by the user of the software.  There are no warranties--expressed or implied--for generated code.  You can do what you wish with it, and once generated, the code is your responsibility and subject to the licensing terms that you deem appropriate.
