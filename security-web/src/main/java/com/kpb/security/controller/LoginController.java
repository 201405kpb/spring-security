package com.kpb.security.controller;

import com.kpb.security.model.LoginError;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class LoginController {

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/login_fail")
    public String loginFail(HttpServletRequest request, Model model) {
        model.addAttribute("request",request);
        LoginError loginError = determineErrorType(request);
        model.addAttribute("errorMessage", loginError != null ? loginError.getMessage() : null);
        return "login_fail";
    }
    private LoginError determineErrorType(HttpServletRequest request) {
        String typeStr = request.getParameter("error");

        return typeStr == null ? null : LoginError.resolve(Integer.valueOf(typeStr));
    }


}
