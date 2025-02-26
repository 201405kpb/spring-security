package com.kpb.security.config;

import com.kpb.security.service.EmailService;
import com.kpb.security.service.SmsService;
import com.kpb.security.service.WeChatService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;


import java.io.IOException;
import java.time.LocalDateTime;

public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private EmailService emailService;

    private SmsService smsService;

    private WeChatService weChatService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        super.onLogoutSuccess(request, response, authentication);

        this.logger.info(String.format("IP %s，用户 %s， 于 %s 退出系统。", request.getRemoteHost(), authentication.getName(), LocalDateTime.now()));

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
}
