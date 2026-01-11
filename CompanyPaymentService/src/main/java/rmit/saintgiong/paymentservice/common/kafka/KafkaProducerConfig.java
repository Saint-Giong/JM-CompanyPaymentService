package rmit.saintgiong.paymentservice.common.kafka;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;


@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
            ProducerFactory<String, Object> pf,
            ConcurrentMessageListenerContainer<String, Object> replyContainer
    ) {
        ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate = new ReplyingKafkaTemplate<>(pf, replyContainer);
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));

        return replyingKafkaTemplate;
    }
}