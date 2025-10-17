package com.cryptoAlert.api.controller;

import com.cryptoAlert.dto.response.CryptoFearIndexResponse;
import com.cryptoAlert.CryptoFearIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Crypto Fear Index Controller", description = "Crypto Fear Index API")
@RestController
@RequestMapping("/api")
public class CryptoFearIndexController {

    private final CryptoFearIndexService cryptoFearIndexService;

    public CryptoFearIndexController(CryptoFearIndexService cryptoFearIndexService) {
        this.cryptoFearIndexService = cryptoFearIndexService;
    }

    @Operation(summary = "Crypto Fear Index 조회", description = "fear & greed index 조회")
    @GetMapping("/crypto-fear-index")
    public ResponseEntity<CryptoFearIndexResponse> getCryptoFearIndex() {
        CryptoFearIndexResponse response = cryptoFearIndexService.getCryptoFearIndex();
        return ResponseEntity.ok(response);
    }
}
