package org.ai.toolkit.aitk.modelzoo.llm;

import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.repository.zoo.Criteria;
import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaIterable;
import de.kherud.llama.args.MiroStat;
import org.ai.toolkit.aitk.llamacpp.LlamaCppInput;
import org.ai.toolkit.aitk.llamacpp.LlamaCppTranslatorFactory;
import org.ai.toolkit.aitk.modelzoo.AbstractBaseModelDefinition;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class LlamaCppModelDefinition extends AbstractBaseModelDefinition<LlamaCppInput, LlamaIterable> {
    @Override
    public String getId() {
        return "/llm/qwen2-1.5b";
    }

    @Override
    public ModelBasicInfo getModelBasicInfo() {
        return  new ModelBasicInfo("qwen2-1.5b", "对话模型", "/modelimage/model.jpg", "请输入一段文字");
    }

    @Override
    public LlamaCppInput postProcessBeforeModel(Input input) throws Exception {
        Param param = getRequestParams().get(0);
        String text = input.getAsString(param.getName());
        InferenceParameters inferParams = new InferenceParameters(text)
                .setTemperature(0.7f)
                .setPenalizeNl(true)
                .setMiroStat(MiroStat.V2);
        return LlamaCppInput.getGenerateLlamaCppInput(inferParams);
    }

    @Override
    public Output postProcessAfterModel(Input input, LlamaIterable modelOutput) throws Exception {
        Output output = new Output();
        output.add("text", new IteratorBytesSupplier(modelOutput.iterator()));
        return output;
    }

    @Override
    public List<Criteria> getCriteriaList() {
        Path modelUrl = Path.of("C:\\Users\\Administrator\\.ollama\\models\\qwen2-1.5b.gguf");
        Criteria<LlamaCppInput, Object> classifier =
                Criteria.builder()
                        .setTypes(LlamaCppInput.class, Object.class)
                        .optModelPath(modelUrl)
                        .optTranslatorFactory(new LlamaCppTranslatorFactory())
                        .optEngine("LlamaCpp")
                        .build();
        return Arrays.asList(classifier);
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
        return params;
    }

    @Override
    public List<Param> getResponseParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("text", FileExtension.STREAM));
        return params;
    }
}
