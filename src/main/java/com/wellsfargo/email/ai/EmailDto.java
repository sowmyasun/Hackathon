package com.wellsfargo.email.ai;


import org.springframework.stereotype.Component;

@Component
public class EmailDto{

    private boolean hasAttachment;
    private String emailContent;

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public boolean isHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }
}
