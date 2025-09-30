package com.example.fiapvideonotification.service;

import com.example.fiapvideonotification.data.VideoStatusMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSenderService emailSenderService;

    private final String FROM_EMAIL = "test@example.com";
    private final String CUSTOMER_EMAIL = "customer@example.com";
    private final UUID VIDEO_ID = UUID.randomUUID();
    private final String VIDEO_NAME = "Test Video";

    @BeforeEach
    void setUp() {
        // Set the FROM_EMAIL field in the service using reflection
        ReflectionTestUtils.setField(emailSenderService, "FROM_EMAIL", FROM_EMAIL);
    }

    @Test
    void sendVideoStatus_whenStatusIsCompleted_shouldSendEmailWithDownloadLink() {
        // Arrange
        VideoStatusMessage message = VideoStatusMessage.builder()
                .videoId(VIDEO_ID)
                .videoName(VIDEO_NAME)
                .customerEmail(CUSTOMER_EMAIL)
                .videoStatus("COMPLETED")
                .build();

        // Act
        emailSenderService.sendVideoStatus(message);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(CUSTOMER_EMAIL, sentMessage.getTo()[0]);
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        assertEquals("Atualização do Vídeo: " + VIDEO_NAME, sentMessage.getSubject());
        
        String expectedBodyPart = "Seu vídeo " + VIDEO_NAME + " foi processado com sucesso! Você pode baixar o arquivo ZIP com os resultados aqui: ";
        String downloadLink = "http://localhost:30080/api/v1/videos/" + VIDEO_ID + "?customerEmail=" + CUSTOMER_EMAIL;
        
        assertTrue(sentMessage.getText().contains(expectedBodyPart));
        assertTrue(sentMessage.getText().contains(downloadLink));
        assertTrue(sentMessage.getText().contains("Obrigado por usar nosso serviço de processamento de vídeos."));
    }

    @Test
    void sendVideoStatus_whenStatusIsError_shouldSendEmailWithErrorMessage() {
        // Arrange
        VideoStatusMessage message = VideoStatusMessage.builder()
                .videoId(VIDEO_ID)
                .videoName(VIDEO_NAME)
                .customerEmail(CUSTOMER_EMAIL)
                .videoStatus("ERROR")
                .build();

        // Act
        emailSenderService.sendVideoStatus(message);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(CUSTOMER_EMAIL, sentMessage.getTo()[0]);
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        assertEquals("Atualização do Vídeo: " + VIDEO_NAME, sentMessage.getSubject());
        
        String expectedBodyPart = "Infelizmente, houve um erro ao processar seu vídeo. Tente novamente mais tarde";
        
        assertTrue(sentMessage.getText().contains(expectedBodyPart));
        assertTrue(sentMessage.getText().contains("Obrigado por usar nosso serviço de processamento de vídeos."));
    }

    @Test
    void sendVideoStatus_whenStatusIsUploaded_shouldSendEmailWithProcessingMessage() {
        // Arrange
        VideoStatusMessage message = VideoStatusMessage.builder()
                .videoId(VIDEO_ID)
                .videoName(VIDEO_NAME)
                .customerEmail(CUSTOMER_EMAIL)
                .videoStatus("UPLOADED")
                .build();

        // Act
        emailSenderService.sendVideoStatus(message);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(CUSTOMER_EMAIL, sentMessage.getTo()[0]);
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        assertEquals("Atualização do Vídeo: " + VIDEO_NAME, sentMessage.getSubject());
        
        String expectedBodyPart = "Seu vídeo está atualmente em processamento. Por favor, aguarde a conclusão.";
        
        assertTrue(sentMessage.getText().contains(expectedBodyPart));
        assertTrue(sentMessage.getText().contains("Obrigado por usar nosso serviço de processamento de vídeos."));
    }

    @Test
    void sendVideoStatus_whenStatusIsOther_shouldSendBasicEmail() {
        // Arrange
        VideoStatusMessage message = VideoStatusMessage.builder()
                .videoId(VIDEO_ID)
                .videoName(VIDEO_NAME)
                .customerEmail(CUSTOMER_EMAIL)
                .videoStatus("SOME_OTHER_STATUS")
                .build();

        // Act
        emailSenderService.sendVideoStatus(message);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(CUSTOMER_EMAIL, sentMessage.getTo()[0]);
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        assertEquals("Atualização do Vídeo: " + VIDEO_NAME, sentMessage.getSubject());
        
        // Should not contain any of the specific status messages
        String body = sentMessage.getText();
        assertTrue(!body.contains("foi processado com sucesso"));
        assertTrue(!body.contains("houve um erro ao processar"));
        assertTrue(!body.contains("está atualmente em processamento"));
        
        // Should contain the basic information
        assertTrue(body.contains("O status do seu vídeo '" + VIDEO_NAME + "' foi atualizado para: SOME_OTHER_STATUS"));
        assertTrue(body.contains("Obrigado por usar nosso serviço de processamento de vídeos."));
    }
}