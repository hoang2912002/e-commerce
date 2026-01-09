package com.fashion.notification.service;

import com.fashion.notification.dto.response.kafka.UserRegisterEvent;
import com.fashion.notification.dto.response.kafka.UserVerifyCodeEvent;

public interface EmailService {
    void sendMailVerifyCode(UserVerifyCodeEvent user);
    void sendMailUserRegister(UserRegisterEvent user);
}
