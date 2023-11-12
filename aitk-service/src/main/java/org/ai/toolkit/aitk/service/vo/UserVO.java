package org.ai.toolkit.aitk.service.vo;

import java.io.Serializable;

public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String avatar;

    private String displayName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
