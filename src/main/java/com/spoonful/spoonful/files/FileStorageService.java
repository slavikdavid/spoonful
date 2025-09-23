package com.spoonful.spoonful.files;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface FileStorageService {
    Path save(String subdir, MultipartFile file);
    List<Path> list(String subdir);
    boolean delete(String subdir, String filename);
    void deleteDirectory(String subdir);
}