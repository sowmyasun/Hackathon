package com.wellsfargo.email.ai.config;

import com.wellsfargo.email.ai.util.PropertiesUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class EmailHuggingFaceApi {

    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
    private static final String API_TOKEN = "";

    private PropertiesUtil propertiesUtil;

    public EmailHuggingFaceApi(PropertiesUtil propertiesUtil) {
        this.propertiesUtil = propertiesUtil;
    }

    public String contentClassification(String text) throws Exception {

        List<String> requestTypes = List.of(propertiesUtil.getRequestTypes());

        List<String> subRequestTypes = List.of(propertiesUtil.getRequestSubTypes());

        String requestType  = classifyRequestSubRequestTyoes(text,requestTypes);
        String subRequestType  = classifyRequestSubRequestTyoes(text,subRequestTypes);

        return requestType;
    }

    private String classifyRequestSubRequestTyoes(String text,List<String> labels) throws IOException, InterruptedException {
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
        System.out.println("Response: " + bodyStr); // Debug log

        JSONObject jsonResponse = new JSONObject(bodyStr);

        if (!jsonResponse.has("labels")) {
            System.err.println("Error: Missing 'labels' in HuggingFace response: " + bodyStr);
            return "Unknown"; // fallback label
        }

        return jsonResponse.getJSONArray("labels").getString(0); // Top prediction
    }


}
