package com.wellsfargo.email.ai.config;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

@Component
public class EmailHuggingFaceApi {

    private static final String API_URL = "https://api-inference.huggingface.co/models/gpt2";
    private static final String API_TOKEN = "";


    public static void main(String[] args) {
        try {
            String inputText = "Java is a powerful programming language.";
            String response = runInference(inputText);
            System.out.println("Inference Output: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String runInference(String text) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(API_URL);

// Add authentication token
        post.setHeader("Authorization", "Bearer " + API_TOKEN);
        post.setHeader("Content-Type", "application/json");

// Prepare JSON input
        StringEntity entity = new StringEntity("{\"inputs\": \"" + text + "\"}");
        post.setEntity(entity);

// Execute request
        CloseableHttpResponse response = client.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        client.close();

// Parse output
        JSONArray jsonArray = new JSONArray(result);
        return jsonArray.getJSONObject(0).getString("generated_text");
    }

}
