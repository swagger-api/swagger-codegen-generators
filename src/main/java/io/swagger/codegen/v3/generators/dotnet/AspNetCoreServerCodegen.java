package io.swagger.codegen.v3.generators.dotnet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.samskivert.mustache.Mustache;
import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenContent;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenSecurity;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.handlebars.ExtensionHelper;
import io.swagger.codegen.v3.utils.URLPathUtil;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static java.util.UUID.randomUUID;

public class AspNetCoreServerCodegen extends AbstractCSharpCodegen {

    private String packageGuid = "{" + randomUUID().toString().toUpperCase() + "}";
    private static final String ASP_NET_CORE_VERSION_OPTION = "--aspnet-core-version";
    private static final String INTERFACE_ONLY_OPTION = "--interface-only";
    private static final String INTERFACE_CONTROLLER_OPTION = "--interface-controller";
    private final String DEFAULT_ASP_NET_CORE_VERSION = "2.2";
    private String aspNetCoreVersion;

    @SuppressWarnings("hiding")
    protected Logger LOGGER = LoggerFactory.getLogger(AspNetCoreServerCodegen.class);

    public AspNetCoreServerCodegen() {
        super();

        setSourceFolder("src");
        outputFolder = "generated-code" + File.separator + this.getName();

        // contextually reserved words
        // NOTE: C# uses camel cased reserved words, while models are title cased. We don't want lowercase comparisons.
        reservedWords.addAll(
            Arrays.asList("var", "async", "await", "dynamic", "yield")
        );

        cliOptions.clear();

        // CLI options
        addOption(CodegenConstants.PACKAGE_NAME,
                "C# package name (convention: Title.Case).",
                this.packageName);

        addOption(CodegenConstants.PACKAGE_VERSION,
                "C# package version.",
                this.packageVersion);

        addOption(CodegenConstants.OPTIONAL_PROJECT_GUID,
                CodegenConstants.OPTIONAL_PROJECT_GUID_DESC,
                null);

        addOption(CodegenConstants.SOURCE_FOLDER,
                CodegenConstants.SOURCE_FOLDER_DESC,
                sourceFolder);

        addOption(CodegenConstants.PRESERVE_COMMENT_NEWLINES,
                "Preserve newlines in comments",
                String.valueOf(this.preserveNewLines));

        // CLI Switches
        addSwitch(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG,
                CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG_DESC,
                this.sortParamsByRequiredFlag);

        addSwitch(CodegenConstants.USE_DATETIME_OFFSET,
                CodegenConstants.USE_DATETIME_OFFSET_DESC,
                this.useDateTimeOffsetFlag);

        addSwitch(CodegenConstants.USE_COLLECTION,
                CodegenConstants.USE_COLLECTION_DESC,
                this.useCollection);

        addSwitch(CodegenConstants.RETURN_ICOLLECTION,
                CodegenConstants.RETURN_ICOLLECTION_DESC,
                this.returnICollection);

        this.aspNetCoreVersion = DEFAULT_ASP_NET_CORE_VERSION;

        addSwitch(INTERFACE_ONLY_OPTION.substring(2),
            "Only generate interfaces for controllers",
            false);

        addSwitch(INTERFACE_CONTROLLER_OPTION.substring(2),
            "Generate interfaces for controllers, implemented by a default controller implementation",
            false);

        addOption(ASP_NET_CORE_VERSION_OPTION.substring(2),
            "ASP.NET Core version",
            DEFAULT_ASP_NET_CORE_VERSION);

    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "aspnetcore";
    }

    @Override
    public String getHelp() {
        return "Generates an ASP.NET Core Web API server.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        setAspNetCoreVersion();

        modelTemplateFiles.put("model.mustache", ".cs");


        if (additionalProperties.containsKey(CodegenConstants.OPTIONAL_PROJECT_GUID)) {
            setPackageGuid((String) additionalProperties.get(CodegenConstants.OPTIONAL_PROJECT_GUID));
        }
        additionalProperties.put("packageGuid", packageGuid);

        additionalProperties.put("dockerTag", this.packageName.toLowerCase());

        additionalProperties.put("aspNetCoreVersion", aspNetCoreVersion);

        String packageFolder = sourceFolder + File.separator + packageName;

        if (aspNetCoreVersion.equals("2.0")) {
            apiTemplateFiles.put("controller.mustache", ".cs");
            addInterfaceControllerTemplate();

            supportingFiles.add(new SupportingFile("Program.mustache", packageFolder, "Program.cs"));
            supportingFiles.add(new SupportingFile("Project.csproj.mustache", packageFolder, this.packageName + ".csproj"));
            supportingFiles.add(new SupportingFile("Dockerfile.mustache", packageFolder, "Dockerfile"));
        } else{
            apiTemplateFiles.put("2.1/controller.mustache", ".cs");
            addInterfaceControllerTemplate();

            supportingFiles.add(new SupportingFile("2.1/Program.mustache", packageFolder, "Program.cs"));
            supportingFiles.add(new SupportingFile("2.1/Project.csproj.mustache", packageFolder, this.packageName + ".csproj"));
            supportingFiles.add(new SupportingFile("2.1/Dockerfile.mustache", packageFolder, "Dockerfile"));
        }

        if (!additionalProperties.containsKey(CodegenConstants.API_PACKAGE)) {
            apiPackage = packageName + ".Controllers";
            additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);
        }

        if (!additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE)) {
            modelPackage = packageName + ".Models";
            additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        }

        supportingFiles.add(new SupportingFile("NuGet.Config", "", "NuGet.Config"));
        supportingFiles.add(new SupportingFile("build.sh.mustache", "", "build.sh"));
        supportingFiles.add(new SupportingFile("build.bat.mustache", "", "build.bat"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("Solution.mustache", "", this.packageName + ".sln"));
        supportingFiles.add(new SupportingFile("gitignore", packageFolder, ".gitignore"));
        supportingFiles.add(new SupportingFile("appsettings.json", packageFolder, "appsettings.json"));

        supportingFiles.add(new SupportingFile("Startup.mustache", packageFolder, "Startup.cs"));

        supportingFiles.add(new SupportingFile("validateModel.mustache", packageFolder + File.separator + "Attributes", "ValidateModelStateAttribute.cs"));
        supportingFiles.add(new SupportingFile("web.config", packageFolder, "web.config"));

        supportingFiles.add(new SupportingFile("Properties" + File.separator + "launchSettings.json", packageFolder + File.separator + "Properties", "launchSettings.json"));

        supportingFiles.add(new SupportingFile("Filters" + File.separator + "BasePathFilter.mustache", packageFolder + File.separator + "Filters", "BasePathFilter.cs"));
        supportingFiles.add(new SupportingFile("Filters" + File.separator + "GeneratePathParamsValidationFilter.mustache", packageFolder + File.separator + "Filters", "GeneratePathParamsValidationFilter.cs"));

        supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "README.md", packageFolder + File.separator + "wwwroot", "README.md"));
        supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "index.html", packageFolder + File.separator + "wwwroot", "index.html"));
        supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "web.config", packageFolder + File.separator + "wwwroot", "web.config"));

        supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "swagger-original.mustache", packageFolder + File.separator + "wwwroot", "swagger-original.json"));
    }

    @Override
    public void setSourceFolder(final String sourceFolder) {
        if(sourceFolder == null) {
            LOGGER.warn("No sourceFolder specified, using default");
            this.sourceFolder =  "src" + File.separator + this.packageName;
        } else if(!sourceFolder.equals("src") && !sourceFolder.startsWith("src")) {
            LOGGER.warn("ASP.NET Core requires source code exists under src. Adjusting.");
            this.sourceFolder =  "src" + File.separator + sourceFolder;
        } else {
            this.sourceFolder = sourceFolder;
        }
    }

    public void setPackageGuid(String packageGuid) {
        this.packageGuid = packageGuid;
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + packageName + File.separator + "Controllers";
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        boolean isInterface = templateName.equalsIgnoreCase("icontroller.mustache");
        String suffix = apiTemplateFiles().get(templateName);
        if (isInterface) {
            return apiFileFolder() + "/I" + toApiFilename(tag) + suffix;
        }
        return apiFileFolder() + '/' + toApiFilename(tag) + suffix;
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + packageName + File.separator  + "Models";
    }


    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        OpenAPI openAPI = (OpenAPI)objs.get("openAPI");
        if(openAPI != null) {
            try {
                objs.put("swagger-json", Json.pretty().writeValueAsString(openAPI).replace("\r\n", "\n"));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return super.postProcessSupportingFileData(objs);
    }


    @Override
    protected void processOperation(CodegenOperation operation) {
        super.processOperation(operation);

        // HACK: Unlikely in the wild, but we need to clean operation paths for MVC Routing
        if (operation.path != null) {
            String original = operation.path;
            operation.path = operation.path.replace("?", "/");
            if (!original.equals(operation.path)) {
                LOGGER.warn("Normalized " + original + " to " + operation.path + ". Please verify generated source.");
            }
        }

        // Converts, for example, PUT to HttpPut for controller attributes
        operation.httpMethod = "Http" + operation.httpMethod.substring(0, 1) + operation.httpMethod.substring(1).toLowerCase();

        if (operation.getContents() != null && !operation.getContents().isEmpty()) {
            List <CodegenContent> contents = operation.getContents()
                    .stream()
                    .filter(codegenContent -> !codegenContent.getIsForm())
                    .collect(Collectors.toList());
            operation.getContents().clear();
            operation.getContents().addAll(contents);
        }
    }

    @Override
    public Mustache.Compiler processCompiler(Mustache.Compiler compiler) {
        // To avoid unexpected behaviors when options are passed programmatically such as { "useCollection": "" }
        return super.processCompiler(compiler).emptyStringIsFalse(true);
    }

    @Override
    public List<CodegenSecurity> fromSecurity(Map<String, SecurityScheme> securitySchemeMap) {
        final List<CodegenSecurity> securities = super.fromSecurity(securitySchemeMap);
        if (securities == null || securities.isEmpty()) {
            return securities;
        }
        boolean hasBasic = false;
        boolean hasBearer = false;
        boolean hasApiKey = false;
        for (int index = 0; index < securities.size(); index++) {
            final CodegenSecurity codegenSecurity = securities.get(index);
            if (getBooleanValue(codegenSecurity, CodegenConstants.IS_BASIC_EXT_NAME)) {
                hasBasic = true;
            }
            if (getBooleanValue(codegenSecurity, CodegenConstants.IS_BEARER_EXT_NAME)) {
                hasBearer = true;
            }
            if (getBooleanValue(codegenSecurity, CodegenConstants.IS_API_KEY_EXT_NAME)) {
                hasApiKey = true;
            }
        }
        final String packageFolder = sourceFolder + File.separator + packageName;
        if (hasBasic) {
            supportingFiles.add(new SupportingFile("Security/BasicAuthenticationHandler.mustache", packageFolder + File.separator + "Security", "BasicAuthenticationHandler.cs"));
        }
        if (hasBearer) {
            supportingFiles.add(new SupportingFile("Security/BearerAuthenticationHandler.mustache", packageFolder + File.separator + "Security", "BearerAuthenticationHandler.cs"));
        }
        if (hasApiKey) {
            supportingFiles.add(new SupportingFile("Security/ApiKeyAuthenticationHandler.mustache", packageFolder + File.separator + "Security", "ApiKeyAuthenticationHandler.cs"));
        }
        return securities;
    }

    private void addInterfaceControllerTemplate() {
        String interfaceOnlyOption = getOptionValue(INTERFACE_ONLY_OPTION);
        boolean interfaceOnly = false;
        if (StringUtils.isNotBlank(interfaceOnlyOption)) {
            interfaceOnly = Boolean.valueOf(getOptionValue(INTERFACE_ONLY_OPTION));
        } else {
            if (additionalProperties.get(INTERFACE_ONLY_OPTION.substring(2)) != null) {
                interfaceOnly = Boolean.valueOf(additionalProperties.get(INTERFACE_ONLY_OPTION.substring(2)).toString());
            }
        }

        String interfaceControllerOption = getOptionValue(INTERFACE_CONTROLLER_OPTION);
        boolean interfaceController = false;
        if (StringUtils.isNotBlank(interfaceControllerOption)) {
            interfaceController = Boolean.valueOf(getOptionValue(INTERFACE_CONTROLLER_OPTION));
        } else {
            if (additionalProperties.get(INTERFACE_CONTROLLER_OPTION.substring(2)) != null) {
                interfaceController = Boolean.valueOf(additionalProperties.get(INTERFACE_CONTROLLER_OPTION.substring(2)).toString());
            }
        }

        if (interfaceController) {
            apiTemplateFiles.put("icontroller.mustache", ".cs");
            additionalProperties.put("interfaceController", Boolean.TRUE);
        }
        if (interfaceOnly) {
            apiTemplateFiles.clear();
            apiTemplateFiles.put("icontroller.mustache", ".cs");
        }
    }


    @Override
    public String getArgumentsLocation() {
        return "/arguments/aspnetcore.yaml";
    }

    private void setAspNetCoreVersion() {
        String optionValue = getOptionValue(ASP_NET_CORE_VERSION_OPTION);
        if (StringUtils.isBlank(optionValue)) {
            if (additionalProperties.get(ASP_NET_CORE_VERSION_OPTION.substring(2)) != null) {
                this.aspNetCoreVersion = additionalProperties.get(ASP_NET_CORE_VERSION_OPTION.substring(2)).toString();
            } else {
                return;
            }
        } else {
            this.aspNetCoreVersion = optionValue;
        }
        if (!this.aspNetCoreVersion.equals("2.0") && !this.aspNetCoreVersion.equals("2.1") && !this.aspNetCoreVersion.equals("2.2")) {
            LOGGER.error("version '" + this.aspNetCoreVersion + "' is not supported, switching to default version: '" + DEFAULT_ASP_NET_CORE_VERSION + "'");
            this.aspNetCoreVersion = DEFAULT_ASP_NET_CORE_VERSION;
        }
    }
}
