package top.hellodays.blog_demo1.model.vo;

import lombok.Data;
import top.hellodays.blog_demo1.enums.ResponseCode;

import java.io.Serializable;


@Data
public class Response<T> implements Serializable {

    //核心返回的信息主要是三个(状态码, 状态码信息, 数据)
    private Integer code;
    private String msg;
    private T data;
    //附加一个相应是否成功的布尔判断属性
    private boolean success = true;

    //构造器部分
    //默认成功无参构造函数(假设不传入任何数据)
    private Response() {
        this.code = 200;
        this.success = true;
    }

    //默认成功带参构造函数(传入数据)
    private Response(T obj) {
        this.code = 200;
        this.data = obj;
        this.success = true;
    }

    //失败带参(枚举)构造函数
    private Response(ResponseCode responseCode) {
        this.success = false;
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
    }

    //失败带参(枚举)构造函数(但是自定义失败信息)
    private Response(ResponseCode responseCode, String message) {
        this.success = false;
        this.code = responseCode.getCode();
        this.msg = message;
    }

    //方法部分
    //调用默认无参成功构造器返回结果
    public static <T> Response<T> success() {
        return new Response();
    }

    //调用默认带参成功构造器返回结果
    public static <T> Response<T> success(T data) {
        return new Response<T>(data);
    }

    //调用带参(枚举)构造器返回结果
    public static <T> Response<T> failure(ResponseCode responseCode) {
        return new Response<T>(responseCode);
    }

    //调用带参(枚举)构造器(但是自定义失败信息)返回结果
    public static <T> Response<T> failure(ResponseCode responseCode, String message) {
        return new Response<T>(responseCode, message);
    }

    //重写toString方法将信息输出到控制台当中
    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }

}