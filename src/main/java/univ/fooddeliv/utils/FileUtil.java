package univ.fooddeliv.utils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUtil {
    private static final String AVATAR_DIR = "uploads/avatars/";
    private static final String CATALOG_DIR = "uploads/catalog/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String saveAvatar(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Размер файла больше 5 МБ");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Можно загрузить только изображение");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + fileExtension;

        Path uploadPath = Paths.get(AVATAR_DIR);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath);

        return newFileName;
    }

    public String saveCatalogImage(MultipartFile file) throws IOException {
        String filename = storeImage(file, CATALOG_DIR);
        return "/uploads/catalog/" + filename;
    }

    private String storeImage(MultipartFile file, String directory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Размер файла больше 5 МБ");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Можно загрузить только изображение");
        }
        String originalFileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = originalFileName.lastIndexOf('.');
        String fileExtension = dot >= 0 ? originalFileName.substring(dot).toLowerCase() : ".jpg";
        if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("Допустимы изображения jpg, png, gif и webp");
        }
        String newFileName = UUID.randomUUID() + fileExtension;
        Path uploadPath = Paths.get(directory);
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(newFileName));
        return newFileName;
    }

    public ResponseEntity<Resource> getAvatar(String filename) throws IOException {
        return readImage(AVATAR_DIR, filename);
    }

    public ResponseEntity<Resource> getCatalogImage(String filename) throws IOException {
        return readImage(CATALOG_DIR, filename);
    }

    private ResponseEntity<Resource> readImage(String directory, String filename) throws IOException {
        if (filename == null || !filename.matches("[a-zA-Z0-9._-]+")) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path filePath = Paths.get(directory).resolve(filename);
            byte[] image = Files.readAllBytes(filePath);
            Resource resource = new ByteArrayResource(image);

            MediaType type = MediaType.IMAGE_JPEG;
            if (filename.endsWith(".png")) {
                type = MediaType.IMAGE_PNG;
            } else if (filename.endsWith(".gif")) {
                type = MediaType.IMAGE_GIF;
            } else if (filename.endsWith(".webp")) {
                type = MediaType.parseMediaType("image/webp");
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(type)
                    .body(resource);
        } catch (NoSuchFileException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public void deleteAvatar(String filename) throws IOException {
        if (filename != null && !filename.isEmpty()) {
            Path filePath = Paths.get(AVATAR_DIR).resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }
}