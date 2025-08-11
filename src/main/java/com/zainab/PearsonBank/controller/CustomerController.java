package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CustomerRequest;
import com.zainab.PearsonBank.service.CustomerService;
import com.zainab.PearsonBank.utils.AccountUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/customer")
@Slf4j
public class CustomerController {
    @Autowired
    CustomerService customerService;

    /**
     * @param request
     * @return AppResponse containing info about newly created account or errors if any
     */
    @PostMapping("/openaccount")
    public ResponseEntity<AppResponse> createAccount (@RequestBody CustomerRequest customerRequest, HttpServletRequest request) {
        log.info("Received request to create new customer account: {} from ip {}", customerRequest, request.getRemoteAddr());
        if (!AccountUtils.validateCustomerRequest(customerRequest)) {
            return ResponseEntity.badRequest().body(new AppResponse("42", "Error occurred due to invalid request", null));
        }
        AppResponse response = customerService.createAccount(customerRequest);
        return ResponseEntity.ok(response);
    }
}
