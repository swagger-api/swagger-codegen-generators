package io.swagger.codegen.v3.generators.java;

import static io.swagger.codegen.v3.CodegenConstants.HAS_ENUMS_EXT_NAME;
import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

import com.github.jknack.handlebars.Lambda;
import com.google.common.collect.ImmutableMap;
import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.features.BeanValidationFeatures;
import io.swagger.codegen.v3.generators.features.NotNullAnnotationFeatures;
import io.swagger.codegen.v3.generators.handlebars.lambda.UppercaseLambda;
import io.swagger.codegen.v3.utils.URLPathUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaVertXServerCodegen extends AbstractJavaCodegen implements BeanValidationFeatures, NotNullAnnotationFeatures {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaVertXServerCodegen.class);

  private static final String ROOT_PACKAGE = "rootPackage";
  private static final String VERTICLE_PACKAGE = "verticlePackage";
  private static final String SPEC_LOCATION = "openapi.yaml";
  private static final String OPENAPI_EXTENSION = "x-vertx-event-bus";
  private static final String OPENAPI_EXTENSION_ADDRESS = "address";
  private static final String OPENAPI_EXTENSION_METHOD_NAME = "method";
  private static final String TITLE = "title";

  public static final String RX_INTERFACE_OPTION = "rxInterface";
  public static final String USE_DATAOBJECT_OPTION = "useDataObject";
  public static final String MOUNT_OPERATION_FROM_OPTION = "mountOperationFrom";
  public static final String MOUNT_OPERATION_FROM_EXTENSIONS = "mountFromExtensions";
  public static final String MOUNT_OPERATION_FROM_INTERFACE = "mountFromInterface";
  public static final String SPEC_LOCATION_OPTION = "specLocation";

  public static final String USE_FUTURE_OPTION = "useFuture";

  protected String rootPackage = "io.swagger.server.api";
  protected String apiVerticle;
  protected String apiVersion = "1.0.0-SNAPSHOT";

  protected boolean useDataObject = false;
  protected boolean mountFromExtensions = false;
  protected boolean mountFromInterface = false;
  protected String title = null;
  protected boolean useBeanValidation = false;
  protected boolean notNullJacksonAnnotation = false;

  /**
   * A Java Vert.X generator. It can be configured with CLI options :
   *<ul>
   * <li>rxInterface : type Boolean if true, API interfaces are generated with RX and methods return
   * Single and Comparable. default : false</li>
   *
   * <li>useDataObject : type Boolean if true, models objects are generated with @DataObject</li>
   *
   * <li>mountOperationFrom : type String, define how routes are mounted.</li>
   *
   * <li>specLocation : define spec location, default as {@link JavaVertXServerCodegen#SPEC_LOCATION}.</li>
   *
   * <li>useFuture : define use services as future, default false.</li>
   * </ul>
   */
  public JavaVertXServerCodegen() {
    super();

    // set the output folder here
    outputFolder = "generated-code" + File.separator + "javaVertXServer";

    apiPackage = rootPackage + ".service";
    apiVerticle = rootPackage + ".verticle";
    modelPackage = rootPackage + ".model";

    additionalProperties.put(ROOT_PACKAGE, rootPackage);
    additionalProperties.put(VERTICLE_PACKAGE, apiVerticle);

    groupId = "io.swagger";
    artifactId = "swagger-java-vertx-server";
    artifactVersion = apiVersion;

    cliOptions.add(CliOption.newBoolean(RX_INTERFACE_OPTION,
      "When specified, API interfaces are generated with RX "
        + "and methods return Single<> and Comparable."));
    cliOptions.add(CliOption.newBoolean(USE_DATAOBJECT_OPTION,
      "When specified, models objects are generated with @DataObject"));

    // add option to mount with operation id ?
    CliOption operationsOption = CliOption.newString(MOUNT_OPERATION_FROM_OPTION,"When specified, defines how operations are mounted. Default with @WebApiServiceGen");
    Map<String, String> mountOperationFromEnum = new HashMap<>();
    mountOperationFromEnum.put(MOUNT_OPERATION_FROM_EXTENSIONS, "Mount operations from extensions with web-api-service module & @WebApiServiceGen. open api contract must define x-vertx-event-bus extension to be mounted");
    mountOperationFromEnum.put(MOUNT_OPERATION_FROM_INTERFACE, "Mount operations from interface with web-api-service module & Interfaces implementing operations. event bus address will be #{tag}.address");
    operationsOption.setEnum(mountOperationFromEnum);
    operationsOption.setDefault(MOUNT_OPERATION_FROM_EXTENSIONS);
    cliOptions.add(operationsOption);

    CliOption specLocation = CliOption.newString(SPEC_LOCATION_OPTION,
      "When specified, define spec location. Default as " + SPEC_LOCATION);
    specLocation.setDefault(SPEC_LOCATION);
    cliOptions.add(specLocation);

    CliOption useFutureOption = CliOption.newBoolean(USE_FUTURE_OPTION,
      "When specified, describe service as future or not. Default as false");
    useFutureOption.setDefault(Boolean.FALSE.toString());
    cliOptions.add(useFutureOption);

    cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
  }

  /**
   * Configures the type of generator.
   *
   * @return the CodegenType for this generator
   * @see CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.SERVER;
  }

  /**
   * Configures a friendly name for the generator. This will be used by the generator to select
   * the library with the -l flag.
   *
   * @return the friendly name for the generator
   */
  public String getName() {
    return "java-vertx";
  }

  /**
   * Returns human-friendly help for the generator. Provide the consumer with help tips,
   * parameters here
   *
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a java-Vert.X Server library.";
  }

  @Override
  public String getDefaultTemplateDir() {
    return "JavaVertXServer";
  }

  @Override
  public void processOpts() {

    if (dateLibrary.equals("legacy")){
      // can't use legacy format
      LOGGER.warn("Legacy date library could not be used. Replaced to java8 as default");
      setDateLibrary("java8");
    }

    super.processOpts();

    modelTemplateFiles.clear();
    modelTemplateFiles.put("model.mustache", ".java");

    apiTemplateFiles.clear();
    apiTemplateFiles.put("api.mustache", ".java");

    apiTestTemplateFiles.clear();
    modelDocTemplateFiles.clear();
    apiDocTemplateFiles.clear();

    if (additionalProperties.containsKey(USE_DATAOBJECT_OPTION)) {
      this.useDataObject = (Boolean.valueOf(additionalProperties.get(USE_DATAOBJECT_OPTION).toString()));
    }


    if (additionalProperties.containsKey(MOUNT_OPERATION_FROM_OPTION)) {
      if (MOUNT_OPERATION_FROM_INTERFACE.equals(additionalProperties.get(MOUNT_OPERATION_FROM_OPTION))) {
        this.mountFromInterface = true;
        additionalProperties.put(MOUNT_OPERATION_FROM_INTERFACE, true);
      }
    }
    if (!this.mountFromInterface) {
      this.mountFromExtensions = true;
      additionalProperties.put(MOUNT_OPERATION_FROM_EXTENSIONS, true);
    }

    if (!additionalProperties.containsKey(SPEC_LOCATION_OPTION)) {
      additionalProperties.put(SPEC_LOCATION_OPTION, SPEC_LOCATION);
    }

    supportingFiles.clear();
    supportingFiles.add(new SupportingFile("MainApiVerticle.mustache", sourceFolder + File.separator + rootPackage.replace(".", File.separator), "MainApiVerticle.java"));

    supportingFiles.add(new SupportingFile("package-info-service.mustache", sourceFolder + File.separator + apiPackage.replace(".", File.separator), "package-info.java"));

    if (this.useDataObject) {
      supportingFiles.add(new SupportingFile("package-info-model.mustache", sourceFolder + File.separator + modelPackage.replace(".", File.separator), "package-info.java"));
      supportingFiles.add(new SupportingFile("json-mappers.mustache", projectFolder + File.separator + "resources/META-INF/vertx", "json-mappers.properties"));
      supportingFiles.add(new SupportingFile("DataObjectMapper.mustache", sourceFolder + File.separator + modelPackage.replace(".", File.separator), "DataObjectMapper.java"));
    }

    writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));
    writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));

    addHandlebarsLambdas(additionalProperties);
  }

  @Override
  public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
    super.postProcessModelProperty(model, property);

    boolean isEnum = getBooleanValue(model, IS_ENUM_EXT_NAME);
    if (!Boolean.TRUE.equals(isEnum)) {
      model.imports.add("JsonProperty");
      boolean hasEnums = getBooleanValue(model, HAS_ENUMS_EXT_NAME);
      if (Boolean.TRUE.equals(hasEnums)) {
        model.imports.add("JsonValue");
      }
    }

    // not use
    model.imports.remove("Schema");
  }

  @Override
  public CodegenModel fromModel(String name, Schema schema, Map<String, Schema> allSchemas) {
    CodegenModel codegenModel = super.fromModel(name, schema, allSchemas);
    codegenModel.imports.remove("ApiModel");
    codegenModel.imports.remove("ApiModelProperty");
    return codegenModel;
  }

  @Override
  public void preprocessOpenAPI(OpenAPI openAPI) {
    super.preprocessOpenAPI(openAPI);

    final URL urlInfo = URLPathUtil.getServerURL(openAPI);
    String port = "8080";
    if (urlInfo != null && urlInfo.getPort() > 0) {
      port = String.valueOf(urlInfo.getPort());
    }

    this.additionalProperties.put("serverPort", port);

    // From the title, compute a reasonable name for the package and the API
    String title = openAPI.getInfo().getTitle();

    // Drop any API suffix
    title = title.trim().replace(" ", "-");
    if (title.toUpperCase().endsWith("API")) {
      title = title.substring(0, title.length() - 3);
    }

    title = camelize(sanitizeName(title));
    additionalProperties.put(TITLE, title);
    supportingFiles.add(new SupportingFile("apiVerticle.mustache", sourceFolder + File.separator + apiVerticle.replace(".", File.separator), title + "Verticle.java"));

    /*
     * manage operation & custom serviceId because operationId field is not
     * required and may be empty
     */
    Paths paths = openAPI.getPaths();
    if (paths != null) {
      for (Entry<String, PathItem> entry : paths.entrySet()) {
        manageOperations(entry.getValue(), entry.getKey());
      }
    }
    this.additionalProperties.remove("gson");
  }

  public void setUseBeanValidation(boolean useBeanValidation) {
    this.useBeanValidation = useBeanValidation;
  }

  @Override
  public void setNotNullJacksonAnnotation(boolean notNullJacksonAnnotation) {
    this.notNullJacksonAnnotation = notNullJacksonAnnotation;
  }

  @Override
  public boolean isNotNullJacksonAnnotation() {
    return notNullJacksonAnnotation;
  }

  private void addHandlebarsLambdas(Map<String, Object> objs) {
    Map<String, Lambda> lambdas = new ImmutableMap.Builder<String, Lambda>()
      .put("uppercase", new UppercaseLambda())
      .build();

    if (objs.containsKey("lambda")) {
      LOGGER.warn("An property named 'lambda' already exists. Mustache lambdas renamed from 'lambda' to '_lambda'. " +
        "You'll likely need to use a custom template, " +
        "see https://github.com/swagger-api/swagger-codegen#modifying-the-client-library-format. ");
      objs.put("_lambda", lambdas);
    } else {
      objs.put("lambda", lambdas);
    }
  }

  private void manageOperations(PathItem pathItem, String pathname) {
    Map<HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
    if (operationMap != null) {
      for (Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
        String serviceId = null;

        if (this.mountFromExtensions) {
          // read extension "x-vertx-event-bus" to write api service from address, not by tag
          // Cases: from vertx doc
          // 1. both strings or path extension null: operation extension overrides all
          // 2. path extension map and operation extension string: path extension interpreted as delivery options and operation extension as address
          // 3. path extension string and operation extension map: path extension interpreted as address
          // 4. both maps: extension map overrides path map elements
          // 5. operation extension null: path extension overrides all
          Object pathExtension = getExtension(pathItem.getExtensions());
          Object operationExtension = getExtension(entry.getValue().getExtensions());

          String address = null;

          if ((operationExtension instanceof String && pathExtension instanceof String) || pathExtension == null) {
            if (operationExtension instanceof String) {
              address = (String) operationExtension;
            } else if (operationExtension instanceof Map) {
              address = (String) ((Map<String, Object>) operationExtension).get(OPENAPI_EXTENSION_ADDRESS);
              serviceId = (String) ((Map<String, Object>) operationExtension).get(OPENAPI_EXTENSION_METHOD_NAME);
            }
          } else if (operationExtension instanceof String && pathExtension instanceof Map) {
            address = (((Map<?, ?>) pathExtension).containsKey(OPENAPI_EXTENSION_ADDRESS))
              ? (String) ((Map<String, Object>) pathExtension).get(OPENAPI_EXTENSION_ADDRESS)
              : (String) operationExtension;
          } else if (operationExtension instanceof Map && pathExtension instanceof String) {
            address = (String) pathExtension;
            serviceId = (String) ((Map<String, Object>) operationExtension).get(OPENAPI_EXTENSION_METHOD_NAME);
          } else  if (operationExtension instanceof Map && pathExtension instanceof Map) {
            Map<String, Object> busExtension = new LinkedHashMap<>(
              (Map<String, Object>) pathExtension);
            busExtension.putAll((Map<String, Object>) operationExtension);
            address = (String) (busExtension).get(OPENAPI_EXTENSION_ADDRESS);
            serviceId = (String) (busExtension).get(OPENAPI_EXTENSION_METHOD_NAME);
          } else if (operationExtension == null && pathExtension instanceof String) {
            address = (String) pathExtension;
          } else if (operationExtension == null && pathExtension instanceof Map) {
            address = (String) ((Map<String, Object>) pathExtension).get(OPENAPI_EXTENSION_ADDRESS);
            serviceId = (String) ((Map<String, Object>) pathExtension).get(OPENAPI_EXTENSION_METHOD_NAME);
          }

          if (null != address) {
            entry.getValue().addExtension("x-event-bus-address", address);
            // codegen use tag to generate api, so we save tags & replace with event bus address
            entry.getValue().setTags(Collections.singletonList(address));
          } else {
            LOGGER.warn("event bus address not found on operationId {}, will not be mount", entry.getValue().getOperationId());
          }
        }

        if (null == serviceId) {
          serviceId = computeServiceId(pathname, entry);
        }
        entry.getValue().addExtension("x-serviceid", sanitizeOperationId(serviceId));
      }
    }
  }

  private Object getExtension(Map<String, Object> extensions) {
    return null != extensions ? extensions.get(OPENAPI_EXTENSION) : null;
  }

  /**
   * @see io.vertx.ext.web.openapi.impl.OpenAPI3Utils#sanitizeOperationId
   */
  private String sanitizeOperationId(String operationId) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < operationId.length(); i++) {
      char c = operationId.charAt(i);
      if (c == '-' || c == ' ' || c == '_') {
        try {
          while (c == '-' || c == ' ' || c == '_') {
            i++;
            c = operationId.charAt(i);
          }
          result.append(Character.toUpperCase(operationId.charAt(i)));
        } catch (StringIndexOutOfBoundsException e) {
        }
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  private String computeServiceId(String pathname, Entry<HttpMethod, Operation> entry) {
    String operationId = entry.getValue().getOperationId();
    return (operationId != null) ? operationId
      : entry.getKey().name()
        + pathname.replaceAll("-", "_").replaceAll("/", "_").replaceAll("[{}]", "");
  }

}
