package com.connectsphere.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"
                ));
        return (String) uploadResult.get("secure_url");
    }

    public String uploadVideo(MultipartFile file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "video"
                ));
        return (String) uploadResult.get("secure_url");
    }

    // Auto-detects image vs video by content type — useful for a single
    // post-media upload endpoint that accepts either.
    public String upload(MultipartFile file, String folder) throws IOException {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video")) {
            return uploadVideo(file, folder);
        }
        return uploadImage(file, folder);
    }
}