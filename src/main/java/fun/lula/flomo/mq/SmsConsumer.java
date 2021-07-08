package fun.lula.flomo.mq;

import fun.lula.flomo.model.dto.response.SendSmsDto;
import fun.lula.flomo.util.api.SmsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class SmsConsumer {

    @Resource
    private SmsApi smsApi;

    @RabbitListener(queuesToDeclare = @Queue("sms_queue"))
    public void sendSms(SendSmsDto sendSmsDto) {
        log.info("MQ消费者 | 已经发送验证码");
//        smsApi.send(sendSmsDto);
    }
}
