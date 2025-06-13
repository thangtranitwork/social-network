package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.FileResponse;
import com.stu.socialnetworkapi.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileResponse load(String id);

    File upload(MultipartFile file);

    List<File> upload(List<MultipartFile> files);

    void deleteFileById(String id);

    void deleteFile(File file);

    void deleteFiles(List<File> files);

    void deleteFilesById(List<String> ids);
}
