package com.zhilian.zr.policy.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.zhilian.zr.policy.config.AliyunOssProperties;
import com.zhilian.zr.policy.dto.PolicyUploadResponse;
import com.zhilian.zr.policy.entity.PolicyDocument;
import com.zhilian.zr.policy.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyDocumentService {
    
    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;
    private final PolicyMapper policyMapper;
    
    /**
     * 生成OSS上传URL
     */
    public PolicyUploadResponse generateUploadUrl(Long userId, String fileName, String fileType) {
        // 生成唯一的OSS Key
        String policyId = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getFileExtension(fileName);
        String ossKey = String.format("policies/%d/%s/%s.%s", userId, policyId, timestamp, extension);
        
        // 生成预签名URL
        Date expiration = new Date(System.currentTimeMillis() + ossProperties.getPolicyExpiration() * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            ossProperties.getBucketName(), 
            ossKey
        );
        request.setExpiration(expiration);
        request.setMethod(com.aliyun.oss.HttpMethod.PUT);
        
        URL uploadUrl = ossClient.generatePresignedUrl(request);
        
        PolicyUploadResponse response = new PolicyUploadResponse();
        response.setUploadUrl(uploadUrl.toString());
        response.setOssKey(ossKey);
        
        return response;
    }
    
    /**
     * 获取文件访问URL
     */
    public String getFileUrl(String ossKey) {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000); // 1小时有效期
        URL url = ossClient.generatePresignedUrl(ossProperties.getBucketName(), ossKey, expiration);
        return url.toString();
    }
    
    /**
     * 删除OSS文件
     */
    public void deleteFile(String ossKey) {
        ossClient.deleteObject(ossProperties.getBucketName(), ossKey);
    }
    
    /**
     * 从OSS下载文件内容
     */
    public String downloadFileContent(String ossKey) {
        try (var object = ossClient.getObject(ossProperties.getBucketName(), ossKey);
             var inputStream = object.getObjectContent()) {
            return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to download file from OSS: {}", ossKey, e);
            throw new RuntimeException("下载文件失败", e);
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "bin";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
