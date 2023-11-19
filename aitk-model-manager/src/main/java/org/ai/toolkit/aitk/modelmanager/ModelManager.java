package org.ai.toolkit.aitk.modelmanager;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.serving.wlm.ModelInfo;
import ai.djl.serving.wlm.WorkLoadManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ModelManager implements InitializingBean {

    private static Logger LOGGER = LoggerFactory.getLogger(ModelManager.class);

    @Autowired
    private List<ModelDefinition> modelList;

    private static final Map<String, ModelDefinition> ALL_MODEL_MAPPING = new ConcurrentHashMap<>();

    private static final WorkLoadManager workLoadManager = new WorkLoadManager();

    private static final Map<String, List<ModelInfo>> ONLINE_MODEL_MAPPING = new ConcurrentHashMap<>();


    public List<ModelDefinition> getModelList() {
        return modelList;
    }

    public ModelDefinition getOnlineModelDefinition(String modelId) {
        if (!ONLINE_MODEL_MAPPING.containsKey(modelId)) {
            throw new AitkException(AitkErrorCode.MODEL_NOT_LOADED_ERROR);
        }
        return ALL_MODEL_MAPPING.get(modelId);
    }

    public ModelInfo getOnlineModelInfo(String modelId) {
        if (!ONLINE_MODEL_MAPPING.containsKey(modelId)) {
            throw new AitkException(AitkErrorCode.MODEL_NOT_LOADED_ERROR);
        }
        return ONLINE_MODEL_MAPPING.get(modelId).get(0);
    }

    public List<ModelInfo> getOnlineModelInfoList(String modelId) {
        if (!ONLINE_MODEL_MAPPING.containsKey(modelId)) {
            throw new AitkException(AitkErrorCode.MODEL_NOT_LOADED_ERROR);
        }
        return ONLINE_MODEL_MAPPING.get(modelId);
    }


    public void loadModel(String modelId, Device device) {
        List<Criteria> criteriaList = ALL_MODEL_MAPPING.get(modelId).getCriteriaList();
        ModelDefinition modelDefinition = ALL_MODEL_MAPPING.get(modelId);
        for (int i = 0; i < criteriaList.size(); i++) {
            ModelInfo modelInfo = new ModelInfo(modelId + "_" + i, "", criteriaList.get(i));
            try {
                Field engineNameField = ModelInfo.class.getDeclaredField("engineName");
                engineNameField.setAccessible(true);
                List<EngineEnum> engineEnumList = modelDefinition.getEngineList();
                engineNameField.set(modelInfo, engineEnumList.get(i).name());
                engineNameField.setAccessible(false);
                workLoadManager.registerWorkerPool(modelInfo).initWorkers(String.valueOf(device.getDeviceId()));
                if (ONLINE_MODEL_MAPPING.containsKey(modelId)) {
                    ONLINE_MODEL_MAPPING.get(modelId).add(modelInfo);
                } else {
                    List<ModelInfo> list = new ArrayList<>();
                    list.add(modelInfo);
                    ONLINE_MODEL_MAPPING.put(modelId, list);
                }
            } catch (Exception e) {
                LOGGER.error("loadModel", e);
                throw new AitkException(AitkErrorCode.MODEL_LOAD_ERROR, e);
            }
        }

    }

    public void unloadModel(String modelId) {
        if (!ONLINE_MODEL_MAPPING.containsKey(modelId)) {
            return;
        }
        List<ModelInfo> modelInfoList = ONLINE_MODEL_MAPPING.get(modelId);
        for (ModelInfo modelInfo:modelInfoList){
            Map<Device, ZooModel> models = modelInfo.getModels();
            if (!CollectionUtils.isEmpty(models)) {
                for (Model m : models.values()) {
                    m.close();
                }
                models.clear();
            }
            workLoadManager.unregisterWorkerPool(modelInfo);
        }
        ONLINE_MODEL_MAPPING.remove(modelId);

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(modelList)) {
            return;
        }
        for (ModelDefinition modelDefinition : modelList) {
            if (ALL_MODEL_MAPPING.containsKey(modelDefinition.getId())) {
                throw new AitkException(AitkErrorCode.KNOWN_ERROR, "modelId is duplicate");
            }
            ALL_MODEL_MAPPING.put(modelDefinition.getId(), modelDefinition);
            loadModel(modelDefinition.getId(), Device.cpu());
        }
    }

    public WorkLoadManager getWorkLoadManager() {
        return workLoadManager;
    }

}
