package com.zhilian.zr.policy.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.zhilian.zr.policy.config.AliyunOssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfImageService {
    
    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;
    
    private static final float DPI = 150;
    private static final String IMAGE_FORMAT = "png";
    
    public List<String> convertPdfToImages(String sourceOssKey, Long policyId, Long userId) {
        List<String> imageUrls = new ArrayList<>();
        
        try {
            log.info("Downloading PDF from OSS: {}", sourceOssKey);
            
            // 下载PDF
            OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), sourceOssKey);
            byte[] pdfBytes;
            try (InputStream inputStream = ossObject.getObjectContent()) {
                pdfBytes = inputStream.readAllBytes();
            }
            
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
                    
                    String imageOssKey = String.format("policies/%d/%d/images/page_%d.%s", 
                        userId, policyId, i + 1, IMAGE_FORMAT);
                    
                    ossClient.putObject(ossProperties.getBucketName(), imageOssKey, 
                        new ByteArrayInputStream(imageBytes));
                    
                    String imageUrl = generateImageUrl(imageOssKey);
                    imageUrls.add(imageUrl);
                    
                    log.info("Converted page {}/{} to image", i + 1, maxPages);
                }
            } finally {
                document.close();
            }
            
            log.info("Successfully converted PDF to {} images", imageUrls.size());
            return imageUrls;
            
        } catch (Exception e) {
            log.error("Failed to convert PDF to images: {}", sourceOssKey, e);
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
    
    private String generateImageUrl(String ossKey) {
        java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000);
        java.net.URL url = ossClient.generatePresignedUrl(ossProperties.getBucketName(), ossKey, expiration);
        return url.toString();
    }
    
    public PdfPreviewInfo getPdfPreviewInfo(String sourceOssKey) {
        try {
            OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), sourceOssKey);
            byte[] pdfBytes;
            try (InputStream inputStream = ossObject.getObjectContent()) {
                pdfBytes = inputStream.readAllBytes();
            }
            
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
            log.error("Failed to get PDF info: {}", sourceOssKey, e);
            throw new RuntimeException("获取PDF信息失败", e);
        }
    }
    
    public boolean isPdfConverted(String sourceOssKey, Long policyId, Long userId) {
        String prefix = String.format("policies/%d/%d/images/", userId, policyId);
        var listing = ossClient.listObjects(ossProperties.getBucketName(), prefix);
        return listing.getObjectSummaries().size() > 0;
    }
    
    public List<String> getConvertedImageUrls(String sourceOssKey, Long policyId, Long userId) {
        List<String> urls = new ArrayList<>();
        String prefix = String.format("policies/%d/%d/images/", userId, policyId);
        
        var listing = ossClient.listObjects(ossProperties.getBucketName(), prefix);
        for (var summary : listing.getObjectSummaries()) {
            String url = generateImageUrl(summary.getKey());
            urls.add(url);
        }
        
        urls.sort((a, b) -> {
            int pageA = extractPageNumber(a);
            int pageB = extractPageNumber(b);
            return Integer.compare(pageA, pageB);
        });
        
        return urls;
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
