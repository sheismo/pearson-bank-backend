package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.EmailDetails;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
}
