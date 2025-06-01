package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.FilePrivacy;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.FileRepository;
import com.stu.socialnetworkapi.repository.TokenRedisRepository;
import com.stu.socialnetworkapi.repository.UserRepository;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private static final String UPLOAD_DIRECTORY = "upload";

    private final Path root = Paths.get(UPLOAD_DIRECTORY);
    private final JwtUtil jwtUtil;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final TokenRedisRepository tokenRedisRepository;

    @PostConstruct
    private void init() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new ApiException(ErrorCode.STORAGE_INITIALIZATION_ERROR);
        }
    }

    @Override
    public Resource load(String id, String token) {
        try {
            Path file = root.resolve(id);
            Resource resource = new UrlResource(file.toUri());
            UUID userId = tokenRedisRepository.findUserIdByToken(token)
                    .map(UUID::fromString)
                    .orElse(null);
            if (!resource.exists() || !resource.isReadable()) {
                throw new ApiException(ErrorCode.FILE_NOT_FOUND);
            }
            if (fileRepository.canUserReadFile(id, userId)) return resource;
            else throw new ApiException(ErrorCode.UNAUTHORIZED);
        } catch (MalformedURLException e) {
            throw new ApiException(ErrorCode.LOAD_FILE_FAILED);
        }
    }

    @Override
    public String getFilename(String id) {
        return fileRepository.getNameById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.FILE_NOT_FOUND));
    }

    @Override
    public File upload(MultipartFile file, FilePrivacy privacy) {
        if (file.isEmpty()) throw new ApiException(ErrorCode.FILE_REQUIRED);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        try {
            String newFileName = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), root.resolve(newFileName));
            File newFile = File.builder()
                    .id(newFileName)
                    .name(originalFilename)
                    .privacy(privacy)
                    .uploader(getCurrentUserRequiredAuthentication())
                    .build();

            return fileRepository.save(newFile);
        } catch (IOException e) {
            throw new ApiException(ErrorCode.UPLOAD_FILE_FAILED);
        }

    }

    @Override
    public List<File> upload(List<MultipartFile> files, FilePrivacy privacy) {
        if (files == null || files.isEmpty()) {
            throw new ApiException(ErrorCode.FILE_REQUIRED);
        }

        List<File> uploadedFiles = new ArrayList<>();
        User uploader = getCurrentUserRequiredAuthentication();

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) throw new ApiException(ErrorCode.FILE_REQUIRED);

                String originalFilename = file.getOriginalFilename();
                String extension = getFileExtension(originalFilename);
                String newFileName = UUID.randomUUID() + extension;
                Files.copy(file.getInputStream(), root.resolve(newFileName));
                File newFile = File.builder().id(newFileName).name(originalFilename).privacy(privacy).uploader(uploader).build();
                uploadedFiles.add(newFile);
            }

            return fileRepository.saveAll(uploadedFiles);
        } catch (Exception e) {
            // Rollback: delete all uploaded files in case of any error
            rollBackUploadFilesFailed(uploadedFiles);

            // Re-throw the original exception
            if (e instanceof ApiException exception) {
                throw exception;
            } else {
                throw new ApiException(ErrorCode.UPLOAD_FILE_FAILED);
            }
        }
    }

    @Override
    public void modifyPrivacy(String id, FilePrivacy privacy) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUploader().getId().equals(jwtUtil.getUserId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        file.setPrivacy(privacy);
        fileRepository.save(file);
    }

    @Override
    public void modifyPrivacy(List<String> ids, FilePrivacy privacy) {
        List<File> files = fileRepository.findAllById(ids);
        UUID currentUserId = jwtUtil.getUserId();

        for (File file : files) {
            if (!file.getUploader().getId().equals(currentUserId)) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            file.setPrivacy(privacy);
        }

        fileRepository.saveAll(files);
    }

    @Override
    public void deleteFileById(String id) {
        File file = fileRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.FILE_NOT_FOUND));
        deleteFile(file);
    }

    @Override
    public void deleteFile(File file) {
        try {
            Path filePath = root.resolve(file.getId());
            Files.deleteIfExists(filePath);
            fileRepository.delete(file);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", file.getId(), e);
            throw new ApiException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    @Override
    public void deleteFiles(List<File> files) {
        for (File file : files) {
            deleteFile(file);
        }
    }

    @Override
    public void deleteFilesById(List<String> ids) {
        List<File> files = fileRepository.findAllById(ids);
        deleteFiles(files);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = Objects.requireNonNull(filename).lastIndexOf(".");
        if (lastDotIndex == -1) return "";
        return filename.substring(lastDotIndex);
    }

    private void rollBackUploadFilesFailed(List<File> uploadedFiles) {
        for (File uploadedFile : uploadedFiles) {
            try {
                // Delete physical file
                Path filePath = root.resolve(uploadedFile.getId());
                Files.deleteIfExists(filePath);

                // Delete from database
                fileRepository.delete(uploadedFile);
            } catch (IOException deleteEx) {
                // Log this but continue with cleanup
                log.error("Failed to delete file during rollback: {}", uploadedFile.getId());
            }
        }
    }

    private User getCurrentUserRequiredAuthentication() {
        return userRepository.findById(jwtUtil.getUserIdRequiredAuthentication()).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}
