package com.wellsfargo.email.ai.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Service
public class EmailExtractionService {

    private final Tika tika = new Tika();

    public String extractEmailContent(File emailFile) {
        if (emailFile.length() == 0) {
            System.out.println("File is empty!");
            return "";
        }
        StringBuilder content;
        try {
            TikaInputStream stream = TikaInputStream.get(emailFile);
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);
            content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to extract content";
        }
        return content.toString();
    }
}
