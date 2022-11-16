package com.kpb.security.config;

import com.kpb.security.model.LoginError;
import com.kpb.security.service.EmailService;
import com.kpb.security.service.SmsService;
import com.kpb.security.service.WeChatService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.time.LocalDateTime;

public class CustomSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String DEFAULT_FAILURE_URL = "/login_fail";

    private String defaultFailureUrl;

    private EmailService emailService;

    private SmsService smsService;

    private WeChatService weChatService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        setDefaultFailureUrl(determineFailureUrl(exception));

        super.onAuthenticationFailure(request, response, exception);

        this.logger.info(String.format("IP %s 于 %s 尝试登录系统失败，失败原因：%s", request.getRemoteHost(), LocalDateTime.now(), exception.getMessage()));

        try {
            // 发邮件
            this.emailService.send();

            // 发短信
            this.smsService.send();

            // 发微信
            this.weChatService.send();
        } catch (Exception ex) {
            this.logger.error(ex.getMessage(), ex);
        }
    }


    public String getDefaultFailureUrl() {
        return defaultFailureUrl;
    }

    @Override
    public void setDefaultFailureUrl(String defaultFailureUrl) {
        super.setDefaultFailureUrl(defaultFailureUrl);
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public SmsService getSmsService() {
        return smsService;
    }

    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }

    public WeChatService getWeChatService() {
        return weChatService;
    }

    public void setWeChatService(WeChatService weChatService) {
        this.weChatService = weChatService;
    }

    private String determineFailureUrl(AuthenticationException exception) {
        // 默认设置登录错误页面为/login_fail
        defaultFailureUrl = StringUtils.hasLength(defaultFailureUrl) ? defaultFailureUrl : DEFAULT_FAILURE_URL;

        Integer failureType = determineFailureType(exception).getType();

        if (failureType != null) {
            defaultFailureUrl += defaultFailureUrl.lastIndexOf("?") > 0 ? "&" : "?" + "error=" + failureType;
        }

        return defaultFailureUrl;
    }

    private LoginError determineFailureType(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return LoginError.BADCREDENTIALS;
        } else if (exception instanceof LockedException) {
            return LoginError.LOCKED;
        } else if (exception instanceof AccountExpiredException) {
            return LoginError.ACCOUNTEXPIRED;
        } else if (exception instanceof UsernameNotFoundException) {
            return LoginError.USERNAMENOTFOUND;
        }

        return LoginError.FAILURE;
    }

}
