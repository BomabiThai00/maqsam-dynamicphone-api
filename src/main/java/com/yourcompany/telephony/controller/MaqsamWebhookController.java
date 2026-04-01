package com.yourcompany.telephony.controller;

import com.yourcompany.telephony.service.CallRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaqsamWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MaqsamWebhookController.class);
    private final CallRoutingService callRoutingService;

    public MaqsamWebhookController(CallRoutingService callRoutingService) {
        this.callRoutingService = callRoutingService;
    }

    // ==========================================
    //   Handle the GET Requests (Query Params)
    // ==========================================
    @GetMapping(
        value = "/",
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> handleMaqsamGetRequest(@RequestParam MultiValueMap<String, String> payload) {
        log.info("Received GET request from Maqsam. Payload keys: {}", payload.keySet());
        return processRequest(payload);
    }

    // ==========================================
    //   Handle the POST Requests (Form Data)
    // ==========================================
    @PostMapping(
        value = "/",
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            "application/x-www-form-urlencoded;charset=UTF-8",
            "application/x-www-form-urlencoded; charset=UTF-8"
        },
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> handleMaqsamPostRequest(@RequestParam MultiValueMap<String, String> payload) {
        log.info("Received POST request from Maqsam. Payload keys: {}", payload.keySet());
        return processRequest(payload);
    }

    // ==========================================
    // 3. Shared Business Logic
    // ==========================================
    private ResponseEntity<String> processRequest(MultiValueMap<String, String> payload) {
        try {
            // Both GET and POST funnel their data into this single service
            String routeTo = callRoutingService.determineRoute(payload);
            return ResponseEntity.ok(routeTo);
        } catch (Exception e) {
            log.error("Critical error in routing logic. Defaulting to 'skip'", e);
            return ResponseEntity.ok("skip");
        }
    }
}