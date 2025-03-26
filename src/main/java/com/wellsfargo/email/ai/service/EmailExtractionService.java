package com.wellsfargo.email.ai.service;

import com.wellsfargo.email.ai.EmailDto;
import com.wellsfargo.email.ai.util.PropertiesUtil;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.springframework.stereotype.Service;

import javax.mail.BodyPart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;

@Service
public class EmailExtractionService {

    private EmailDto emailDto;
    private PropertiesUtil propertiesUtil;

    private final Tika tika = new Tika();

    public EmailExtractionService(EmailDto emailDto, PropertiesUtil propertiesUtil) {
        this.emailDto = emailDto;
        this.propertiesUtil = propertiesUtil;
    }

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
            emailDto = new EmailDto();
            emailDto = processEmailAttachments(emailFile, emailDto,propertiesUtil.getEmailFolder());
            content = emailDto.isHasAttachment() ? new StringBuilder(emailDto.getEmailContent()) : new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to extract content";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    public static EmailDto processEmailAttachments(File file, EmailDto emailDto, String attachPath) throws Exception {
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file);
        }
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        Object content;
        boolean hasAttachment;

        try (InputStream source = new FileInputStream(file)) {
            MimeMessage message = new MimeMessage(session, source);

            content = message.getContent();
            if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                hasAttachment = processMultipart(multipart,attachPath,emailDto);
                emailDto.setHasAttachment(hasAttachment);
                if (!hasAttachment) {
                    System.out.println("No attachments found.");
                }
            } else {
                System.out.println("No attachments found.");
                emailDto.setHasAttachment(false);
            }
        }
        return emailDto;
    }

    public static boolean processMultipart(MimeMultipart multipart,String attachPath,EmailDto emailDto) throws Exception {
        boolean hasAttachments = false;

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);

            if (part.getContent() instanceof MimeMultipart) {
                hasAttachments |= processMultipart((MimeMultipart) part.getContent(),attachPath,emailDto);
            } else {
                String disposition = part.getDisposition();
                String contentType = part.getContentType();

                if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                    System.out.println("Detected attachment: " + part.getFileName());
                    // Save and read attachment content
                    InputStream attachmentStream = part.getInputStream();
                    String savedPath = attachPath + part.getFileName();
                    System.out.println("Saved: " + savedPath);
                    saveAttachment(attachmentStream, savedPath);
                    String encodedString = extractAttachmentContent(savedPath);
                    emailDto.setEmailContent(new String(Base64.getDecoder().decode(encodedString)));
                    hasAttachments = true;
                } else if (contentType != null && contentType.toLowerCase().startsWith("application")) {
                    System.out.println("Possible attachment (no disposition): " + part.getFileName());
                    hasAttachments = true;
                }
            }
        }
        return hasAttachments;
    }

    private static void saveAttachment(InputStream inputStream, String savedPath) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(savedPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public static String extractAttachmentContent(String filePath) throws IOException, TikaException {
        Tika tika = new Tika();
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        String mimeType = tika.detect(file);
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));

        if (mimeType.startsWith("text/") || mimeType.contains("json") || mimeType.contains("csv")) {
            // Read as text file
            return new String(fileBytes);
        } else if (mimeType.contains("pdf") || mimeType.contains("msword")) {
            // Encode binary files (PDF, DOCX) as Base64
            return Base64.getEncoder().encodeToString(fileBytes);
        } else {
            return "[Unsupported file type: " + mimeType + "]";
        }
    }
}
