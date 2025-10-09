package com.zainab.PearsonBank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetails {
    @Schema(name = "Email Recipient")
    private String recipient;

    @Schema(name = "Email Subject")
    private String subject;

    @Schema(name = "Email Body")
    private String body;

    @Schema(name = "Email Attachment")
    private String attachment;
}
