package com.cryptoAlert;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

public class MailTest {

    public static void main(String[] args) {
        System.out.println("=== 메일 설정 테스트 시작 ===");
        
        // JavaMailSender 직접 생성 및 설정
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("danny.jung1223@gmail.com");
        mailSender.setPassword("nlpp zyvx epvp kccz");
        
        // SMTP 속성 설정
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "true");
        
        System.out.println("Host: " + mailSender.getHost());
        System.out.println("Port: " + mailSender.getPort());
        System.out.println("Username: " + mailSender.getUsername());
        System.out.println("Password: " + (mailSender.getPassword() != null ? "설정됨" : "설정되지 않음"));
        System.out.println("JavaMailProperties: " + mailSender.getJavaMailProperties());
        
        // 메일 발송 테스트
        try {
            System.out.println("\n=== 간단한 메일 발송 테스트 시작 ===");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("jungd1223@naver.com");
            message.setSubject("Crypto Alert Test");
            message.setText("This is a test email from Crypto Alert system.");
            message.setFrom("danny.jung1223@gmail.com");
            
            System.out.println("메일 발송 시도 중...");
            System.out.println("To: " + message.getTo()[0]);
            System.out.println("Subject: " + message.getSubject());
            System.out.println("From: " + message.getFrom());
            
            mailSender.send(message);
            System.out.println("✅ 메일 발송 성공!");
            
        } catch (Exception e) {
            System.out.println("❌ 메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
