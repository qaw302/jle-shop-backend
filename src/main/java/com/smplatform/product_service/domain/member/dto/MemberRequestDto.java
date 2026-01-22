package com.smplatform.product_service.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.member.enums.Gender;
import com.smplatform.product_service.domain.member.enums.MemberAuthority;
import com.smplatform.product_service.domain.member.enums.MemberLevel;
import com.smplatform.product_service.domain.member.enums.MemberStatus;
import com.smplatform.product_service.domain.member.enums.search.DateSerach;
import com.smplatform.product_service.domain.member.enums.search.OrderDateSearch;
import com.smplatform.product_service.domain.member.enums.search.OrderSerach;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

public class MemberRequestDto {

    @Getter
    public static class MemberCreate {

        @NotBlank
        @Email
        private String memberId;

        @NotBlank
        private String password;

        @NotBlank
        private String name;

        private LocalDate birthday;

        @NotBlank
        private String phoneNumber;

        private Gender gender;

        private Boolean tosAgreement; // Optional

        private Boolean privacyAgreement; // Optional

        private Boolean marketingAgreement;  // Optional

        public Member toEntity(String hashedPassword) {
            return Member.builder()
                    .memberId(memberId)
                    .name(name)
                    .password(hashedPassword) // Set Password to hashedPassword from arg
                    .birthday(birthday) // Set as null if not provided
                    .phoneNumber(phoneNumber)
                    .gender(gender)
                    .status(MemberStatus.ACTIVE) // Set default status
                    .authority(MemberAuthority.USER) // Set default authority
                    .level(MemberLevel.NEW) // Set default level
                    .tosAgreement(tosAgreement != null ? tosAgreement : false) // Default to false if null
                    .privacyAgreement(privacyAgreement != null ? privacyAgreement : false) // Default to false if null
                    .marketingAgreement(marketingAgreement != null ? marketingAgreement : false) // Default to false if null
                    .build();
        }
    }

    @Getter
    public static class MemberSearchRequestParam {
        private String name;
        private String email;
        private MemberLevel level;
        private MemberStatus status;
        private DateSerach dateSearch; // 가입일, 생일
        private LocalDate startDate;
        private LocalDate endDate;
        private int startAge;
        private int endAge;
        private Gender gender;
        private OrderSerach orderSerach; // 총 주문금액, 총 실결제금액. 총 주문건수, 총 실주문건수
        private int orderSearchStartValue;
        private int orderSearchEndValue;
        private OrderDateSearch orderDateSearch;
        private LocalDate startOrderDate;
        private LocalDate endOrderDate;
        private Long productId;
    }

    @Getter
    public static class MemberUpdate {
        private String name;
        private LocalDate birthday;
        private String phoneNumber;
        private Gender gender;
        private MemberStatus status;
        private MemberLevel level;
        private Boolean tosAgreement;
        private Boolean privacyAgreement;
        private Boolean marketingAgreement;
    }

    @Getter
    public static class PasswordUpdate {
        @NotBlank
        private String oldPassword;
        @NotBlank
        private String newPassword;
    }

    @Getter
    public static class Withdraw {
        @JsonProperty("memo")
        private String memo;
    }
}
