package com.cryptoAlert.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class CryptoFearIndexResponse {
    private String name;
    private List<CryptoFearData> data;

    @Data
    public static class CryptoFearData {
        private String value;
        
        @JsonProperty("value_classification")
        private String valueClassification;
        private String timestamp;
    }
}
