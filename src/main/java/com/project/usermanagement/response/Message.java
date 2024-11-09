package com.project.usermanagement.response;

public class Message {
    private String userId;
    private long timeStamp;
    private String operation;

    public Message(String userId, long timeStamp, String operation) {
        this.userId = userId;
        this.timeStamp = timeStamp;
        this.operation = operation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
