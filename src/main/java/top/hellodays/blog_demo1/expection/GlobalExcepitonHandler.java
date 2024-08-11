package top.hellodays.blog_demo1.expection;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.hellodays.blog_demo1.model.vo.Response;
import top.hellodays.blog_demo1.enums.ResponseCode;

import java.io.IOException;

/**
 * 全局异常处理类
 * 顾名思义, 该类会处理被指定过的所有异常
 * 第一步, 先回顾一下Java的异常?
 * https://javabetter.cn/exception/gailan.html#_04%E3%80%81%E5%85%B3%E4%BA%8E-throw-%E5%92%8C-throws
 * 该类中出现的新注解都是为了处理异常而生
 * 参考: https://blog.csdn.net/qq_63431773/article/details/133493542
 *
 * @Slf4j注解可以简单理解而成日志记录, 但是一键偷懒
 * 参考: https://www.cnblogs.com/xrq730/p/8619156.html
 *
 * 最后, 全局异常是前端友好的, 你看返回值就知道是发给前端看的了, 同时也是后端友好的, 这样能根据日志快速定位问题
 */

@Slf4j
@RestControllerAdvice
public class GlobalExcepitonHandler {

    // 捕捉shiro的异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public Response handle401(ShiroException e) {
        return Response.failure(ResponseCode.UNAUTHORIZED, e.getMessage());
    }

    /**
     * 处理Assert的异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Response handler(IllegalArgumentException e) throws IOException {
        log.error("Assert异常:-------------->{}", e.getMessage());
        return Response.failure(ResponseCode.UNAUTHORIZED, e.getMessage());
    }

    /**
     * @Validated 校验错误异常处理
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Response handler(MethodArgumentNotValidException e) throws IOException {
        log.error("运行时异常:-------------->", e);
        BindingResult bindingResult = e.getBindingResult();
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
        return Response.failure(ResponseCode.FAIL, objectError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)
    public Response handler(RuntimeException e) throws IOException {
        log.error("运行时异常:-------------->", e);
        return Response.failure(ResponseCode.FAIL, e.getMessage());
    }

}
