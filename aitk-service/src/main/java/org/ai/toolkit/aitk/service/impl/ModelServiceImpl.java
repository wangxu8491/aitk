package org.ai.toolkit.aitk.service.impl;

import ai.djl.modality.Input;

import java.util.*;
import java.util.stream.Collectors;

import ai.djl.modality.Output;
import org.ai.toolkit.aitk.modelzoo.constant.ModelParentTypeEnum;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceExecutor;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceCallback;
import org.ai.toolkit.aitk.modelmanager.ModelManager;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.ai.toolkit.aitk.modelzoo.llm.LlamaCppModelDefinition;
import org.ai.toolkit.aitk.service.ModelService;
import org.ai.toolkit.aitk.service.vo.LlmModelVO;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;
import org.ai.toolkit.aitk.service.vo.ModelNodeDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ModelServiceImpl implements ModelService {

    @Autowired
    private ModelManager modelManager;
    @Autowired
    private InferenceExecutor inferenceExecutor;


    @Override
    public void asyncPredict(String modelId, Input input, InferenceCallback callback) {
        inferenceExecutor.asyncExecute(modelId, input, callback);
    }

    @Override
    public Output syncPredict(String modelId, Input input) {
        return inferenceExecutor.syncExecute(modelId, input);
    }

    @Override
    public ModelParamVO getModelParamVO(String modelId) {
        ModelDefinition modelDefinition = modelManager.getOnlineModelDefinition(modelId);
        return new ModelParamVO(modelDefinition.getLoadModelParams(), modelDefinition.getRequestParams(),
                modelDefinition.getResponseParams(), modelDefinition.getModelBasicInfo());
    }

    @Override
    public List<ModelNodeDataVO> getModelTreeData() {
        List<ModelDefinition> modelDefinitionList = modelManager.getModelList();
        if (CollectionUtils.isEmpty(modelDefinitionList)) {
            return new ArrayList<>();
        }
        Map<ModelTypeEnum, List<ModelDefinition>> modelTypeEnumListMap = new HashMap<>();
        for (ModelDefinition modelDefinition : modelDefinitionList) {
            if (modelTypeEnumListMap.containsKey(modelDefinition.getModelType())) {
                modelTypeEnumListMap.get(modelDefinition.getModelType()).add(modelDefinition);
            } else {
                modelTypeEnumListMap.put(modelDefinition.getModelType(), new ArrayList<>(Arrays.asList(modelDefinition)));
            }
        }
        List<ModelNodeDataVO> result = new ArrayList<>();
        Map<ModelParentTypeEnum, List<ModelTypeEnum>> modelParentTypeEnumListMap = ModelTypeEnum.getParentTypeMap();
        for (ModelParentTypeEnum modelParentTypeEnum : ModelParentTypeEnum.values()) {
            ModelNodeDataVO modelNodeDataVO = new ModelNodeDataVO();
            modelNodeDataVO.setLabel(modelParentTypeEnum.getName());
            modelNodeDataVO.setId(modelParentTypeEnum.name());
            List<ModelTypeEnum> modelTypeEnums = modelParentTypeEnumListMap.get(modelParentTypeEnum);
            if (CollectionUtils.isEmpty(modelTypeEnums)) {
                continue;
            }
            List<ModelNodeDataVO> modelTypeNodes = new ArrayList<>();
            modelNodeDataVO.setChildren(modelTypeNodes);
            for (ModelTypeEnum modelTypeEnum : modelTypeEnums) {
                ModelNodeDataVO modelTypeNode = new ModelNodeDataVO();
                modelTypeNode.setLabel(modelTypeEnum.getName());
                modelTypeNode.setId(modelTypeEnum.name());
                modelTypeNodes.add(modelTypeNode);
                if (modelTypeEnumListMap.containsKey(modelTypeEnum)) {
                    List<ModelNodeDataVO> models = new ArrayList<>();
                    modelTypeNode.setChildren(models);
                    modelTypeEnumListMap.get(modelTypeEnum).forEach(model -> {
                                ModelNodeDataVO modelDefine = new ModelNodeDataVO();
                                modelDefine.setId(model.getId());
                                modelDefine.setLabel(model.getModelBasicInfo().getDisplayName());
                                modelDefine.setPath(model.getModelType().name());
                                models.add(modelDefine);
                            }
                    );
                }
            }
            result.add(modelNodeDataVO);
        }
        return result;
    }

    @Override
    public List<LlmModelVO> getLllModelVOByModelName(String modelName) {
        List<LlmModelVO> llmModelVOList = modelManager.getModelList().stream().filter(o -> o instanceof LlamaCppModelDefinition)
                .map(o -> (LlamaCppModelDefinition) o)
                .filter(o -> o.getModelName().equals(modelName)).map(o -> new LlmModelVO(o.getId(), o.getSize())).collect(Collectors.toList());

        return llmModelVOList;
    }

}
