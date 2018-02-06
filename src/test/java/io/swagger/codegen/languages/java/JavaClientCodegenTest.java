package io.swagger.codegen.languages.java;

import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenModelType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaClientCodegenTest {

    @Test
    public void modelInheritanceSupportInGson() throws Exception {
        List allModels = new ArrayList();

        CodegenModel parent1 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        parent1.setName("parent1");
        parent1.setClassname("test.Parent1");

        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("model", parent1);
        allModels.add(modelMap);

        CodegenModel parent2 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        parent2.setName("parent2");
        parent2.setClassname("test.Parent2");

        modelMap = new HashMap<>();
        modelMap.put("model", parent2);
        allModels.add(modelMap);

        CodegenModel model1 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model1.setName("model1");
        model1.setClassname("test.Model1");
        model1.setParentModel(parent1);

        modelMap = new HashMap<>();
        modelMap.put("model", model1);
        allModels.add(modelMap);

        CodegenModel model2 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model2.setName("model2");
        model2.setClassname("test.Model2");
        model2.setParentModel(parent1);

        modelMap = new HashMap<>();
        modelMap.put("model", model2);
        allModels.add(modelMap);

        CodegenModel model3 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model3.setName("model3");
        model3.setClassname("test.Model3");
        model3.setParentModel(parent1);

        modelMap = new HashMap<>();
        modelMap.put("model", model3);
        allModels.add(modelMap);

        CodegenModel model4 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model4.setName("model4");
        model4.setClassname("test.Model4");
        model4.setParentModel(parent2);

        modelMap = new HashMap<>();
        modelMap.put("model", model4);
        allModels.add(modelMap);

        CodegenModel model5 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model5.setName("model5");
        model5.setClassname("test.Model5");
        model5.setParentModel(parent2);

        modelMap = new HashMap<>();
        modelMap.put("model", model5);
        allModels.add(modelMap);

        JavaClientCodegen clientCodegen = new JavaClientCodegen();
        List<Map<String, Object>> parentsList = clientCodegen.modelInheritanceSupportInGson(allModels);

        Assert.assertNotNull(parentsList);
        Assert.assertEquals(parentsList.size(), 2);

        Map<String, Object> parent = parentsList.get(0);
        Assert.assertEquals(parent.get("classname"), "test.Parent1");

        List<CodegenModel> children = (List<CodegenModel>) parent.get("children");
        Assert.assertNotNull(children);
        Assert.assertEquals(children.size(), 3);

        Map<String, Object> models = (Map<String, Object>) children.get(0);
        Assert.assertEquals(models.get("name"), "model1");
        Assert.assertEquals(models.get("classname"), "test.Model1");

        models = (Map<String, Object>) children.get(1);
        Assert.assertEquals(models.get("name"), "model2");
        Assert.assertEquals(models.get("classname"), "test.Model2");

        models = (Map<String, Object>) children.get(2);
        Assert.assertEquals(models.get("name"), "model3");
        Assert.assertEquals(models.get("classname"), "test.Model3");


        parent = parentsList.get(1);
        Assert.assertEquals(parent.get("classname"), "test.Parent2");

        children = (List<CodegenModel>) parent.get("children");
        Assert.assertNotNull(children);
        Assert.assertEquals(children.size(), 2);

        models = (Map<String, Object>) children.get(0);
        Assert.assertEquals(models.get("name"), "model4");
        Assert.assertEquals(models.get("classname"), "test.Model4");

        models = (Map<String, Object>) children.get(1);
        Assert.assertEquals(models.get("name"), "model5");
        Assert.assertEquals(models.get("classname"), "test.Model5");
    }
}
