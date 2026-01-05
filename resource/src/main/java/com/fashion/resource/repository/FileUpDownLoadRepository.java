package com.fashion.resource.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fashion.resource.common.enums.FileEnum;
import com.fashion.resource.dto.response.FileResponse;
import com.fashion.resource.entity.File;

import ch.qos.logback.core.util.MD5Util;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FileUpDownLoadRepository {
    
    @Value("${app.file.storage-dir}")
    String storage;
    
    @Value("${app.file.download-prefix}")
    String prefixUrlDownload;

    public FileResponse storageImage(
        MultipartFile file,
        String ownerId,
        FileEnum valueType
    ){
        try {
            // 1. Lấy folderPath từ Enum: "user", "product
            String subFolder = valueType.getFolderPath();

            // 2. Tạo đường dẫn đầy đủ đến thư mục đích
            Path folderPath = Paths.get(storage, subFolder).normalize();

            // 3. Kiểm tra và tạo thư mục nếu chưa có
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // 4. Tạo tên file
            String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String prefixName = valueType.name() + "__" + ownerId + "__" + System.currentTimeMillis();
            String fileName = (fileExtension == null) ? 
                prefixName : 
                prefixName + "." + fileExtension;

            // 5. Xác định đường dẫn file cuối cùng và thực hiện copy
            Path filePath = folderPath.resolve(fileName).normalize().toAbsolutePath();
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return FileResponse.builder()
                .contentType(file.getContentType())
                .size(file.getSize())
                .name(fileName)
                .md5Checksum(DigestUtils.md5DigestAsHex(file.getInputStream()))
                .path(filePath.toString())
                .ownerId(ownerId)
                .valueType(valueType)
                .acceptUrl(prefixUrlDownload + fileName)
                .build();
        } catch (IOException e){
            log.error("RESOURCE-SERVICE: storageImage(): IOException: {}", e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            log.error("RESOURCE-SERVICE: storageImage(): Exception: {}", e.getMessage());
            throw e;
        }
        return null;
    }

    public Resource readImage(File file){
        try {
            var data = Files.readAllBytes(Path.of(file.getPath()));
            return new ByteArrayResource(data);
            
        } catch (IOException e) {
            log.error("RESOURCE-SERVICE: readImage(): IOException: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("RESOURCE-SERVICE: readImage(): Exception: {}", e.getMessage());
            throw e;
        }
        return null;
    }
}
