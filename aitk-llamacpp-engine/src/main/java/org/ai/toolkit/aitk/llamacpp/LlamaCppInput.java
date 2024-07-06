package org.ai.toolkit.aitk.llamacpp;

import de.kherud.llama.InferenceParameters;

public class LlamaCppInput {

    private InferenceMethod inferenceMethod;

    private InferenceParameters inferenceParameters;

    private String prompt;

    private int[] tokens;

    private LlamaCppInput() {
    }

    public Object[] getInputParams(InferenceMethod inferenceMethod) {
        if (InferenceMethod.COMPLETE.equals(inferenceMethod)) {
            return new Object[]{this.inferenceParameters};
        } else if (InferenceMethod.GENERATE.equals(inferenceMethod)) {
            return new Object[]{this.inferenceParameters};
        } else if (InferenceMethod.EMBED.equals(inferenceMethod)) {
            return new Object[]{this.prompt};
        } else if (InferenceMethod.ENCODE.equals(inferenceMethod)) {
            return new Object[]{this.prompt};
        } else if (InferenceMethod.DECODE.equals(inferenceMethod)) {
            return new Object[]{this.tokens};
        }
        return new Object[]{};
    }

    public static LlamaCppInput getCompleteLlamaCppInput(InferenceParameters parameters) {
        LlamaCppInput llamaCppInput = new LlamaCppInput();
        llamaCppInput.setInferenceParameters(parameters);
        llamaCppInput.setInferenceMethod(InferenceMethod.COMPLETE);
        return llamaCppInput;
    }

    public static LlamaCppInput getGenerateLlamaCppInput(InferenceParameters parameters) {
        LlamaCppInput llamaCppInput = new LlamaCppInput();
        llamaCppInput.setInferenceParameters(parameters);
        llamaCppInput.setInferenceMethod(InferenceMethod.GENERATE);
        return llamaCppInput;
    }

    public static LlamaCppInput getEmbedLlamaCppInput(String prompt) {
        LlamaCppInput llamaCppInput = new LlamaCppInput();
        llamaCppInput.setPrompt(prompt);
        llamaCppInput.setInferenceMethod(InferenceMethod.EMBED);
        return llamaCppInput;
    }

    public static LlamaCppInput getEncodeLlamaCppInput(String prompt) {
        LlamaCppInput llamaCppInput = new LlamaCppInput();
        llamaCppInput.setPrompt(prompt);
        llamaCppInput.setInferenceMethod(InferenceMethod.ENCODE);
        return llamaCppInput;
    }

    public static LlamaCppInput getDecodeLlamaCppInput(int[] tokens) {
        LlamaCppInput llamaCppInput = new LlamaCppInput();
        llamaCppInput.setTokens(tokens);
        llamaCppInput.setInferenceMethod(InferenceMethod.DECODE);
        return llamaCppInput;
    }


    public InferenceMethod getInferenceMethod() {
        return inferenceMethod;
    }

    public void setInferenceMethod(InferenceMethod inferenceMethod) {
        this.inferenceMethod = inferenceMethod;
    }

    public InferenceParameters getInferenceParameters() {
        return inferenceParameters;
    }

    public void setInferenceParameters(InferenceParameters inferenceParameters) {
        this.inferenceParameters = inferenceParameters;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int[] getTokens() {
        return tokens;
    }

    public void setTokens(int[] tokens) {
        this.tokens = tokens;
    }
}
