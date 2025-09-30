package com.example.fiapvideonotification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String FROM_EMAIL;

    private void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setFrom(FROM_EMAIL);
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        mailSender.send(mailMessage);
    }

    public void sendVideoStatus(VideoStatusMessage message){
        String subject = "Atualização do Vídeo: " + message.getVideoName();
        String body = "Olá,\n\n" +
                "O status do seu vídeo '" + message.getVideoName() + "' foi atualizado para: " + message.getVideoStatus() + ".\n\n";

        if ("COMPLETED".equalsIgnoreCase(message.getVideoStatus())) {
            body += "Seu vídeo "+message.getVideoName()+" foi processado com sucesso! Você pode baixar o arquivo ZIP com os resultados aqui: "
                    + "http://localhost:30080/api/v1/videos/"
                    + message.getVideoId()
                    + "?customerEmail=" + message.getCustomerEmail()
                    + "\n\n";
        } else if ("ERROR".equalsIgnoreCase(message.getVideoStatus())) {
            body += "Infelizmente, houve um erro ao processar seu vídeo. Tente novamente mais tarde" + "\n\n";
        } else if( "UPLOADED".equalsIgnoreCase(message.getVideoStatus())) {
            body += "Seu vídeo está atualmente em processamento. Por favor, aguarde a conclusão.\n\n";
        }

        body += "Obrigado por usar nosso serviço de processamento de vídeos.\n" +
                "Atenciosamente,\n" +
                "Fiap X";

        sendEmail(message.getCustomerEmail(), subject, body);
    }

}