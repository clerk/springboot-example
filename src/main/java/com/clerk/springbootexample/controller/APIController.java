package com.clerk.springbootexample.controller;

import com.clerk.springbootexample.schema.response.VerifiedJwtResponse;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {

    @GetMapping("/clerk_jwt")
    public @ResponseBody VerifiedJwtResponse clerk_jwt(@AuthenticationPrincipal String userId) {
        return new VerifiedJwtResponse(userId);
    }

    @GetMapping("/gated_data")
    public @ResponseBody Map<String, String> get_gated_data() {
        return Map.of("foo", "bar");
    }

}
