package com.mvron.booking.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAuthenticationEntryPointTest {

    private final JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();

    @Test
    void commence_returns401WithProblemJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad credentials"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/problem+json"));
        assertTrue(response.getContentAsString().contains("\"status\":401"));
        assertTrue(response.getHeader("WWW-Authenticate").startsWith("Bearer"));
    }
}