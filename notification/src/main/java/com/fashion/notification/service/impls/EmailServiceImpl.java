package com.fashion.notification.service.impls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fashion.notification.common.enums.EnumError;
import com.fashion.notification.dto.response.kafka.UserRegisterEvent;
import com.fashion.notification.dto.response.kafka.UserVerifyCodeEvent;
import com.fashion.notification.exception.ServiceException;
import com.fashion.notification.service.EmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailServiceImpl implements EmailService{
    final TemplateEngine templateEngine;
    final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String hostMail; 

    @Override
    public void sendMailVerifyCode(UserVerifyCodeEvent user) {
        try {
            Context context = new Context();
            context.setVariable("id", user.getId());
            context.setVariable("fullName", user.getFullName());
            context.setVariable("email", user.getEmail());
            context.setVariable("verifyCode", user.getVerifyCode());
            context.setVariable("verificationExpiration", user.getVerificationExpiration());

            // 1. Render HTML từ template Thymeleaf
            String htmlContent = templateEngine.process("mail/verify-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 2. Thiết lập thông tin người gửi/nhận
            helper.setFrom(hostMail, "Fashion Shop Support"); // Tên hiển thị
            helper.setTo(user.getEmail());
            helper.setSubject("Mã xác thực tài khoản");
            helper.setText(htmlContent, true); // Biến htmlContent lấy từ template ở trên

            // 3. Gửi mail
            mailSender.send(message);
            log.info("Send mail verify code successful to: {}", user.getEmail());
        } catch (ServiceException e){
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.NOTIFICATION_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void sendMailUserRegister(UserRegisterEvent user) {
        try {
            Context context = new Context();
            context.setVariable("id", user.getId());
            context.setVariable("fullName", user.getFullName());
            context.setVariable("email", user.getEmail());
            context.setVariable("verificationAt", user.getVerificationAt());

            String htmlContent = templateEngine.process("mail/welcome-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(hostMail, "Fashion Shop Welcome"); // Tên hiển thị
            helper.setTo(user.getEmail());
            helper.setSubject("Chào mừng bạn đến với Fashion Shop!");
            helper.setText(htmlContent, true); // Biến htmlContent lấy từ template ở trên

            // 3. Gửi mail
            mailSender.send(message);
            log.info("Send mail notification user register successful to: {}", user.getEmail());
        } catch (ServiceException e){
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.NOTIFICATION_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
}
