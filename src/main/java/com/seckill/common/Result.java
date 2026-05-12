package com.seckill.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.code;
        r.message = ResultCode.SUCCESS.message;
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(ResultCode rc) {
        Result<T> r = new Result<>();
        r.code = rc.code;
        r.message = rc.message;
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
