package com.wellsfargo.email.ai.controller;

import com.wellsfargo.email.ai.EmailResponseDTO;
import com.wellsfargo.email.ai.config.EmailHuggingFaceApi;
import com.wellsfargo.email.ai.service.EmailExtractionService;
import com.wellsfargo.email.ai.util.PropertiesUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    public ResponseEntity<List<EmailResponseDTO>> processEmail() throws Exception {
        Resource resource = new FileSystemResource(propertiesUtil.getEmailFolder());
        List<EmailResponseDTO> emailResponseDTOList = new ArrayList<>();
        EmailResponseDTO emailResponseDTO = new EmailResponseDTO();

        File[] emailFiles = resource.getFile().listFiles();
        if(null!= emailFiles) {
            for (File emailFile : emailFiles) {
                if (emailFile.isFile()) {
                    String content = emailExtractionService.extractEmailContent(emailFile);
                    emailResponseDTO =emailHuggingFaceApi.contentClassification(content);
                    emailResponseDTOList.add(emailResponseDTO);
                    System.out.println("Email content-" + emailResponseDTOList);
                }
            }
        }
        return ResponseEntity.ok(emailResponseDTOList);
    }
}


