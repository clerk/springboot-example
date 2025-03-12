package com.clerk.springbootexample.filter;

import com.clerk.backend_api.helpers.jwks.AuthenticateRequest;
import com.clerk.backend_api.helpers.jwks.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.jwks.RequestState;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestAuthenticationFilter extends OncePerRequestFilter {

    @Value("${clerk.api.secret-key}")
    private String clerkApiSecretKey;

    @Value("${clerk.api.authorized-parties}")
    private List<String> clerkApiAuthorizedParties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {

            // Cast spring request to Java HttpRequest to make it compatible with Clerk SDK
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI(request.getRequestURL().toString()))
                .header("Authorization", request.getHeader("Authorization"))
                .build();

            // authenticate with clerk API
            RequestState state = AuthenticateRequest.authenticateRequest(httpRequest,
                AuthenticateRequestOptions.Builder.withSecretKey(clerkApiSecretKey).authorizedParties(clerkApiAuthorizedParties).build()
            );

            // If there is any auth error returned by clerk, return a 401, if required we can also cast the error
            // to either AuthErrorReason or TokenVerificationErrorReason to get more details about the error
            if (state.reason().isPresent()){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                String userId = (String) state.claims().get().get("sub");

                /*
                set user as authenticated principal, this can then be used in the controller to identify the user
                We are setting only userId, but we can also set other details like roles, etc in a custom object.
                */

                Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
}
