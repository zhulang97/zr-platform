package com.zhilian.zr.policy.util;

import com.zhilian.zr.policy.config.StorageProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfImageService {
    
    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    
    private static final float DPI = 150;
    private static final String IMAGE_FORMAT = "png";
    
    public List<String> convertPdfToImages(String sourceObjectKey, Long policyId, Long userId) {
        List<String> imageUrls = new ArrayList<>();
        
        try {
            log.info("Downloading PDF from MinIO: {}", sourceObjectKey);
            
            // 下载PDF
            byte[] pdfBytes = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .object(sourceObjectKey)
                    .build()
            ).readAllBytes();
            
            log.info("Converting PDF to images...");
            
            // 加载PDF文档
            PDDocument document = org.apache.pdfbox.Loader.loadPDF(pdfBytes);
            try {
                PDFRenderer renderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();
                log.info("PDF has {} pages", pageCount);
                
                int maxPages = Math.min(pageCount, 50);
                
                for (int i = 0; i < maxPages; i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, DPI);
                    BufferedImage compressedImage = compressImage(image);
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(compressedImage, IMAGE_FORMAT, baos);
                    byte[] imageBytes = baos.toByteArray();
                    
                    String imageObjectKey = String.format("policies/%d/%d/images/page_%d.%s", 
                        userId, policyId, i + 1, IMAGE_FORMAT);
                    
                    minioClient.putObject(
                        PutObjectArgs.builder()
                            .bucket(storageProperties.getBucketName())
                            .object(imageObjectKey)
                            .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                            .contentType("image/png")
                            .build()
                    );
                    
                    String imageUrl = generateImageUrl(imageObjectKey);
                    imageUrls.add(imageUrl);
                    
                    log.info("Converted page {}/{} to image", i + 1, maxPages);
                }
            } finally {
                document.close();
            }
            
            log.info("Successfully converted PDF to {} images", imageUrls.size());
            return imageUrls;
            
        } catch (Exception e) {
            log.error("Failed to convert PDF to images: {}", sourceObjectKey, e);
            throw new RuntimeException("PDF转图片失败: " + e.getMessage(), e);
        }
    }
    
    private BufferedImage compressImage(BufferedImage source) {
        int maxWidth = 1200;
        int width = source.getWidth();
        int height = source.getHeight();
        
        if (width > maxWidth) {
            double ratio = (double) maxWidth / width;
            width = maxWidth;
            height = (int) (height * ratio);
        }
        
        BufferedImage compressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        compressed.createGraphics().drawImage(
            source.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH), 
            0, 0, null
        );
        
        return compressed;
    }
    
    private String generateImageUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(storageProperties.getBucketName())
                    .object(objectKey)
                    .expiry(7, TimeUnit.DAYS)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", objectKey, e);
            throw new RuntimeException("生成图片URL失败", e);
        }
    }
    
    public PdfPreviewInfo getPdfPreviewInfo(String sourceObjectKey) {
        try {
            byte[] pdfBytes = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .object(sourceObjectKey)
                    .build()
            ).readAllBytes();
            
            PDDocument document = org.apache.pdfbox.Loader.loadPDF(pdfBytes);
            try {
                PdfPreviewInfo info = new PdfPreviewInfo();
                info.setPageCount(document.getNumberOfPages());
                info.setConverted(false);
                return info;
            } finally {
                document.close();
            }
        } catch (Exception e) {
            log.error("Failed to get PDF info: {}", sourceObjectKey, e);
            throw new RuntimeException("获取PDF信息失败", e);
        }
    }
    
    public boolean isPdfConverted(String sourceObjectKey, Long policyId, Long userId) {
        String prefix = String.format("policies/%d/%d/images/", userId, policyId);
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .prefix(prefix)
                    .recursive(false)
                    .build()
            );
            
            int count = 0;
            for (Result<Item> result : results) {
                count++;
                if (count > 0) break;
            }
            return count > 0;
        } catch (Exception e) {
            log.error("Failed to check PDF conversion status: {}", sourceObjectKey, e);
            return false;
        }
    }
    
    public List<String> getConvertedImageUrls(String sourceObjectKey, Long policyId, Long userId) {
        List<String> urls = new ArrayList<>();
        String prefix = String.format("policies/%d/%d/images/", userId, policyId);
        
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(storageProperties.getBucketName())
                    .prefix(prefix)
                    .recursive(false)
                    .build()
            );
            
            for (Result<Item> result : results) {
                Item item = result.get();
                String url = generateImageUrl(item.objectName());
                urls.add(url);
            }
            
            urls.sort((a, b) -> {
                int pageA = extractPageNumber(a);
                int pageB = extractPageNumber(b);
                return Integer.compare(pageA, pageB);
            });
            
            return urls;
        } catch (Exception e) {
            log.error("Failed to get converted image URLs: {}", sourceObjectKey, e);
            throw new RuntimeException("获取转换图片失败", e);
        }
    }
    
    private int extractPageNumber(String url) {
        try {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            String pageStr = fileName.replace("page_", "").replace(".png", "");
            return Integer.parseInt(pageStr);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public static class PdfPreviewInfo {
        private int pageCount;
        private boolean converted;
        private List<String> imageUrls;
        
        public int getPageCount() { return pageCount; }
        public void setPageCount(int pageCount) { this.pageCount = pageCount; }
        public boolean isConverted() { return converted; }
        public void setConverted(boolean converted) { this.converted = converted; }
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    }
}
