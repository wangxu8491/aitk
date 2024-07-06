package org.ai.toolkit.aitk.modelmanager;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.serving.wlm.ModelInfo;
import ai.djl.serving.wlm.WorkLoadManager;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.common.git.GitEnum;
import org.ai.toolkit.aitk.common.git.GitUtil;
import org.ai.toolkit.aitk.modelmanager.llm.LlmModelDefine;
import org.ai.toolkit.aitk.modelmanager.llm.ModelDetail;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.ai.toolkit.aitk.modelzoo.ModelRepositoryType;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.llm.LlamaCppModelDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ModelManager implements InitializingBean {

    private static Logger LOGGER = LoggerFactory.getLogger(ModelManager.class);

    private static final String DEFINE_FILE_JSON = "llm/model.json";

    @Autowired
    private ModelRepositoryType modelRepositoryType;

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

    public void loadModel(String modelId, Device device, List<Map<String, ?>> optionList) {
        List<Criteria> criteriaList = ALL_MODEL_MAPPING.get(modelId).getCriteriaList();
        ModelDefinition modelDefinition = ALL_MODEL_MAPPING.get(modelId);
        if (!CollectionUtils.isEmpty(optionList) && optionList.size() != criteriaList.size()) {
            throw new AitkException(AitkErrorCode.MODEL_LOAD_ERROR, "criteriaList.size() not equals optionList.size()");
        }
        for (int i = 0; i < criteriaList.size(); i++) {
            Criteria criteria = criteriaList.get(i);
            if (!CollectionUtils.isEmpty(optionList)) {
                Map<String, ?> options = optionList.get(i);
                if (!CollectionUtils.isEmpty(options)) {
                    Criteria.Builder builder = criteria.toBuilder();
                    options.entrySet().stream().forEach(kv -> builder.optOption(kv.getKey(), String.valueOf(kv.getValue())));
                    criteria = builder.build();
                }
            }
            ModelInfo modelInfo = new ModelInfo(modelId + "_" + i, "", criteria);
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
        for (ModelInfo modelInfo : modelInfoList) {
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
        GitEnum gitEnum = modelRepositoryType.getDefaultGitEnum();
        try {
            GitUtil.gitClone(gitEnum);
        } catch (Exception e) {
            LOGGER.error("gitClone err", e);
        }
        try {
            GitUtil.gitPull(gitEnum);
        } catch (Exception e) {
            LOGGER.error("gitpull err", e);
        }
        String json = GitUtil.getModelBasePath(gitEnum) + File.separator + DEFINE_FILE_JSON;
        Gson gson = new Gson();
        List<LlmModelDefine> llmModelDefines = gson.fromJson(new FileReader(json), new TypeToken<List<LlmModelDefine>>() {}.getType());
        if (!CollectionUtils.isEmpty(llmModelDefines)) {
            for (LlmModelDefine llmModelDefine : llmModelDefines) {
                if (!Objects.isNull(llmModelDefine) && !CollectionUtils.isEmpty(llmModelDefine.getModels())) {
                    for (ModelDetail modelDetail : llmModelDefine.getModels()) {
                        LlamaCppModelDefinition llamaCppModelDefinition = new LlamaCppModelDefinition(llmModelDefine.getName(),
                                modelDetail.getName(), modelDetail.getPath(), new ModelBasicInfo());
                        modelList.add(llamaCppModelDefinition);
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(modelList)) {
            return;
        }
        for (ModelDefinition modelDefinition : modelList) {
            if (ALL_MODEL_MAPPING.containsKey(modelDefinition.getId())) {
                throw new AitkException(AitkErrorCode.KNOWN_ERROR, "modelId is duplicate");
            }
            ALL_MODEL_MAPPING.put(modelDefinition.getId(), modelDefinition);
            loadModel(modelDefinition.getId(), Device.cpu(), null);
        }
    }

    public WorkLoadManager getWorkLoadManager() {
        return workLoadManager;
    }

}
