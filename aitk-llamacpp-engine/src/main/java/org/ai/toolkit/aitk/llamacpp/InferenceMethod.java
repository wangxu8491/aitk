package org.ai.toolkit.aitk.llamacpp;

public enum InferenceMethod {

    COMPLETE("complete"),
    GENERATE("generate"),
    EMBED("embed"),
    ENCODE("encode"),
    DECODE("decode")
    ;

    private String methodName;

    InferenceMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
