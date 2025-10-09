package com.zainab.PearsonBank.event;

import com.zainab.PearsonBank.dto.EmailDetails;
import lombok.Value;

@Value
public class EmailEvent {
    EmailDetails emailDetails;
}
