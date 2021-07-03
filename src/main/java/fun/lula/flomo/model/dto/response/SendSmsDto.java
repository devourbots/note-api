package fun.lula.flomo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendSmsDto implements Serializable {

    private String phone;
    private String message;

}
