package top.hellodays.blog_demo1.enums;


/**
 * HTTP状态码枚举类, 建议持续完善(细化)
 */
public enum ResponseCode {

    //定义常量, 常量内设置是参数对应枚举类的两个属性
    SUCCESS(200, "成功"),
    FAIL(400, "失败"),
    UNAUTHORIZED(401, "未认证"),
    NOT_FOUND(404, "接口不存在"),
    METHOD_NOT_ALLOWED(405, "方法不被允许"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /*参数错误:1001-1999*/
    PARAMS_IS_INVALID(1001, "参数无效"),
    PARAMS_IS_BLANK(1002, "参数为空"),
    USER_IS_EXITES(1003, "用户名已存在");

    //状态码的两个属性(包装类)
    private Integer Code;
    private String Msg;

    //带参构造函数
    ResponseCode(Integer code, String msg) {
        this.Code = code;
        this.Msg = msg;
    }

    //枚举类无法使用Lombok
    public Integer getCode() {
        return Code;
    }

    public String getMsg() {
        return Msg;
    }

}
