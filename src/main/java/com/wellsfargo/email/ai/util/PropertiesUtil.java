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

    @Value("${email.request.types}")
    private String[] requestTypes;
    @Value("${email.request.subtypes}")
    private String[] requestSubTypes;

    public String[] getRequestTypes() {
        return requestTypes;
    }

    public String[] getRequestSubTypes() {
        return requestSubTypes;
    }

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
