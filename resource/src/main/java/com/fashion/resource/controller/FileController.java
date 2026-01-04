package com.fashion.resource.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/files")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileController {
    
    @GetMapping("")
    public String downLoadMediaFile() {
        return "hello";
    }
    
}
