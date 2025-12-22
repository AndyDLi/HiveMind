package com.andydli.hivemind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

@Controller
public class ViewController {
    @GetMapping("/")
    public String landing(Authentication auth) {
        return isAuthenticated(auth) ? "redirect:/home" : "login";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        return isAuthenticated(auth) ? "redirect:/home" : "login";
    }

    @GetMapping("/register")
    public String register(Authentication auth) {
        return isAuthenticated(auth) ? "redirect:/home" : "register";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}