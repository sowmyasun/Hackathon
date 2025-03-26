package com.wellsfargo.email.ai.controller;

import com.wellsfargo.email.ai.config.EmailHuggingFaceApi;
import com.wellsfargo.email.ai.service.EmailExtractionService;
import com.wellsfargo.email.ai.util.PropertiesUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/email")
public class EmailProcessController {

    private EmailExtractionService emailExtractionService;
    private PropertiesUtil propertiesUtil;
    private EmailHuggingFaceApi emailHuggingFaceApi;


    public EmailProcessController(EmailExtractionService emailExtractionService, PropertiesUtil propertiesUtil, EmailHuggingFaceApi emailHuggingFaceApi) {
        this.emailExtractionService = emailExtractionService;
        this.propertiesUtil = propertiesUtil;
        this.emailHuggingFaceApi = emailHuggingFaceApi;
    }

    @GetMapping("/process")
    public void processEmail() throws Exception {
        Resource resource = new FileSystemResource(propertiesUtil.getEmailFolder());
        File[] emailFiles = resource.getFile().listFiles();
        if(null!= emailFiles) {
            for (File emailFile : emailFiles) {
                if (emailFile.isFile()) {
                    String content = emailExtractionService.extractEmailContent(emailFile);
                    emailHuggingFaceApi.contentClassification(content);
                    System.out.println("File content-" + content);
                }

            }
        }


    }
}


