package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.*;

public interface CustomerService {
    AppResponse<?> onboardNewCustomer(CustomerRequest customerRequest);
    AppResponse<?> verifyCustomerEmail(String emailAddress, String otp);
    AppResponse<?> createAccountForCustomer(String emailAddress);
    AppResponse<?> balanceEnquiry(EnquiryRequest request);
    String nameEnquiry(EnquiryRequest request);
}
