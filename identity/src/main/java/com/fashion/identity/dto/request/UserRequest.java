package com.fashion.identity.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fashion.identity.common.enums.GenderEnum;
import com.fashion.identity.dto.request.AddressRequest.InnerAddressRequest;
import com.fashion.identity.dto.request.RoleRequest.InnerRoleRequest;
import com.fashion.identity.dto.response.RoleResponse;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    public interface Create {}
    public interface Update {}

    @NotBlank(message = "user.id.notNull", groups = Update.class)
    String id;

    @NotNull(message = "user.userName.notNull", groups = {Create.class, Update.class})
    LocalDate dob;

    @NotBlank(message = "user.email.notNull", groups = {Create.class, Update.class})
    @Email(
        message = "user.email.invalid", 
        regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
        groups = {Create.class, Update.class}
    )
    String email;

    @NotBlank(message = "user.fullName.notNull", groups = {Create.class, Update.class})
    String fullName;

    @NotNull(message = "user.gender.notNull", groups = {Create.class, Update.class})
    GenderEnum gender;

    @NotBlank(message = "user.phoneNumber.notNull", groups = {Create.class, Update.class})
    @Pattern(
        regexp = "^(0|\\+84)(\\d{9})$", 
        message = "user.phoneNumber.invalid",
        groups = {Create.class, Update.class}
    )
    String phoneNumber;

    @NotBlank(message = "user.userName.notNull", groups = {Create.class, Update.class})
    String userName;

    @NotBlank(message = "user.password.notNull", groups = {Create.class, Update.class})
    @Size(message = "user.password.limit", min = 6, groups = {Create.class, Update.class})
    String password;
    
    InnerRoleRequest role;

    List<InnerAddressRequest> addresses;
}
