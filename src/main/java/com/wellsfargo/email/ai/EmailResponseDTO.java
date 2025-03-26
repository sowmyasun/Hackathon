
package com.wellsfargo.email.ai;

import lombok.Data;

import java.util.Map;

@Data
public class EmailResponseDTO {
    private String requestType;
    private String subRequestType;
    private String primaryIntent;
    private boolean duplicate;
    private Map<String, String> extractedFields;
}
