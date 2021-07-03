package fun.lula.flomo.util.result;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public enum CommonsResultCode implements ResultCode {
    SUCCESS(true, 0, "success"),
    FAIL(false, -1, "error");

    private boolean success;
    private Integer code;
    private String message;


    @Override
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
