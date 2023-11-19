package org.ai.toolkit.aitk.service;

import ai.djl.modality.Input;
import java.util.List;
import org.ai.toolkit.aitk.service.vo.ModelAsContactVO;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceCallback;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;

public interface ModelService {

    List<ModelAsContactVO> getAllModels();

    void predict(String modelId, Input input, InferenceCallback callback);

    ModelParamVO getModelParamVO(String modelId);

    ModelAsContactVO getModelAsContactVO(String modelId);

}
