package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.enums.FilePrivacy;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    Resource load(String id, String token);
    String getFilename(String id);

    File upload(MultipartFile file, FilePrivacy privacy);

    List<File> upload(List<MultipartFile> files, FilePrivacy privacy);

    void modifyPrivacy(String id, FilePrivacy privacy);

    void modifyPrivacy(List<String> ids, FilePrivacy privacy);

    void deleteFileById(String id);

    void deleteFile(File file);

    void deleteFiles(List<File> files);

    void deleteFilesById(List<String> ids);
}
