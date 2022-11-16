package com.kpb.security.service.impl;

import com.kpb.security.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void send() {
        System.out.println("邮件发送邮件成功！");
    }

}
