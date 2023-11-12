package org.ai.toolkit.aitk.service.vo;

import java.io.Serializable;

public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String status="succeed";
    private String type;
    private Long sendTime = System.currentTimeMillis();
    private String content;
    private String toContactId;
    private UserVO fromUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToContactId() {
        return toContactId;
    }

    public void setToContactId(String toContactId) {
        this.toContactId = toContactId;
    }

    public UserVO getFromUser() {
        return fromUser;
    }

    public void setFromUser(UserVO fromUser) {
        this.fromUser = fromUser;
    }
}
