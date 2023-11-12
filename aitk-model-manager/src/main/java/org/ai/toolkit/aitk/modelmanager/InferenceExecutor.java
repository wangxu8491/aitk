package org.ai.toolkit.aitk.modelmanager;

import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.serving.wlm.Job;
import ai.djl.serving.wlm.ModelInfo;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InferenceExecutor {

    @Autowired
    private ModelManager modelManager;

    public <P, Q> void execute(String modelId, Input input, InferenceCallback callback) {
       try{
           ModelInfo modelInfo = modelManager.getOnlineModelInfo(modelId);
           ModelDefinition<P, Q> modelDefinition = modelManager.getOnlineModelDefinition(modelId);
           P preResult = modelDefinition.postProcessBeforeModel(input);
           CompletableFuture<Q> completableFuture = modelManager.getWorkLoadManager().runJob(new Job<>(modelInfo,
               preResult));
           completableFuture.whenComplete((o, e) -> {
               if (!Objects.isNull(e)) {
                   callback.callback(e, null);
                   return;
               }
               try{
                   Output output = modelDefinition.postProcessAfterModel(input, o);
                   callback.callback(null, output);
               }catch (Throwable t){
                   callback.callback(t, null);
               }
           });
       }catch (Exception e){
           throw new RuntimeException(e);
       }
    }
}
