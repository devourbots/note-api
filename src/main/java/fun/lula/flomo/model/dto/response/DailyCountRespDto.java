package fun.lula.flomo.model.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DailyCountRespDto {
    private List<Map> dailyCount;
}
