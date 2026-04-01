package com.yourcompany.telephony.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class CallRoutingService {

    private static final Logger log = LoggerFactory.getLogger(CallRoutingService.class);

    public String determineRoute(MultiValueMap<String, String> payload) {
        String callId = payload.getFirst("id");
        String caller_number = payload.getFirst("caller_number");

        if (caller_number == null || caller_number.isBlank()) {
            log.warn("Maqsam request missing caller_number. CallId: {}", callId);
            return "skip";
        }

        if (isVipCustomer(caller_number)) {
            log.info("VIP Caller identified: {}. Routing to Priority Queue.", caller_number);
            return "201123066960";
        }

        log.info("Standard caller: {}. Skipping dynamic route.", caller_number);
        return "skip";
    }

    private boolean isVipCustomer(String phoneNumber) {
        return "966115201360".equals(phoneNumber);
    }
}
