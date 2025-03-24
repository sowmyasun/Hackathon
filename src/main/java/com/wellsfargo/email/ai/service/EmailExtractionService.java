package com.wellsfargo.email.ai.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class EmailExtractionService {

    private final Tika tika = new Tika();

    public String extractEmailContent(File emailFile) {
        try {
            return tika.parseToString(emailFile);
        } catch (IOException | TikaException e) {
            e.printStackTrace();
            return "Failed to extract content";
        }
    }
}
