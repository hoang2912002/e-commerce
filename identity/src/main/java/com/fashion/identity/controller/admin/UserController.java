package com.fashion.identity.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.identity.common.annotation.ApiMessageResponse;
import com.fashion.identity.dto.request.UserRequest;
import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.UserResponse;
import com.fashion.identity.entity.User;
import com.fashion.identity.mapper.UserMapper;
import com.fashion.identity.service.UserService;

import feign.Response;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    UserMapper userMapper;

    @GetMapping("")
    @ApiMessageResponse("user.success.get.all")
    public ResponseEntity<PaginationResponse<List<UserResponse>>> getAllUsers(
        @ModelAttribute UserSearchRequest request
    ) {
        return ResponseEntity.ok(this.userService.getAllUsers(request));
    }


    @PostMapping("")
    @ApiMessageResponse("user.success.create")
    public ResponseEntity<UserResponse> createUser(
        @RequestBody @Validated(UserRequest.Create.class) UserRequest user
    ) throws Exception {
        User createUser = userMapper.toValidated(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUser(createUser));
    }

    @PutMapping("")
    @ApiMessageResponse("user.success.update")
    public ResponseEntity<UserResponse> updateUser(
        @RequestBody @Validated(UserRequest.Update.class) UserRequest user
    ) {
        User updateUser = userMapper.toValidated(user);
        return ResponseEntity.ok(this.userService.updateUser(updateUser));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("user.success.get.single")
    public ResponseEntity<UserResponse> getUserById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(this.userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @ApiMessageResponse("user.success.delete")
    public void deleteUserById(
        @PathVariable("id") UUID id
    ){
        this.userService.deleteUserById(id);
    }
}
