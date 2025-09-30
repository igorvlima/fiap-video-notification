package com.example.fiapvideonotification.service;

import com.example.fiapvideonotification.data.VideoStatusMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoNotificationConsumer {
    private final ObjectMapper objectMapper;
    private final EmailSenderService service;

    @KafkaListener(
            topics = "video-status",
            groupId = "video-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record) {
        String messageJson = record.value();
        try {
            VideoStatusMessage message = objectMapper.readValue(messageJson, VideoStatusMessage.class);

            System.out.println("🎥 Processando vídeo: " + message.getVideoId());
            service.sendVideoStatus(message);

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }
}