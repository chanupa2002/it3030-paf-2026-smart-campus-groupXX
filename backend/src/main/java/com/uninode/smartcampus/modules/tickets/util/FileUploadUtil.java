package com.uninode.smartcampus.modules.tickets.util;

import org.springframework.web.multipart.MultipartFile;
import com.uninode.smartcampus.modules.tickets.exception.InvalidFileTypeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FileUploadUtil {

    private static final String UPLOAD_DIR = "uploads/tickets";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList("image/jpeg", "image/png"));

    public static void validateFile(MultipartFile file) throws InvalidFileTypeException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileTypeException("File size exceeds maximum limit of 5 MB");
        }

        // Check file extension
        String fileName = file.getOriginalFilename();
        if (fileName == null || !hasValidExtension(fileName)) {
            throw new InvalidFileTypeException("Invalid file type. Only JPG and PNG files are allowed.");
        }

        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new InvalidFileTypeException("Invalid MIME type. Only JPG and PNG files are allowed.");
        }
    }

    public static String saveFile(MultipartFile file) throws IOException, InvalidFileTypeException {
        validateFile(file);

        // Create absolute uploads directory under the application working directory
        Path uploadPath = Paths.get(System.getProperty("user.dir")).resolve(UPLOAD_DIR).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID() + "." + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFileName);
        file.transferTo(filePath.toFile());

        return filePath.toAbsolutePath().toString();
    }

    public static void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            // Log error but don't throw
            System.err.println("Failed to delete file: " + filePath + " - " + e.getMessage());
        }
    }

    private static boolean hasValidExtension(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    public static String getFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return contentType;
        }
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        }
        return "image/jpeg";
    }
}
