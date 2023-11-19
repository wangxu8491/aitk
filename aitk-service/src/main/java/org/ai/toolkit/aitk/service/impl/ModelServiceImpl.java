package org.ai.toolkit.aitk.service.impl;

import ai.djl.modality.Input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.ai.toolkit.aitk.modelzoo.executor.InferenceExecutor;
import org.ai.toolkit.aitk.service.vo.ModelAsContactVO;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceCallback;
import org.ai.toolkit.aitk.modelmanager.ModelManager;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.ai.toolkit.aitk.service.ModelService;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ModelServiceImpl implements ModelService, InitializingBean {

    @Autowired
    private ModelManager modelManager;
    @Autowired
    private InferenceExecutor inferenceExecutor;

    private static final Map<String, ModelAsContactVO> MODEL_AS_CONTACT_VO_MAP = new HashMap<>();

    @Override
    public List<ModelAsContactVO> getAllModels() {
        return MODEL_AS_CONTACT_VO_MAP.entrySet().stream().map(Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public void predict(String modelId, Input input, InferenceCallback callback) {
        inferenceExecutor.execute(modelId, input, callback);
    }

    @Override
    public ModelParamVO getModelParamVO(String modelId) {
        ModelDefinition modelDefinition = modelManager.getOnlineModelDefinition(modelId);
        return new ModelParamVO(modelDefinition.getLoadModelParams(), modelDefinition.getRequestParams(),
            modelDefinition.getResponseParams(), modelDefinition.getModelBasicInfo());
    }

    @Override
    public ModelAsContactVO getModelAsContactVO(String modelId) {
        return MODEL_AS_CONTACT_VO_MAP.get(modelId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<ModelDefinition> modelDefinitionList = modelManager.getModelList();
        if (CollectionUtils.isEmpty(modelDefinitionList)) {
            return;
        }
        for (ModelDefinition modelDefinition : modelDefinitionList) {
            ModelAsContactVO modelAsContactVO = new ModelAsContactVO();
            modelAsContactVO.setId(modelDefinition.getId());
            modelAsContactVO.setDisplayName(modelDefinition.getModelBasicInfo().getDisplayName());
            modelAsContactVO.setUnread(0);
            modelAsContactVO.setIndex(modelDefinition.getModelType().name());
            modelAsContactVO.setLastSendTime(System.currentTimeMillis());
            modelAsContactVO.setAvatar(modelDefinition.getModelBasicInfo().getModelIcon());
            MODEL_AS_CONTACT_VO_MAP.put(modelDefinition.getId(), modelAsContactVO);
        }
    }
}
