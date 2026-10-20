package com.enterprise.risk.api;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RiskController {

    @GetMapping("/risk-status")
    public Map<String, Object> riskStatus(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        String subject = jwt != null
                ? jwt.getSubject()
                : (authentication != null ? authentication.getName() : "anonymous");

        return Map.of(
                "status", "protected",
                "subject", subject,
                "control", "JWT validation and authenticated access");
    }
}
