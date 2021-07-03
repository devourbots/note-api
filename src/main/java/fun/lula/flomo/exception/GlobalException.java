package fun.lula.flomo.exception;

import cn.dev33.satoken.exception.NotLoginException;
import fun.lula.flomo.util.result.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler(value = Exception.class)
    public ApiResult handlerException(Exception ex) {
        ex.printStackTrace();
        if (ex instanceof NotLoginException) {
            log.info("进入全局异常{}", ex.getMessage());
            return ApiResult.FAIL(ex.getMessage());
        } else if (ex instanceof ServiceException) {
            return ApiResult.FAIL(ex.getMessage());
        } else {
            return ApiResult.FAIL("服务器异常");
        }
    }


    @ExceptionHandler(value = ServiceException.class)
    public ApiResult serviceExceptionHandler(ServiceException serviceException) {
        return new ApiResult(false, -1, serviceException.getMessage());
    }

    @ExceptionHandler(value = NotLoginException.class)
    public ApiResult notLoginExceptionHandler(NotLoginException ex) {
        ex.printStackTrace();
        String message;
        if (ex.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = NotLoginException.NOT_TOKEN_MESSAGE;
        } else if (ex.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = NotLoginException.INVALID_TOKEN_MESSAGE;
        } else if (ex.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = NotLoginException.TOKEN_TIMEOUT_MESSAGE;
        } else if (ex.getType().equals(NotLoginException.BE_REPLACED)) {
            message = NotLoginException.BE_REPLACED_MESSAGE;
        } else if (ex.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = NotLoginException.INVALID_TOKEN_MESSAGE;
        } else {
            message = "当前会话未登录";
        }
        return ApiResult.FAIL(message);
    }

}
