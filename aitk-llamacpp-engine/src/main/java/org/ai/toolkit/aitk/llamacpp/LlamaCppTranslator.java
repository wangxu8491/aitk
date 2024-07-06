package org.ai.toolkit.aitk.llamacpp;

import ai.djl.ndarray.NDList;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslatorContext;
import de.kherud.llama.LlamaModel;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LlamaCppTranslator<I, O> implements NoBatchifyTranslator<I, O> {

    private Map<String, Method> methodMap = new HashMap<>();

    @Override
    public void prepare(TranslatorContext ctx) {
        Method[] methods = LlamaModel.class.getMethods();
        for (Method method : methods) {
            methodMap.put(method.getName(), method);
        }
    }

    @Override
    public NDList processInput(TranslatorContext ctx, I input) throws Exception {
        LlamaCppModel llamaCppModel = (LlamaCppModel) ctx.getModel();
        LlamaModel llamaModel = llamaCppModel.getModel();
        if (input instanceof LlamaCppInput) {
            LlamaCppInput llamaCppInput = (LlamaCppInput) input;
            Method method = methodMap.get(llamaCppInput.getInferenceMethod().getMethodName());
            Object output = method.invoke(llamaModel, llamaCppInput.getInputParams(llamaCppInput.getInferenceMethod()));
            ctx.setAttachment("out", output);
        }
        return new NDList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public O processOutput(TranslatorContext ctx, NDList list) {
        return (O) ctx.getAttachment("out");
    }


}