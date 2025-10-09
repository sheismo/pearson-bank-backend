package com.zainab.PearsonBank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {
    @Schema(name = "First Name")
    private String firstName;

    @Schema(name = "Last Name")
    private String lastName;

    @Schema(name = "Other Name")
    private String otherName;

    @Schema(name = "Gender")
    private String gender;

    @Schema(name = "Address")
    private String address;

    @Schema(name = "Country")
    private String country;

    @Schema(name = "State")
    private String state;

    @Schema(name = "Email")
    private String email;

    @Schema(name = "Phone Number")
    private String phoneNumber;

    @Schema(name = "Alternative Phone Number")
    private String alternativePhoneNumber;

    @Schema(name = "NIN")
    private String nin;

    @Schema(name = "BVN")
    private String bvn;
}
