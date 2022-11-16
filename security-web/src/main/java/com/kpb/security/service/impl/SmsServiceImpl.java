package com.kpb.security.service.impl;

import com.kpb.security.service.SmsService;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void send() {
        System.out.println("短信发送成功！");
    }
}
