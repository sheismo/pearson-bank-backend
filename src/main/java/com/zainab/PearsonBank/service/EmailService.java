package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.EmailDetails;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails) throws MessagingException;
}
