package org.ai.toolkit.aitk.modelzoo.llm;

import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.repository.zoo.Criteria;
import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaIterable;
import de.kherud.llama.ModelParameters;
import org.ai.toolkit.aitk.llamacpp.LlamaCppInput;
import org.ai.toolkit.aitk.llamacpp.LlamaCppTranslatorFactory;
import org.ai.toolkit.aitk.modelzoo.AbstractBaseModelDefinition;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

public class LlamaCppModelDefinition extends AbstractBaseModelDefinition<LlamaCppInput, LlamaIterable> {

    private final String modelName;

    private final String size;

    private final String modelPath;

    private final ModelBasicInfo modelBasicInfo;

    private static final String FORMAT = "llm/%s/%s";

    @Override
    public String getId() {
        return String.format(FORMAT, modelName, size);
    }

    @Override
    public List<String> getModelFileList() {
        return Arrays.asList(modelPath);
    }

    private List<Param> inferenceParams = new ArrayList<>();

    private List<Param> modelParams = new ArrayList<>();

    public LlamaCppModelDefinition(String modelName, String size, String modelPath, ModelBasicInfo modelBasicInfo) {
        this.modelName = modelName;
        this.size = size;
        this.modelPath = modelPath;
        this.modelBasicInfo = modelBasicInfo;
        try {
            Field[] inferenceFields = InferenceParameters.class.getDeclaredFields();
            if (!Objects.isNull(inferenceFields)) {
                for (Field field : inferenceFields) {
                    field.setAccessible(true);
                    inferenceParams.add(new Param(field.get(null).toString(), FileExtension.TEXT));
                    field.setAccessible(false);
                }
            }
            Field[] modelFields = ModelParameters.class.getDeclaredFields();
            if (!Objects.isNull(modelFields)) {
                for (Field field : modelFields) {
                    field.setAccessible(true);
                    modelParams.add(new Param(field.get(null).toString(), FileExtension.TEXT));
                    field.setAccessible(false);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ModelBasicInfo getModelBasicInfo() {
        return modelBasicInfo;
    }

    @Override
    public LlamaCppInput postProcessBeforeModel(Input input) throws Exception {
        Param param = getRequestParams().get(0);
        String text = input.getAsString(param.getName());
        InferenceParameters inferParams = new InferenceParameters(text);
        Field field = InferenceParameters.class.getSuperclass().getDeclaredField("parameters");
        field.setAccessible(true);
        Map<String, String> parameters = (Map<String, String>) field.get(inferParams);
        for (Param iferParam : inferenceParams) {
            String value = input.getAsString(iferParam.getName());
            if (StringUtils.hasLength(value)) {
                parameters.put(iferParam.getName(), value);
            }
        }
        field.setAccessible(false);
        return LlamaCppInput.getGenerateLlamaCppInput(inferParams);
    }

    @Override
    public Output postProcessAfterModel(Input input, LlamaIterable modelOutput) throws Exception {
        Output output = new Output();
        output.add("text", new IteratorLlamaCppSupplier(modelOutput.iterator()));
        return output;
    }

    @Override
    public List<Criteria> getCriteriaList() {
        Criteria<LlamaCppInput, Object> llamaCpp =
                Criteria.builder()
                        .setTypes(LlamaCppInput.class, Object.class)
                        .optModelPath(getModelPath(modelPath))
                        .optTranslatorFactory(new LlamaCppTranslatorFactory())
                        .optEngine("LlamaCpp")
                        .build();
        return Arrays.asList(llamaCpp);
    }

    @Override
    public List<EngineEnum> getEngineList() {
        return Arrays.asList(EngineEnum.LlamaCpp);
    }

    @Override
    public ModelTypeEnum getModelType() {
        return ModelTypeEnum.LLM;
    }

    @Override
    public List<Param> getRequestParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("text", FileExtension.TEXT));
        params.addAll(inferenceParams);
        return params;
    }

    @Override
    public List<Param> getResponseParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("text", FileExtension.STREAM));
        return params;
    }

    @Override
    public List<Param> getLoadModelParams() {
        return modelParams;
    }

    public String getModelName() {
        return modelName;
    }

    public String getSize() {
        return size;
    }
}
