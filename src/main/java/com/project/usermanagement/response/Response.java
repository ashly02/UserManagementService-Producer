package com.project.usermanagement.response;

public class Response {
    private String status;
    private Object data;
    private String errors;

    public Response(String status, Object data, String errors) {
        this.status = status;
        this.data = data;
        this.errors = errors;
    }

    public Response(String status, Object data) {
        this.status = status;
        this.data = data;
    }

    public Response() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }
}
