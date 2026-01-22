package com.smplatform.product_service.domain.member.dto;

import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.member.enums.MemberStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MemberResponseDto {
    @Getter
    public static class MemberInfo {
        private final String memberId;
        private final String name;
        private final LocalDate birthday;
        private final String phoneNumber;
        private final String gender;
        private final String status;
        private final String level;
        private final String authority;
        private final Boolean withdrawn;
        private final LocalDateTime createAt;
        private final LocalDateTime updateAt;

        public MemberInfo(Member member) {
            this.memberId = member.getMemberId();
            this.name = member.getName();
            this.birthday = member.getBirthday();
            this.phoneNumber = member.getPhoneNumber();
            this.gender = member.getGender() != null ? member.getGender().toString() : null;
            this.status = member.getStatus().toString();
            this.level = member.getLevel().toString();
            this.authority = member.getAuthority().toString();
            this.withdrawn = member.getStatus() == MemberStatus.WITHDRAWN;
            this.createAt = member.getCreateAt();
            this.updateAt = member.getUpdateAt();
        }
    }

    @Getter
    public static class MemberCredential {
        private String loginId;
        private String password;

        public MemberCredential(Member member) {
            this.loginId = member.getMemberId();
            this.password = member.getPassword();
        }

    }
}
