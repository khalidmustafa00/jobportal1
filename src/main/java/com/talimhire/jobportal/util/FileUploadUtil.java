package com.talimhire.jobportal.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileUploadUtil {

    private final Cloudinary cloudinary;

    // This constructor reads the keys you put in application.properties
    public FileUploadUtil(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        // Uploads to Cloudinary and returns the URL (the "Ticket")
        return cloudinary.uploader()
                .upload(multipartFile.getBytes(), ObjectUtils.asMap(
                        "resource_type", "auto", // Automatically handles Images and PDFs
                        "folder", "jobportal_users" // Keeps your dashboard organized
                ))
                .get("url")
                .toString();
    }
}