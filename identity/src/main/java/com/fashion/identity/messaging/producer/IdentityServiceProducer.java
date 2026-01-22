package com.fashion.identity.messaging.producer;

import com.fashion.identity.dto.response.kafka.UserRegisterEvent;
import com.fashion.identity.dto.response.kafka.UserRegisterEvent.InternalUserRegistedEvent;
import com.fashion.identity.dto.response.kafka.UserVerifyCodeEvent;
import com.fashion.identity.dto.response.kafka.UserVerifyCodeEvent.InternalUserCreatedEvent;

public interface IdentityServiceProducer {
    void produceUserEventSuccess(InternalUserCreatedEvent event);
    void produceUserVerifyEventSuccess(InternalUserRegistedEvent event);
}
