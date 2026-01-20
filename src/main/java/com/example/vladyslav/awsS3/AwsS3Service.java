package com.example.vladyslav.awsS3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import com.example.vladyslav.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AwsS3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region:eu-north-1}")
    private String region;

    @Value("${aws.s3.access.key:}")
    private String accessKey;

    @Value("${aws.s3.secret.key:it clean}")
    private String secretKey;

    // Define allowed file extensions for security
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    public String saveImageToS3(MultipartFile photo) {
        try {
            // Validate file type (must be an image). Checks MIME type
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new NotFoundException("Invalid file type. Only images are allowed.");
            }

            // Validate file extension
            String originalFilename = photo.getOriginalFilename();
            if (originalFilename == null || !isAllowedExtension(originalFilename)) {
                throw new NotFoundException("Invalid file extension. Allowed: " + ALLOWED_EXTENSIONS);
            }

            // Prevent overwriting files: Use unique filename with UUID. Example photo.jpg -> f3a9b123-4567-89ab-cdef-123456789abc.jpg
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String s3Filename = UUID.randomUUID() + fileExtension;

            // Secure AWS credentials usage
            AmazonS3 s3Client = createS3Client();

            try(InputStream inputStream = photo.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                metadata.setContentLength(photo.getSize());


                // Upload to S3
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3Filename, inputStream, metadata);
                s3Client.putObject(putObjectRequest);
            }

            // Use regional virtual-hosted–style URL
            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + s3Filename;

        } catch (Exception e) {
            throw new NotFoundException("Unable to upload image to S3: " + e.getMessage());
        }
    }

    // Method to validate file extension
    private boolean isAllowedExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    // Secure AWS S3 Client creation
    private AmazonS3 createS3Client() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion(region);

        if(accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()){
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            builder = builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        } else {
            builder = builder.withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
        }
        return builder.build();
    }
}
