package com.fashion.resource.service;

import org.springframework.web.multipart.MultipartFile;

import com.fashion.resource.common.enums.FileEnum;
import com.fashion.resource.dto.response.FileDownLoadResponse;
import com.fashion.resource.dto.response.FileResponse;

public interface FileService {
    FileResponse uploadFile(MultipartFile file, String ownerId, FileEnum valueType);
    FileDownLoadResponse downloadImage(String fileName);
}
