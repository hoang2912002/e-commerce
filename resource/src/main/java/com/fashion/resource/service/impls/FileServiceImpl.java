package com.fashion.resource.service.impls;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fashion.resource.common.enums.EnumError;
import com.fashion.resource.common.enums.FileEnum;
import com.fashion.resource.dto.response.FileDownLoadResponse;
import com.fashion.resource.dto.response.FileResponse;
import com.fashion.resource.entity.File;
import com.fashion.resource.exception.ServiceException;
import com.fashion.resource.mapper.FileMapper;
import com.fashion.resource.repository.FileRepository;
import com.fashion.resource.repository.FileUpDownLoadRepository;
import com.fashion.resource.service.FileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService{
    FileUpDownLoadRepository fileUpDownLoadRepository;
    FileRepository fileRepository;
    FileMapper fileMapper;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public FileResponse uploadFile(MultipartFile file, String ownerId, FileEnum valueType) {
        try {
            // Lưu ảnh
            FileResponse fileResponse = this.fileUpDownLoadRepository.storageImage(file, ownerId, valueType);

            File fileSave = fileRepository.save(fileMapper.toEntity(fileResponse));

            FileResponse returnFile = fileMapper.toDto(fileSave);
            returnFile.setAcceptUrl(fileResponse.getAcceptUrl());
            return returnFile;
        } catch (ServiceException e){
            log.error("RESOURCE-SERVICE: uploadFile(): ServiceException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("RESOURCE-SERVICE: uploadFile(): Exception: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public FileDownLoadResponse downloadImage(String fileName) {
        try {
            File file = fileRepository.findByName(fileName).orElseThrow(
                () -> new ServiceException(EnumError.RESOURCE_FILE_ERR_NOT_FOUND_NAME,"file.not.found.name", Map.of("name", fileName))
            );
            return new FileDownLoadResponse(file.getContentType(), fileUpDownLoadRepository.readImage(file));
        } catch (ServiceException e){
            log.error("RESOURCE-SERVICE: downloadImage(): ServiceException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("RESOURCE-SERVICE: downloadImage(): Exception: {}", e.getMessage());
            throw e;
        }
    }
    
}
