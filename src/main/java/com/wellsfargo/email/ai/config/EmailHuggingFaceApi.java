package com.wellsfargo.email.ai.config;

import com.wellsfargo.email.ai.EmailResponseDTO;
import com.wellsfargo.email.ai.util.PropertiesUtil;
import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailHuggingFaceApi {

    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
    private static final String API_TOKEN = "";
    private final String HUGGINGFACE_LLM_URL = "https://api-inference.huggingface.co/models/google/flan-t5-base";


    private PropertiesUtil propertiesUtil;

    public EmailHuggingFaceApi(PropertiesUtil propertiesUtil) {
        this.propertiesUtil = propertiesUtil;
    }

    public EmailResponseDTO contentClassification(String text) throws Exception {

        List<String> requestTypes = List.of(propertiesUtil.getRequestTypes());

        List<String> subRequestTypes = List.of(propertiesUtil.getRequestSubTypes());

        String requestType  = classifyRequestSubRequestTypes(text,requestTypes);
        String subRequestType  = classifyRequestSubRequestTypes(text,subRequestTypes);

        Map<String, String> fields = extractFieldsWithRegexAndFallback(text);

        EmailResponseDTO dto = new EmailResponseDTO();
        dto.setRequestType(requestType);
        dto.setSubRequestType(subRequestType);
        dto.setPrimaryIntent(requestType + " - " + subRequestType);
        dto.setDuplicate(false);
        dto.setExtractedFields(fields);
        return dto;
    }

    private String classifyRequestSubRequestTypes(String text, List<String> labels) throws IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("inputs", text);

        JSONObject parameters = new JSONObject();
        parameters.put("candidate_labels", new JSONArray(labels));
        body.put("parameters", parameters);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String bodyStr = response.body().trim();
        System.out.println("Response : " + bodyStr); // Debug log

        JSONObject jsonResponse = new JSONObject(bodyStr);

        if (!jsonResponse.has("labels")) {
            System.err.println("Error: Missing 'labels' in HuggingFace response: " + bodyStr);
            return "Unknown"; // fallback label
        }

        return jsonResponse.getJSONArray("labels").getString(0); // Top prediction
    }


    private Map<String, String> extractFieldsWithRegexAndFallback(String content) throws Exception {
        Map<String, String> fields = new HashMap<>();

        // Regex for amount
        Pattern amountPattern = Pattern.compile("USD\\s[\\d,]+\\.\\d{2}");
        Matcher amtMatcher = amountPattern.matcher(content);
        if (amtMatcher.find()) {
            fields.put("amount", amtMatcher.group());
        }

        // Regex for dealId (generalized)
        Pattern dealIdPattern = Pattern.compile("(?i)(deal id|reference|ref)[:\\s]*([\\w\\s\\-\\d]+)");
        Matcher dealMatcher = dealIdPattern.matcher(content);
        if (dealMatcher.find()) {
            fields.put("dealId", dealMatcher.group(2).trim());
        }

        // Fallback to LLM if anything is missing
        if (!fields.containsKey("amount") || !fields.containsKey("dealId")) {
            fields.putAll(extractFieldsWithLLM(content));
        }

        return fields;
    }

    private Map<String, String> extractFieldsWithLLM(String content) throws Exception {
        JSONObject body = new JSONObject();
        String prompt = "From the below message, extract:\n" +
                "- Amount (in USD)\n" +
                "- Deal ID\n" +
                "Return the result exactly as:\nAmount: <value>; Deal ID: <value>\n\nMessage:\n" + content;

        body.put("inputs", prompt);
        body.put("parameters", new JSONObject());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HUGGINGFACE_LLM_URL))
                .header("Authorization", "Bearer " + API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String bodyStr = response.body().trim();
        String output;

        try {
            JSONArray arr = new JSONArray(bodyStr);
            output = arr.getJSONObject(0).getString("generated_text");
        } catch (Exception ex) {
            JSONObject obj = new JSONObject(bodyStr);
            output = obj.optString("generated_text", bodyStr);
        }

        System.out.println("LLM Output: " + output); // debug log

        Map<String, String> fields = new HashMap<>();
        String[] tokens = output.split(";|\n");
        for (String token : tokens) {
            if (token.toLowerCase().contains("amount") && token.contains(":")) {
                fields.put("amount", token.split(":", 2)[1].trim());
            } else if (token.toLowerCase().contains("deal") && token.contains(":")) {
                fields.put("dealId", token.split(":", 2)[1].trim());
            }
        }

        return fields;
    }


}
