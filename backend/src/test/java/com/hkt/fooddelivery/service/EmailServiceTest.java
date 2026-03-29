package com.hkt.fooddelivery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles({"dev", "local"})
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendSimpleEmail() {
        String targetEmail = "nttung1901@gmail.com";
        Map<String, Object> props = new HashMap<>();
        props.put("name", "Thanh Tùng");
        props.put("otpCode", "123456");

        assertDoesNotThrow(() -> {
            emailService.sendHtmlEmail(
                    targetEmail,
                    "Mã xác nhận tài khoản",
                    "email-template",
                    props
            );
        });

    }
}