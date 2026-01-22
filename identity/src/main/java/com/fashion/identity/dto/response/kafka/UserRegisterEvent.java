package com.fashion.identity.dto.response.kafka;

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
public class UserRegisterEvent {
    UUID id;
    String fullName;
    String email;
    String verificationAt;

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InternalUserRegistedEvent extends ApplicationEvent {
        UserRegisterEvent userData;

        public InternalUserRegistedEvent(Object source, UserRegisterEvent userData) {
            super(source);
            this.userData = userData;
        }
    }
}
