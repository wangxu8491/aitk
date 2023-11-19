package org.ai.toolkit.aitk.modelzoo.executor;

import ai.djl.modality.Input;

public interface InferenceExecutor {

    public <P, Q> void execute(String modelId, Input input, InferenceCallback callback) ;

    public <I, O> O execute(String modelId, I input, Integer modelIndex);
}
