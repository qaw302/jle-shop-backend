package com.smplatform.product_service.domain.member.entity;

import com.smplatform.product_service.domain.member.dto.MemberRequestDto;
import com.smplatform.product_service.domain.member.enums.Gender;
import com.smplatform.product_service.domain.member.enums.MemberAuthority;
import com.smplatform.product_service.domain.member.enums.MemberLevel;
import com.smplatform.product_service.domain.member.enums.MemberStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Getter
@Builder
@Table(name = "members")
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @Column(name = "member_id", nullable = false)
    private String memberId;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birthday")
    private LocalDate birthday;

    @NotNull
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false)
    private MemberAuthority authority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private MemberLevel level;

    @Column(name = "tos_agreement")
    private Boolean tosAgreement;

    @Column(name = "privacy_agreement")
    private Boolean privacyAgreement;

    @Column(name = "marketing_agreement")
    private Boolean marketingAgreement;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

//    @Column(name = "points")
//    private Integer points = 0;


    public void update(MemberRequestDto.MemberUpdate memberUpdateDto) {
        Optional.ofNullable(memberUpdateDto.getName()).ifPresent(name -> this.name = name);
        Optional.ofNullable(memberUpdateDto.getBirthday()).ifPresent(birthday -> this.birthday = birthday);
        Optional.ofNullable(memberUpdateDto.getPhoneNumber()).ifPresent(phoneNumber -> this.phoneNumber = phoneNumber);
        Optional.ofNullable(memberUpdateDto.getGender()).ifPresent(gender -> this.gender = gender);
        Optional.ofNullable(memberUpdateDto.getStatus()).ifPresent(status -> this.status = status);
        Optional.ofNullable(memberUpdateDto.getLevel()).ifPresent(level -> this.level = level);
        Optional.ofNullable(memberUpdateDto.getTosAgreement()).ifPresent(tosAgreement -> this.tosAgreement = tosAgreement);
        Optional.ofNullable(memberUpdateDto.getPrivacyAgreement()).ifPresent(privacyAgreement -> this.privacyAgreement = privacyAgreement);
        Optional.ofNullable(memberUpdateDto.getMarketingAgreement()).ifPresent(marketingAgreement -> this.marketingAgreement = marketingAgreement);
    }

    public void updatePassword(String hashedPassword) {
        this.password = hashedPassword;
    }

    public void delete() {
        this.status = MemberStatus.WITHDRAWN;
    }

//    public void usePoints(Integer pointsToUse) {
//        if (this.points == null) {
//            this.points = 0;
//        }
//        if (pointsToUse == null || pointsToUse <= 0) {
//            throw new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다.");
//        }
//        if (this.points < pointsToUse) {
//            throw new IllegalArgumentException("포인트가 부족합니다. 현재 포인트: " + this.points);
//        }
//        this.points -= pointsToUse;
//    }
//
//    public void addPoints(Integer pointsToAdd) {
//        if (this.points == null) {
//            this.points = 0;
//        }
//        if (pointsToAdd == null || pointsToAdd < 0) {
//            throw new IllegalArgumentException("추가할 포인트는 0 이상이어야 합니다.");
//        }
//        this.points += pointsToAdd;
//    }

}
