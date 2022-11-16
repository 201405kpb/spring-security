package com.kpb.security.service.impl;

import com.kpb.security.service.WeChatService;
import org.springframework.stereotype.Service;

@Service
public class WeChatServiceImpl implements WeChatService {

    @Override
    public void send() {
        System.out.println("微信消息发送成功！");
    }
}
