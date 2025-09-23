package com.spoonful.spoonful.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path root;

    public LocalFileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload root: " + root, e);
        }
    }

    @Override
    public Path save(String subdir, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Empty file");
        try {
            Path dir = root.resolve(subdir).normalize();
            Files.createDirectories(dir);
            String original = file.getOriginalFilename();
            String filename = (original == null || original.isBlank()) ? "file" : Paths.get(original).getFileName().toString();

            // ensure uniqueness
            Path target = dir.resolve(filename);
            if (Files.exists(target)) {
                String base = filename;
                String ext = "";
                int dot = filename.lastIndexOf('.');
                if (dot > 0) { base = filename.substring(0, dot); ext = filename.substring(dot); }
                int i = 1;
                while (Files.exists(target)) {
                    target = dir.resolve(base + "_" + i++ + ext);
                }
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().normalize();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    @Override
    public List<Path> list(String subdir) {
        Path dir = root.resolve(subdir).normalize();
        List<Path> out = new ArrayList<>();
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                for (Path p : ds) {
                    if (Files.isRegularFile(p)) out.add(p.toAbsolutePath().normalize());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to list directory " + dir, e);
            }
        }
        return out;
    }

    @Override
    public boolean delete(String subdir, String filename) {
        Path p = root.resolve(subdir).resolve(filename).normalize();
        try {
            return Files.deleteIfExists(p);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + p, e);
        }
    }

    @Override
    public void deleteDirectory(String subdir) {
        Path dir = root.resolve(subdir).normalize();
        if (Files.notExists(dir)) return;
        try {
            Files.walk(dir)
                    .sorted((a,b) -> b.getNameCount() - a.getNameCount()) // delete children first
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException e) { throw new RuntimeException(e); }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete directory " + dir, e);
        }
    }
}