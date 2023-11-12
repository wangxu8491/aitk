package org.ai.toolkit.aitk.modelmanager;

import ai.djl.modality.Output;

public interface InferenceCallback {

    void callback(Throwable throwable, Output output);
}
