package com.stu.helloserver.common;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    // 构造方法
    public Result() {}
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 手动添加 Getter 和 Setter
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    // 成功静态方法
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 失败静态方法
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}

