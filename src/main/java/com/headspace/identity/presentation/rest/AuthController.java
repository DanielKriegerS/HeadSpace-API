package com.headspace.identity.presentation.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        response.setHeader("Set-Cookie", "HEADSPACE_SESSION=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax; Secure=false");
        response.addHeader("Set-Cookie", "XSRF-TOKEN=; Max-Age=0; Path=/; HttpOnly=false; SameSite=Lax");
    }
}
