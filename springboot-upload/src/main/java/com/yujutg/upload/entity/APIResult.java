package com.yujutg.upload.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class APIResult<T> {

    private Boolean success;

    private T data;

    private String msg;


    public APIResult() {
    }

    public APIResult(Boolean success) {
        this.success = success;
    }

    public APIResult(Boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public APIResult(Boolean success, T data, String msg) {
        this.success = success;
        this.data = data;
        this.msg = msg;
    }

    public APIResult(Boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public static APIResult SUCCESS(){
        return new APIResult(true);
    }

    public static APIResult FAIL(){
        return new APIResult(false);
    }

    public static APIResult SUCCESS(Object data){
        return new APIResult(true, data);
    }

    public static APIResult SUCCESS(String msg, Object data){
        return new APIResult(true, data, msg);
    }

    public static APIResult FAIL(String msg, Object data){
        return new APIResult(false, data, msg);
    }

    public static APIResult FAIL(String msg){
        return new APIResult(false, msg);
    }


}
