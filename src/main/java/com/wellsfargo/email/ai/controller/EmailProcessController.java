package com.wellsfargo.email.ai.controller;

import com.wellsfargo.email.ai.service.EmailExtractionService;
import com.wellsfargo.email.ai.util.PropertiesUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
    private ResourceLoader resourceLoader;


    public EmailProcessController(EmailExtractionService emailExtractionService, PropertiesUtil propertiesUtil, ResourceLoader resourceLoader) {
        this.emailExtractionService = emailExtractionService;
        this.propertiesUtil = propertiesUtil;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/process")
    public void processEmail() throws IOException {
        Resource resource = new FileSystemResource(propertiesUtil.getEmailFolder());
        File[] emailFiles = resource.getFile().listFiles();
        for(File emailFile: emailFiles ){
        if(emailFile.isFile()){
            String content = emailExtractionService.extractEmailContent(emailFile);
            System.out.println("File content-"+content);
        }

        }


    }
}


