package com.fashion.resource.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fashion.resource.common.annotation.ApiMessageResponse;
import com.fashion.resource.common.annotation.SkipWrapResponse;
import com.fashion.resource.common.enums.FileEnum;
import com.fashion.resource.dto.response.FileDownLoadResponse;
import com.fashion.resource.dto.response.FileResponse;
import com.fashion.resource.service.FileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/files")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileController {
    final FileService fileService;

    @PostMapping("/upload/{valueType}/{ownerId}")
    public ResponseEntity<FileResponse> uploadImage(
        @RequestPart("file") MultipartFile file,
        @PathVariable("valueType") FileEnum valueType,
        @PathVariable("ownerId") String ownerId
    ) { 
        return ResponseEntity.ok(fileService.uploadFile(file, ownerId, valueType));
    }
    
    @GetMapping("/download/{fileName}")
    @ApiMessageResponse("file.success.get.single.name")
    @SkipWrapResponse
    public ResponseEntity<Resource> downloadImage(
        @PathVariable("fileName") String fileName
    ) {
        FileDownLoadResponse file = fileService.downloadImage(fileName);
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_TYPE,
            file.contentType()
        ).body(
            file.resource()
        );
    }
    
}
