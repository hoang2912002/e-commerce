package com.fashion.identity.dto.response.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVerifyCodeEvent {
    UUID id;
    String fullName;
    String email;
    String verifyCode;
    String verificationExpiration;


    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InternalUserCreatedEvent extends ApplicationEvent {
        UserVerifyCodeEvent userData;

        public InternalUserCreatedEvent(Object source, UserVerifyCodeEvent userData) {
            super(source);
            this.userData = userData;
        }
    }
}
