package io.swagger.codegen.v3.generators.go;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

public class GoClientCodegen extends AbstractGoCodegen {
    protected String packageVersion = "1.0.0";
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";
    public static final String WITH_XML = "withXml";

    public GoClientCodegen() {
        this.outputFolder = "generated-code/go";
        this.modelTemplateFiles.put("model.mustache", ".go");
        this.apiTemplateFiles.put("api.mustache", ".go");
        this.modelDocTemplateFiles.put("model_doc.mustache", ".md");
        this.apiDocTemplateFiles.put("api_doc.mustache", ".md");
        this.hideGenerationTimestamp = Boolean.TRUE;
        this.setReservedWordsLowerCase(Arrays.asList("string", "bool", "uint", "uint8", "uint16", "uint32", "uint64", "int", "int8", "int16", "int32", "int64", "float32", "float64", "complex64", "complex128", "rune", "byte", "uintptr", "break", "default", "func", "interface", "select", "case", "defer", "go", "map", "struct", "chan", "else", "goto", "package", "switch", "const", "fallthrough", "if", "range", "type", "continue", "for", "import", "return", "var", "error", "ApiResponse", "nil"));
        this.cliOptions.add((new CliOption("packageVersion", "Go package version.")).defaultValue("1.0.0"));
        this.cliOptions.add(CliOption.newBoolean("withXml", "whether to include support for application/xml content type and include XML annotations in the model (works with libraries that provide support for JSON and XML)"));
    }

    @Override
    public String getDefaultTemplateDir() {
        return "go";
    }

    public void processOpts() {
        super.processOpts();

        if (this.additionalProperties.containsKey("packageName")) {
            this.setPackageName((String)this.additionalProperties.get("packageName"));
        } else {
            this.setPackageName("swagger");
        }

        if (this.additionalProperties.containsKey("packageVersion")) {
            this.setPackageVersion((String)this.additionalProperties.get("packageVersion"));
        } else {
            this.setPackageVersion("1.0.0");
        }

        this.additionalProperties.put("packageName", this.packageName);
        this.additionalProperties.put("packageVersion", this.packageVersion);
        this.additionalProperties.put("apiDocPath", this.apiDocPath);
        this.additionalProperties.put("modelDocPath", this.modelDocPath);
        this.modelPackage = this.packageName;
        this.apiPackage = this.packageName;
        this.supportingFiles.add(new SupportingFile("swagger.mustache", "api", "swagger.yaml"));
        this.supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        this.supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        this.supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        this.supportingFiles.add(new SupportingFile("configuration.mustache", "", "configuration.go"));
        this.supportingFiles.add(new SupportingFile("client.mustache", "", "client.go"));
        this.supportingFiles.add(new SupportingFile("response.mustache", "", "response.go"));
        this.supportingFiles.add(new SupportingFile(".travis.yml", "", ".travis.yml"));
        if (this.additionalProperties.containsKey("withXml")) {
            this.setWithXml(Boolean.parseBoolean(this.additionalProperties.get("withXml").toString()));
            if (this.withXml) {
                this.additionalProperties.put("withXml", "true");
            }
        }

    }

    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    public String getName() {
        return "go";
    }

    public String getHelp() {
        return "Generates a Go client library (beta).";
    }

    public String apiFileFolder() {
        return this.outputFolder + File.separator;
    }

    public String modelFileFolder() {
        return this.outputFolder + File.separator;
    }

    public String apiDocFileFolder() {
        return (this.outputFolder + "/" + this.apiDocPath).replace('/', File.separatorChar);
    }

    public String modelDocFileFolder() {
        return (this.outputFolder + "/" + this.modelDocPath).replace('/', File.separatorChar);
    }

    public String toModelDocFilename(String name) {
        return this.toModelName(name);
    }

    public String toApiDocFilename(String name) {
        return this.toApiName(name);
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {
        final CodegenOperation codegenOperation = super.fromOperation(path, httpMethod, operation, schemas, openAPI);
        if (codegenOperation.getHasBodyParam() || codegenOperation.bodyParam != null) {
            codegenOperation.getFormParams().clear();
        }
        return codegenOperation;
    }
}

