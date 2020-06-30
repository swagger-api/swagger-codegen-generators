package io.swagger.codegen.v3.generators.typescript;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.utils.ModelUtils;
import io.swagger.codegen.v3.utils.SemVer;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class TypeScriptAngularClientCodegen extends AbstractTypeScriptClientCodegen {

    private static Logger LOGGER = LoggerFactory.getLogger(TypeScriptAngularClientCodegen.class);

    private static final SimpleDateFormat SNAPSHOT_SUFFIX_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    public static final String NPM_NAME = "npmName";
    public static final String NPM_VERSION = "npmVersion";
    public static final String NPM_REPOSITORY = "npmRepository";
    public static final String SNAPSHOT = "snapshot";
    public static final String WITH_INTERFACES = "withInterfaces";
    public static final String NG_VERSION = "ngVersion";
    public static final String NG_PACKAGR = "useNgPackagr";
    public static final String PROVIDED_IN_ROOT ="providedInRoot";

    protected String npmName = null;
    protected String npmVersion = "1.0.0";
    protected String npmRepository = null;

    public TypeScriptAngularClientCodegen() {
        super();
        this.outputFolder = "generated-code" + File.separator + "typescript-angular";

        this.cliOptions.add(new CliOption(NPM_NAME, "The name under which you want to publish generated npm package"));
        this.cliOptions.add(new CliOption(NPM_VERSION, "The version of your npm package"));
        this.cliOptions.add(new CliOption(NPM_REPOSITORY, "Use this property to set an url your private npmRepo in the package.json"));
        this.cliOptions.add(new CliOption(SNAPSHOT, "When setting this property to true the version will be suffixed with -SNAPSHOT.yyyyMMddHHmm", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
        this.cliOptions.add(new CliOption(WITH_INTERFACES, "Setting this property to true will generate interfaces next to the default class implementations.", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
        this.cliOptions.add(new CliOption(NG_VERSION, "The version of Angular. Default is '4.3'"));
        this.cliOptions.add(new CliOption(PROVIDED_IN_ROOT, "Use this property to provide Injectables in root (it is only valid in angular version greater or equal to 6.0.0).", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
    }

    @Override
    protected void addAdditionPropertiesToCodeGenModel(CodegenModel codegenModel, Schema schema) {
        if (schema instanceof MapSchema  && hasSchemaProperties(schema)) {
            codegenModel.additionalPropertiesType = getTypeDeclaration((Schema) schema.getAdditionalProperties());
            addImport(codegenModel, codegenModel.additionalPropertiesType);
        } else if (schema instanceof MapSchema && hasTrueAdditionalProperties(schema)) {
            codegenModel.additionalPropertiesType = getTypeDeclaration(new ObjectSchema());
        }
    }

    @Override
    public String getName() {
        return "typescript-angular";
    }

    @Override
    public String getHelp() {
        return "Generates a TypeScript Angular (2.x or 4.x) client library.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        modelTemplateFiles.put("model.mustache", ".ts");
        apiTemplateFiles.put("api.service.mustache", ".ts");

        languageSpecificPrimitives.add("Blob");
        typeMapping.put("file", "Blob");
        apiPackage = "api";
        modelPackage = "model";

        supportingFiles.add(
                new SupportingFile("models.mustache", modelPackage().replace('.', '/'), "models.ts"));
        supportingFiles
                .add(new SupportingFile("apis.mustache", apiPackage().replace('.', '/'), "api.ts"));
        supportingFiles.add(new SupportingFile("index.mustache", getIndexDirectory(), "index.ts"));
        supportingFiles.add(new SupportingFile("api.module.mustache", getIndexDirectory(), "api.module.ts"));
        supportingFiles.add(new SupportingFile("configuration.mustache", getIndexDirectory(), "configuration.ts"));
        supportingFiles.add(new SupportingFile("variables.mustache", getIndexDirectory(), "variables.ts"));
        supportingFiles.add(new SupportingFile("encoder.mustache", getIndexDirectory(), "encoder.ts"));
        supportingFiles.add(new SupportingFile("gitignore", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("npmignore", "", ".npmignore"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));

        SemVer ngVersion = determineNgVersion();

        additionalProperties.put(NG_VERSION, ngVersion);

        // for Angular 2 AOT support we will use good-old ngc,
        // Angular Package format wasn't invented at this time and building was much more easier
        if (!ngVersion.atLeast("4.0.0")) {
            LOGGER.warn("Please update your legacy Angular " + ngVersion + " project to benefit from 'Angular Package Format' support.");
            additionalProperties.put(NG_PACKAGR, false);
        } else {
            additionalProperties.put(NG_PACKAGR, true);
        }

        // Set the typescript version compatible to the Angular version
        if (ngVersion.atLeast("8.0.0")) {
            additionalProperties.put("tsVersion", ">=3.4.0 <3.6.0");
        } else if (ngVersion.atLeast("7.0.0")) {
            additionalProperties.put("tsVersion", ">=3.1.1 <3.2.0");
        } else if (ngVersion.atLeast("6.0.0")) {
            additionalProperties.put("tsVersion", ">=2.7.2 and <2.10.0");
        } else if (ngVersion.atLeast("5.0.0")) {
            additionalProperties.put("tsVersion", ">=2.1.5 <2.7.0");
        } else {
            // Angular v2-v4 requires typescript ">=2.1.5 <2.8"
            additionalProperties.put("tsVersion", ">=2.1.5 <2.8.0");
        }

        // Set the rxJS version compatible to the Angular version
        if (ngVersion.atLeast("8.0.0")) {
            additionalProperties.put("rxjsVersion", "6.5.0");
            additionalProperties.put("useRxJS6", true);
        } else if (ngVersion.atLeast("7.0.0")) {
            additionalProperties.put("rxjsVersion", "6.3.0");
            additionalProperties.put("useRxJS6", true);
        } else if (ngVersion.atLeast("6.0.0")) {
            additionalProperties.put("rxjsVersion", "6.1.0");
            additionalProperties.put("useRxJS6", true);
        } else {
            // Angular prior to v6
            additionalProperties.put("rxjsVersion", "5.4.0");
        }
        if (!ngVersion.atLeast("4.3.0")) {
            supportingFiles.add(new SupportingFile("rxjs-operators.mustache", getIndexDirectory(), "rxjs-operators.ts"));
        }

        // for Angular 2 AOT support we will use good-old ngc,
        // Angular Package format wasn't invented at this time and building was much more easier
        if (!ngVersion.atLeast("4.0.0")) {
            LOGGER.warn("Please update your legacy Angular " + ngVersion + " project to benefit from 'Angular Package Format' support.");
            additionalProperties.put("useNgPackagr", false);
        } else {
            additionalProperties.put("useNgPackagr", true);
            supportingFiles.add(new SupportingFile("ng-package.mustache", getIndexDirectory(), "ng-package.json"));
        }

        // Libraries generated with v1.x of ng-packagr will ship with AoT metadata in v3, which is intended for Angular v4.
        // Libraries generated with v2.x of ng-packagr will ship with AoT metadata in v4, which is intended for Angular v5 (and Angular v6).
        additionalProperties.put("useOldNgPackagr", !ngVersion.atLeast("5.0.0"));

        // Specific ng-packagr configuration
        if (ngVersion.atLeast("8.0.0")) {
            additionalProperties.put("ngPackagrVersion", "5.4.0");
            additionalProperties.put("tsickleVersion", "0.35.0");
        } else if (ngVersion.atLeast("7.0.0")) {
            // compatible versions with typescript version
            additionalProperties.put("ngPackagrVersion", "5.1.0");
            additionalProperties.put("tsickleVersion", "0.34.0");
        } else if (ngVersion.atLeast("6.0.0")) {
            // compatible versions with typescript version
            additionalProperties.put("ngPackagrVersion", "3.0.6");
            additionalProperties.put("tsickleVersion", "0.32.1");
        } else if (ngVersion.atLeast("5.0.0")) {
            // compatible versions with typescript version
            additionalProperties.put("ngPackagrVersion", "2.4.5");
            additionalProperties.put("tsickleVersion", "0.27.5");
        } else {
            // Angular versions prior to v5
            additionalProperties.put("ngPackagrVersion", "1.6.0");
        }

        // set zone.js version
        if (ngVersion.atLeast("8.0.0")) {
            additionalProperties.put("zonejsVersion", "0.9.1");
        } else if (ngVersion.atLeast("5.0.0")) {
            // compatible versions to Angular 5+
            additionalProperties.put("zonejsVersion", "0.8.26");
        } else {
            // Angular versions prior to v5
            additionalProperties.put("zonejsVersion", "0.7.6");
        }

        // set http client usage
        if (ngVersion.atLeast("8.0.0")) {
            additionalProperties.put("useHttpClient", true);
            additionalProperties.put("useHttpClientPackage", false);
        } else if (ngVersion.atLeast("4.3.0")) {
            additionalProperties.put("useHttpClient", true);
            additionalProperties.put("useHttpClientPackage", true);
        } else {
            additionalProperties.put("useHttpClient", false);
            additionalProperties.put("useHttpClientPackage", false);
        }

        if (additionalProperties.containsKey(PROVIDED_IN_ROOT) && !ngVersion.atLeast("6.0.0")) {
            additionalProperties.put(PROVIDED_IN_ROOT,false);
        }

        additionalProperties.put("injectionToken", ngVersion.atLeast("4.0.0") ? "InjectionToken" : "OpaqueToken");
        additionalProperties.put("injectionTokenTyped", ngVersion.atLeast("4.0.0"));

        if (additionalProperties.containsKey(NPM_NAME)) {
            addNpmPackageGeneration();
        }

        if (additionalProperties.containsKey(WITH_INTERFACES)) {
            boolean withInterfaces = Boolean.parseBoolean(additionalProperties.get(WITH_INTERFACES).toString());
            if (withInterfaces) {
                apiTemplateFiles.put("apiInterface.mustache", "Interface.ts");
            }
        }

    }

    private SemVer determineNgVersion() {
        SemVer ngVersion;
        if (additionalProperties.containsKey(NG_VERSION)) {
            ngVersion = new SemVer(additionalProperties.get(NG_VERSION).toString());
        } else {
            ngVersion = new SemVer("8.0.0");
            LOGGER.info("generating code for Angular {} ...", ngVersion);
            LOGGER.info("  (you can select the angular version by setting the additionalProperty ngVersion)");
        }
        return ngVersion;
    }

    private void addNpmPackageGeneration() {
        if (additionalProperties.containsKey(NPM_NAME)) {
            this.setNpmName(additionalProperties.get(NPM_NAME).toString());
        }

        if (additionalProperties.containsKey(NPM_VERSION)) {
            this.setNpmVersion(additionalProperties.get(NPM_VERSION).toString());
        }

        if (additionalProperties.containsKey(SNAPSHOT)
                && Boolean.valueOf(additionalProperties.get(SNAPSHOT).toString())) {
            this.setNpmVersion(npmVersion + "-SNAPSHOT." + SNAPSHOT_SUFFIX_FORMAT.format(new Date()));
        }
        additionalProperties.put(NPM_VERSION, npmVersion);

        if (additionalProperties.containsKey(NPM_REPOSITORY)) {
            this.setNpmRepository(additionalProperties.get(NPM_REPOSITORY).toString());
        }

        //Files for building our lib
        supportingFiles.add(new SupportingFile("README.mustache", getIndexDirectory(), "README.md"));
        supportingFiles.add(new SupportingFile("package.mustache", getIndexDirectory(), "package.json"));
        supportingFiles.add(new SupportingFile("typings.mustache", getIndexDirectory(), "typings.json"));
        supportingFiles.add(new SupportingFile("tsconfig.mustache", getIndexDirectory(), "tsconfig.json"));
        if (additionalProperties.containsKey(NG_PACKAGR)
            && Boolean.valueOf(additionalProperties.get(NG_PACKAGR).toString())) {
            supportingFiles.add(new SupportingFile("ng-package.mustache", getIndexDirectory(), "ng-package.json"));
        }
    }

    private String getIndexDirectory() {
        String indexPackage = modelPackage.substring(0, Math.max(0, modelPackage.lastIndexOf('.')));
        return indexPackage.replace('.', File.separatorChar);
    }

    @Override
    public boolean isDataTypeFile(final String dataType) {
        return dataType != null && dataType.equals("Blob");
    }

    @Override
    public String getArgumentsLocation() {
        return null;
    }

    @Override
    public String getDefaultTemplateDir() {
        return "typescript-angular";
    }

    @Override
    public String getTypeDeclaration(Schema propertySchema) {
        Schema inner;
        if(propertySchema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema)propertySchema;
            inner = arraySchema.getItems();
            return this.getSchemaType(propertySchema) + "<" + this.getTypeDeclaration(inner) + ">";
        } else if(propertySchema instanceof MapSchema   && hasSchemaProperties(propertySchema)) {
            inner = (Schema) propertySchema.getAdditionalProperties();
            return "{ [key: string]: " + this.getTypeDeclaration(inner) + "; }";
        } else if (propertySchema instanceof MapSchema && hasTrueAdditionalProperties(propertySchema)) {
            inner = new ObjectSchema();
            return "{ [key: string]: " + this.getTypeDeclaration(inner) + "; }";
        } else if(propertySchema instanceof FileSchema || propertySchema instanceof BinarySchema) {
            return "Blob";
        } else if(propertySchema instanceof ObjectSchema) {
            return "any";
        } else {
            return super.getTypeDeclaration(propertySchema);
        }
    }

    @Override
    public String getSchemaType(Schema schema) {
        String swaggerType = super.getSchemaType(schema);
        if(isLanguagePrimitive(swaggerType) || isLanguageGenericType(swaggerType)) {
            return swaggerType;
        }
        applyLocalTypeMapping(swaggerType);
        return swaggerType;
    }

    private String applyLocalTypeMapping(String type) {
        if (typeMapping.containsKey(type)) {
            type = typeMapping.get(type);
        }
        return type;
    }

    private boolean isLanguagePrimitive(String type) {
        return languageSpecificPrimitives.contains(type);
    }

    private boolean isLanguageGenericType(String type) {
        for (String genericType : languageGenericTypes) {
            if (type.startsWith(genericType + "<")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void postProcessParameter(CodegenParameter parameter) {
        super.postProcessParameter(parameter);
        parameter.dataType = applyLocalTypeMapping(parameter.dataType);
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> operations) {
        Map<String, Object> objs = (Map<String, Object>) operations.get("operations");

        // Add filename information for api imports
        objs.put("apiFilename", getApiFilenameFromClassname(objs.get("classname").toString()));

        List<CodegenOperation> ops = (List<CodegenOperation>) objs.get("operation");
        for (CodegenOperation op : ops) {
            if ((boolean) additionalProperties.get("useHttpClient")) {
                op.httpMethod = op.httpMethod.toLowerCase(Locale.ENGLISH);
            } else {
                // Convert httpMethod to Angular's RequestMethod enum
                // https://angular.io/docs/ts/latest/api/http/index/RequestMethod-enum.html
                switch (op.httpMethod) {
                case "GET":
                    op.httpMethod = "RequestMethod.Get";
                    break;
                case "POST":
                    op.httpMethod = "RequestMethod.Post";
                    break;
                case "PUT":
                    op.httpMethod = "RequestMethod.Put";
                    break;
                case "DELETE":
                    op.httpMethod = "RequestMethod.Delete";
                    break;
                case "OPTIONS":
                    op.httpMethod = "RequestMethod.Options";
                    break;
                case "HEAD":
                    op.httpMethod = "RequestMethod.Head";
                    break;
                case "PATCH":
                    op.httpMethod = "RequestMethod.Patch";
                    break;
                default:
                    throw new RuntimeException("Unknown HTTP Method " + op.httpMethod + " not allowed");
                }
            }

            // Prep a string buffer where we're going to set up our new version of the string.
            StringBuilder pathBuffer = new StringBuilder();
            StringBuilder parameterName = new StringBuilder();
            int insideCurly = 0;

            // Iterate through existing string, one character at a time.
            for (int i = 0; i < op.path.length(); i++) {
                switch (op.path.charAt(i)) {
                case '{':
                    // We entered curly braces, so track that.
                    insideCurly++;

                    // Add the more complicated component instead of just the brace.
                    pathBuffer.append("${encodeURIComponent(String(");
                    break;
                case '}':
                    // We exited curly braces, so track that.
                    insideCurly--;

                    // Add the more complicated component instead of just the brace.
                    pathBuffer.append(toVarName(parameterName.toString()));
                    pathBuffer.append("))}");
                    parameterName.setLength(0);
                    break;
                default:
                    if (insideCurly > 0) {
                        parameterName.append(op.path.charAt(i));
                    } else {
                        pathBuffer.append(op.path.charAt(i));
                    }
                    break;
                }
            }

            // Overwrite path to TypeScript template string, after applying everything we just did.
            op.path = pathBuffer.toString();
        }

        // Add additional filename information for model imports in the services
        List<Map<String, Object>> imports = (List<Map<String, Object>>) operations.get("imports");
        for (Map<String, Object> im : imports) {
            im.put("filename", im.get("import"));
            im.put("classname", getModelnameFromModelFilename(im.get("filename").toString()));
        }

        return operations;
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        Map<String, Object> result = super.postProcessModels(objs);

        // Add additional filename information for imports
        List<Object> models = (List<Object>) postProcessModelsEnum(result).get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            mo.put("tsImports", toTsImports(cm, cm.imports));
        }

        return result;
    }

    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> processedModels) {
        for (Map.Entry<String, Object> entry : processedModels.entrySet()) {
            final Map<String, Object> inner = (Map<String, Object>) entry.getValue();
            final List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
            for (Map<String, Object> mo : models) {
                final CodegenModel codegenModel = (CodegenModel) mo.get("model");
                if (codegenModel.getIsAlias() && codegenModel.imports != null && !codegenModel.imports.isEmpty()) {
                    mo.put("tsImports", toTsImports(codegenModel, codegenModel.imports));
                }
            }
        }
        return processedModels;
    }

    private List<Map<String, String>> toTsImports(CodegenModel cm, Set<String> imports) {
        List<Map<String, String>> tsImports = new ArrayList<>();
        for (String im : imports) {
            if (!im.equals(cm.classname)) {
                HashMap<String, String> tsImport = new HashMap<>();
                tsImport.put("classname", im);
                tsImport.put("filename", toModelFilename(im));
                tsImports.add(tsImport);
            }
        }
        return tsImports;
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultService";
        }
        return initialCaps(name) + "Service";
    }

    @Override
    public String toApiFilename(String name) {
        if (name.length() == 0) {
            return "default.service";
        }
        return camelize(name, true) + ".service";
    }

    @Override
    public String toApiImport(String name) {
        return apiPackage() + "/" + toApiFilename(name);
    }

    @Override
    public String toModelFilename(String name) {
        return camelize(toModelName(name), true);
    }

    @Override
    public String toModelImport(String name) {
        return modelPackage() + "/" + toModelFilename(name);
    }

    public String getNpmName() {
        return npmName;
    }

    public void setNpmName(String npmName) {
        this.npmName = npmName;
    }

    public String getNpmVersion() {
        return npmVersion;
    }

    public void setNpmVersion(String npmVersion) {
        this.npmVersion = npmVersion;
    }

    public String getNpmRepository() {
        return npmRepository;
    }

    public void setNpmRepository(String npmRepository) {
        this.npmRepository = npmRepository;
    }

    private String getApiFilenameFromClassname(String classname) {
        String name = classname.substring(0, classname.length() - "Service".length());
        return toApiFilename(name);
    }

    private String getModelnameFromModelFilename(String filename) {
        String name = filename.substring((modelPackage() + File.separator).length());
        return camelize(name);
    }

}
