package fun.lula.flomo.util.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {
    private boolean success;
    private Integer code;
    private String message;
    private T data;

    ApiResult() {}

    public ApiResult(boolean success, Integer code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApiResult(boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    ApiResult(ResultCode resultCode) {
        this(resultCode, null);
    }

    ApiResult(T data) {
        this(CommonsResultCode.SUCCESS, data);
    }

    ApiResult(ResultCode resultCode, T data) {
        this.success = resultCode.isSuccess();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }


    public static ApiResult SUCCESS() {
        return new ApiResult(CommonsResultCode.SUCCESS);
    }

    public static <T> ApiResult SUCCESS(T data) {
        return new ApiResult(CommonsResultCode.SUCCESS, data);
    }

    public static ApiResult FAIL() {
        return new ApiResult(CommonsResultCode.FAIL);
    }

    public static <T> ApiResult FAIL(T data) {
        return new ApiResult(CommonsResultCode.FAIL, data);
    }

}
