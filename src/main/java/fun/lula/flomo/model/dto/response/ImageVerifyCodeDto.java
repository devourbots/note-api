package fun.lula.flomo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageVerifyCodeDto {
    private String tokenId;
    private String images;
}
