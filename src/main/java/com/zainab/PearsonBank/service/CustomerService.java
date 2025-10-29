package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.*;

public interface CustomerService {
    AppResponse<?> createAccount(CustomerRequest customerRequest);
    AppResponse<?> balanceEnquiry(EnquiryRequest request);
    String nameEnquiry(EnquiryRequest request);
}
