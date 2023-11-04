package io.swagger.codegen.v3.generators.typescript;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.SupportingFile;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class TypeScriptAxiosClientCodegen extends AbstractTypeScriptClientCodegen {

    public static final String NPM_NAME = "npmName";
    public static final String NPM_VERSION = "npmVersion";
    public static final String NPM_REPOSITORY = "npmRepository";
    public static final String DEFAULT_API_PACKAGE = "apis";
    public static final String DEFAULT_MODEL_PACKAGE = "models";

    protected String npmRepository = null;
    protected String npmName = null;
    protected String npmVersion = "1.0.0";


    private String tsModelPackage = "";

    public TypeScriptAxiosClientCodegen() {
        super();
        importMapping.clear();
        outputFolder = "generated-code/typescript-axios";
        LOGGER.info("Template folder: " + this.templateDir());
        LOGGER.info("Template engine: " + this.getTemplateEngine());
        reservedWords.add("query");

        // Custom CLI options
        this.cliOptions.add(new CliOption(NPM_NAME, "The name under which you want to publish generated npm package"));
        this.cliOptions.add(new CliOption(NPM_VERSION, "The version of your npm package. Defaults to 1.0.0"));
        this.cliOptions.add(new CliOption(NPM_REPOSITORY,
                "Use this property to set an url your private npm registry in the package.json"));
    }

    @Override
    public String getName() {
        return "typescript-axios";
    }

    @Override
    public String getHelp() {
        return "Generates a TypeScript Axios client library.";
    }

    /**
     * Creates a relative path to a file or folder. The resulting path is
     * relative to the root directory of the final client library.
     *
     * @param path The path to the file or folder.
     * @return A path to the file or folder which is relative to the client
     * library root directory.
     */
    private static String getRelativeToRoot(String path) {
        StringBuilder sb = new StringBuilder();
        int slashCount = path.split("/").length;
        if (slashCount == 0) {
            sb.append("./");
        } else {
            for (int i = 0; i < slashCount; ++i) {
                sb.append("../");
            }
        }
        return sb.toString();
    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (StringUtils.isBlank(modelPackage)) {
            modelPackage = DEFAULT_MODEL_PACKAGE;
        }
        if (StringUtils.isBlank(apiPackage)) {
            apiPackage = DEFAULT_API_PACKAGE;
        }
        tsModelPackage = modelPackage.replaceAll("\\.", "/");
        String tsApiPackage = apiPackage.replaceAll("\\.", "/");

        String modelRelativeToRoot = getRelativeToRoot(tsModelPackage);
        String apiRelativeToRoot = getRelativeToRoot(tsApiPackage);

        additionalProperties.put("tsModelPackage", tsModelPackage);
        additionalProperties.put("tsApiPackage", tsApiPackage);
        additionalProperties.put("apiRelativeToRoot", apiRelativeToRoot);
        additionalProperties.put("modelRelativeToRoot", modelRelativeToRoot);

        supportingFiles.add(new SupportingFile("index.mustache", "", "index.ts"));
        supportingFiles.add(new SupportingFile("baseApi.mustache", "", "base.ts"));
        supportingFiles.add(new SupportingFile("api.mustache", "", "api.ts"));
        supportingFiles.add(new SupportingFile("configuration.mustache", "", "configuration.ts"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        supportingFiles.add(new SupportingFile("gitignore", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("npmignore", "", ".npmignore"));

        modelTemplateFiles.put("model.mustache", ".ts");
        apiTemplateFiles.put("apiInner.mustache", ".ts");
        supportingFiles.add(new SupportingFile("modelIndex.mustache", tsModelPackage, "index.ts"));

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("package.mustache", "", "package.json"));
        supportingFiles.add(new SupportingFile("tsconfig.mustache", "", "tsconfig.json"));

        addNpmPackageGeneration();
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> operations) {
        boolean hasImports = operations.get("hasImport") != null && Boolean.parseBoolean(operations.get("hasImport").toString());
        if (hasImports) {
            List<Map<String, String>> imports = (List<Map<String, String>>) operations.get("imports");

            for (Map<String, String> importMap : imports) {
                final String importValue = importMap.get("import");
                if (StringUtils.isNotBlank(importValue) && importValue.contains(".")) {
                    int index = importValue.indexOf(".");
                    importMap.put("import", importValue.substring(index + 1));
                }
            }
        }
        return operations;
    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        objs = super.postProcessOperationsWithModels(objs, allModels);
        Map<String, Object> vals = (Map<String, Object>) objs.getOrDefault("operations", new HashMap<>());
        List<CodegenOperation> operations = (List<CodegenOperation>) vals.getOrDefault("operation", new ArrayList<>());
        /*
            Filter all the operations that are multipart/form-data operations and set the vendor extension flag
            'multipartFormData' for the template to work with.
         */
        operations.stream()
            .filter(op -> getBooleanValue(op, CodegenConstants.HAS_CONSUMES_EXT_NAME))
            .filter(op -> op.consumes.stream().anyMatch(opc -> opc.values().stream().anyMatch("multipart/form-data"::equals)))
            .forEach(op -> op.vendorExtensions.putIfAbsent("multipartFormData", true));
        return objs;
    }

    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        Map<String, Object> result = super.postProcessAllModels(objs);
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Map<String, Object> inner = (Map<String, Object>) entry.getValue();
            List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
            for (Map<String, Object> model : models) {
                CodegenModel codegenModel = (CodegenModel) model.get("model");
                //todo: model.put("hasAllOf", codegenModel.allOf.size() > 0);
                //todo: model.put("hasOneOf", codegenModel.oneOf.size() > 0);
            }
        }
        return result;
    }


    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        List<Object> models = (List<Object>) postProcessModelsEnum(objs).get("models");

        for (Object _mo  : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");

            // Deduce the model file name in kebab case
            cm.classFilename = cm.classname.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);

            //processed enum names
            cm.imports = new TreeSet<String>(cm.imports);
            // name enum with model name, e.g. StatusEnum => PetStatusEnum
            for (CodegenProperty var : cm.vars) {
                if (getBooleanValue(var, CodegenConstants.IS_ENUM_EXT_NAME)) {
                    var.datatypeWithEnum = var.datatypeWithEnum.replace(var.enumName, cm.classname + var.enumName);
                    var.enumName = var.enumName.replace(var.enumName, cm.classname + var.enumName);
                }
            }
            if (cm.parent != null) {
                for (CodegenProperty var : cm.allVars) {
                    if (getBooleanValue(var, CodegenConstants.IS_ENUM_EXT_NAME)) {
                        var.datatypeWithEnum = var.datatypeWithEnum.replace(var.enumName, cm.classname + var.enumName);
                        var.enumName = var.enumName.replace(var.enumName, cm.classname + var.enumName);
                    }
                }
            }
        }

        // Apply the model file name to the imports as well
        for (Map<String, String> m : (List<Map<String, String>>) objs.get("imports")) {
            String javaImport = m.get("import").substring(modelPackage.length() + 1);
            String tsImport = tsModelPackage + "/" + javaImport;
            m.put("tsImport", tsImport);
            m.put("class", javaImport);
            m.put("filename", javaImport.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT));
        }
        return objs;
    }

    /**
     * Extracts npm package fields from `additionalProperties`. These fields
     * are provided as custom CLI options.
     */
    private void addNpmPackageGeneration() {
        // Name of the NPM package
        if (additionalProperties.containsKey(NPM_NAME)) {
            this.setNpmName(additionalProperties.get(NPM_NAME).toString());
        }

        // NPM package version (SemVer)
        if (additionalProperties.containsKey(NPM_VERSION)) {
            this.setNpmVersion(additionalProperties.get(NPM_VERSION).toString());
        }
        /* Package version has default value. Make internal version and
         * additionalProperties version consistent.
         */
        additionalProperties.put(NPM_VERSION, npmVersion);

        // NPM registry the package is pushed to
        if (additionalProperties.containsKey(NPM_REPOSITORY)) {
            this.setNpmRepository(additionalProperties.get(NPM_REPOSITORY).toString());
        }
    }

    /**
     * Overriding toRegularExpression() to avoid escapeText() being called,
     * as it would return a broken regular expression if any escaped character / metacharacter were present.
     */
    @Override
    public String toRegularExpression(String pattern) {
        return addRegularExpressionDelimiter(pattern);
    }

    @Override
    public String toModelFilename(String name) {
        return super.toModelFilename(name).replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    @Override
    public String toApiFilename(String name) {
        return super.toApiFilename(name).replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    @Override
    public String getDefaultTemplateDir() {
        return "typescript-axios";
    }

    /**
     * Gets the name of the generated NPM package.
     *
     * @return The NPM package name.
     */
    public String getNpmName() {
        return this.npmName;
    }

    public void setNpmName(String npmName) {
       this.npmName = npmName;
    }

    /**
     * Gets the generated NPM package SemVer string.
     *
     * @return The package version.
     */
    public String getNpmVersion() {
        return this.npmVersion;
    }

    public void setNpmVersion(String npmVersion) {
        this.npmVersion = npmVersion;
    }

    /**
     * Gets the name of the NPM registry the package is published to.
     *
     * @return The NPM registry name.
     */
    public String getNpmRepository() {
        return this.npmRepository;
    }

    public void setNpmRepository(String npmRepository) {
        this.npmRepository = npmRepository;
    }

}
