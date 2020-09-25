package io.swagger.codegen.v3.generators.typescript;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.util.SchemaTypeUtil;

import org.apache.commons.lang3.StringUtils;

public class TypeScriptFetchClientCodegen extends AbstractTypeScriptClientCodegen {
    private static final SimpleDateFormat SNAPSHOT_SUFFIX_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    public static final String NPM_NAME = "npmName";
    public static final String NPM_VERSION = "npmVersion";
    public static final String NPM_REPOSITORY = "npmRepository";
    public static final String SNAPSHOT = "snapshot";
    public static final String WITH_INTERFACES = "withInterfaces";

    protected String npmName = null;
    protected String npmVersion = "1.0.0";
    protected String npmRepository = null;

    public TypeScriptFetchClientCodegen() {
        super();
        this.outputFolder = "generated-code" + File.separator + "typescript-fetch";

        this.cliOptions.add(new CliOption(NPM_NAME, "The name under which you want to publish generated npm package"));
        this.cliOptions.add(new CliOption(NPM_VERSION, "The version of your npm package"));
        this.cliOptions.add(new CliOption(NPM_REPOSITORY,
                "Use this property to set an url your private npmRepo in the package.json"));
        this.cliOptions.add(new CliOption(SNAPSHOT,
                "When setting this property to true the version will be suffixed with -SNAPSHOT.yyyyMMddHHmm",
                SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
        this.cliOptions.add(new CliOption(WITH_INTERFACES,
                "Setting this property to true will generate interfaces next to the default class implementations.",
                SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
    }

    @Override
    protected void addAdditionPropertiesToCodeGenModel(CodegenModel codegenModel, Schema schema) {
        if (schema instanceof MapSchema && hasSchemaProperties(schema)) {
            codegenModel.additionalPropertiesType = getTypeDeclaration((Schema) schema.getAdditionalProperties());
            addImport(codegenModel, codegenModel.additionalPropertiesType);
        } else if (schema instanceof MapSchema && hasTrueAdditionalProperties(schema)) {
            codegenModel.additionalPropertiesType = getTypeDeclaration(new ObjectSchema());
        }
    }

    @Override
    public String getName() {
        return "typescript-fetch";
    }

    @Override
    public String getHelp() {
        return "Generates a TypeScript client library using Fetch API (beta).";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.add(new SupportingFile("index.mustache", "", "index.ts"));
        supportingFiles.add(new SupportingFile("api.mustache", "", "api.ts"));
        supportingFiles.add(new SupportingFile("api_test.mustache", "", "api_test.spec.ts"));
        supportingFiles.add(new SupportingFile("configuration.mustache", "", "configuration.ts"));
        supportingFiles.add(new SupportingFile("custom.d.mustache", "", "custom.d.ts"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        supportingFiles.add(new SupportingFile("gitignore", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("json_decode.mustache", "", "json_decode.ts"));

        if (additionalProperties.containsKey(NPM_NAME)) {
            addNpmPackageGeneration();
        }
    }

    @Override
    public String getDefaultTemplateDir() {
        return "typescript-fetch";
    }

    @Override
    public String getTypeDeclaration(Schema propertySchema) {
        Schema inner;
        if (propertySchema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) propertySchema;
            inner = arraySchema.getItems();
            return this.getSchemaType(propertySchema) + "<" + this.getTypeDeclaration(inner) + ">";
        } else if (propertySchema instanceof MapSchema && hasSchemaProperties(propertySchema)) {
            inner = (Schema) propertySchema.getAdditionalProperties();
            return "{ [key: string]: " + this.getTypeDeclaration(inner) + "; }";
        } else if (propertySchema instanceof MapSchema && hasTrueAdditionalProperties(propertySchema)) {
            inner = new ObjectSchema();
            return "{ [key: string]: " + this.getTypeDeclaration(inner) + "; }";
        } else if (propertySchema instanceof FileSchema || propertySchema instanceof BinarySchema) {
            return "Blob";
        } else if (propertySchema instanceof ObjectSchema) {
            return "any";
        } else {
            return super.getTypeDeclaration(propertySchema);
        }
    }

    @Override
    public CodegenParameter fromParameter(Parameter parameter, Set<String> imports) {
        final CodegenParameter codegenParameter = super.fromParameter(parameter, imports);
        if (parameter.getSchema() != null && isObjectSchema(parameter.getSchema())) {
            //fixme: codegenParameter.getVendorExtensions().put(CodegenConstants.IS_OBJECT_EXT_NAME, Boolean.TRUE);
            codegenParameter.getVendorExtensions().put("x-is-object", Boolean.TRUE);
        }
        return codegenParameter;
    }

    @Override
    public CodegenParameter fromRequestBody(RequestBody body, String name, Schema schema, Map<String, Schema> schemas, Set<String> imports) {
        final CodegenParameter codegenParameter = super.fromRequestBody(body, name, schema, schemas, imports);
        if (schema == null) {
            schema = getSchemaFromBody(body);
        }
        if (schema != null && isObjectSchema(schema)) {
            //fixme: codegenParameter.getVendorExtensions().put(CodegenConstants.IS_OBJECT_EXT_NAME, Boolean.TRUE);
            codegenParameter.getVendorExtensions().put("x-is-object", Boolean.TRUE);
        }
        return codegenParameter;
    }

    @Override
    public String getSchemaType(Schema schema) {
        String swaggerType = super.getSchemaType(schema);
        if (isLanguagePrimitive(swaggerType) || isLanguageGenericType(swaggerType)) {
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

        String type = applyLocalTypeMapping(parameter.dataType);
        parameter.dataType = type;
        parameter.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, isLanguagePrimitive(type));
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

        // Files for building our lib
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("package.mustache", "", "package.json"));
        supportingFiles.add(new SupportingFile("tsconfig.mustache", "", "tsconfig.json"));
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

}
