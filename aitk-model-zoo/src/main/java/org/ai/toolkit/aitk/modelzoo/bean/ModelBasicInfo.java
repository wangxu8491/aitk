package org.ai.toolkit.aitk.modelzoo.bean;

import java.io.Serializable;

public class ModelBasicInfo implements Serializable {

    private static final long serialVersionUID = 4304918934741560841L;

    private String displayName;

    private String modelDescription;

    private String modelIcon;

    private String prompt;

    public ModelBasicInfo() {
    }

    public ModelBasicInfo(String displayName, String modelDescription, String modelIcon, String prompt) {
        this.displayName = displayName;
        this.modelDescription = modelDescription;
        this.modelIcon = modelIcon;
        this.prompt = prompt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public void setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
    }

    public String getModelIcon() {
        return modelIcon;
    }

    public void setModelIcon(String modelIcon) {
        this.modelIcon = modelIcon;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
