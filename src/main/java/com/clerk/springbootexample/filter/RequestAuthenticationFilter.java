package com.clerk.springbootexample.filter;

import com.clerk.backend_api.helpers.security.AuthenticateRequest;
import com.clerk.backend_api.helpers.security.models.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.security.models.MachineAuthVerificationData;
import com.clerk.backend_api.helpers.security.models.RequestState;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestAuthenticationFilter extends OncePerRequestFilter {

    @Value("${clerk.api.secret-key}")
    private String clerkApiSecretKey;

    @Value("${clerk.api.authorized-parties}")
    private List<String> clerkApiAuthorizedParties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException {

        try {

            Map<String, List<String>> headers = new HashMap<>();
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                List<String> headerValues = new ArrayList<>();
                request.getHeaders(headerName).asIterator().forEachRemaining(headerValues::add);
                headers.put(headerName, headerValues);
            });

            // authenticate with clerk API
            RequestState state = AuthenticateRequest.authenticateRequest(headers,
                AuthenticateRequestOptions.Builder.withSecretKey(clerkApiSecretKey).authorizedParties(clerkApiAuthorizedParties).build()
            );


            if (!state.isSignedIn()){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.addHeader("Content-Type", "application/json");
                response.getWriter().write("{\"detail\": \"" + state.reason().get().message() + "\"}");


            } else {
                String userId;
                if (state.claims().isPresent()){
                    userId = (String) state.claims().get().get("user_id");
                }
                else if (state.tokenVerificationResponse().isPresent()){
                    userId =
                        ((MachineAuthVerificationData) state.tokenVerificationResponse().get().payload()).getSubject();
                }
                else {
                    throw new Exception("unable to authenticate request, no user_id found in claims or token verification response");
                }

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
            response.addHeader("Content-Type", "application/json");
            response.getWriter().write("{\"detail\": \"Unable to authenticate request\"}");
        }

    }
}
