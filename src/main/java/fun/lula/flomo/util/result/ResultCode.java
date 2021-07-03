package fun.lula.flomo.util.result;

public interface ResultCode {
    boolean isSuccess();

    Integer getCode();

    String getMessage();
}
