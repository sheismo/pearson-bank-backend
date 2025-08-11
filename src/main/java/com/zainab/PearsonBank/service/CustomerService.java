package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CustomerRequest;

public interface CustomerService {
    AppResponse createAccount(CustomerRequest customerRequest);
}
