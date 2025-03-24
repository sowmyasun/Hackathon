package com.wellsfargo.email.ai.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesUtil {

    @Value("${email.ai.huggingface.base-url}")
    private String apiUrl;
    @Value("${email.ai.huggingface.api-key}")
    private String token;

    @Value("${email.folder}")
    private String emailFolder;

    public String getEmailFolder() {
        return emailFolder;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getToken() {
        return token;
    }
}
