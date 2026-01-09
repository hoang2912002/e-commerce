package com.fashion.identity.messaging.producer;

import com.fashion.identity.dto.response.kafka.UserRegisterEvent;
import com.fashion.identity.dto.response.kafka.UserVerifyCodeEvent;

public interface IdentityServiceProducer {
    void produceUserEventSuccess(UserVerifyCodeEvent userData);
    void produceUserVerifyEventSuccess(UserRegisterEvent userData);
}
