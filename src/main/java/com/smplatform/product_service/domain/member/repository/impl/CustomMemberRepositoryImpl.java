package com.smplatform.product_service.domain.member.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.member.dto.MemberRequestDto;
import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.member.enums.Gender;
import com.smplatform.product_service.domain.member.enums.MemberLevel;
import com.smplatform.product_service.domain.member.enums.MemberStatus;
import com.smplatform.product_service.domain.member.enums.search.DateSerach;
import com.smplatform.product_service.domain.member.repository.CustomMemberRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static com.smplatform.product_service.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class CustomMemberRepositoryImpl implements CustomMemberRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Member> searchMember(MemberRequestDto.MemberSearchRequestParam searchRequestParamDto) {
        return queryFactory
                .selectFrom(member)
                .where(
                        nameContains(searchRequestParamDto.getName()),
                        emailContains(searchRequestParamDto.getEmail()),
                        levelEq(searchRequestParamDto.getLevel()),
                        statusEq(searchRequestParamDto.getStatus()),
                        dateSearchBetween(searchRequestParamDto.getDateSearch(), searchRequestParamDto.getStartDate(), searchRequestParamDto.getEndDate()),
                        ageBetween(searchRequestParamDto.getStartAge(), searchRequestParamDto.getEndAge()),
                        genderEq(searchRequestParamDto.getGender())
                )
                .fetch();
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? member.name.contains(name) : null;
    }

    private BooleanExpression emailContains(String email) {
        return email != null ? member.memberId.contains(email) : null;
    }

    private BooleanExpression levelEq(MemberLevel level) {
        return level != null ? member.level.eq(MemberLevel.valueOf(level.name())) : null;
    }

    private BooleanExpression statusEq(MemberStatus status) {
        return status != null ? member.status.eq(MemberStatus.valueOf(status.name())) : null;
    }

    private BooleanExpression dateSearchBetween(DateSerach dateSearch, LocalDate startDate, LocalDate endDate) {
        if (dateSearch == null || startDate == null || endDate == null) {
            return null;
        }

        switch (dateSearch) {
            case JOIN:
                return member.createAt.between(
                        startDate.atTime(0, 0, 0),
                        endDate.atTime(23, 59, 59)
                );
            case BIRTHDAY:
                return member.birthday.between(startDate, endDate);
            default:
                return null;
        }
    }

    private BooleanExpression ageBetween(int startAge, int endAge) {
        BooleanExpression agePredicate = null;
        if (startAge != 0) {
            agePredicate = member.birthday.loe(LocalDate.now().minusYears(startAge));
        }
        if (endAge != 0) {
            BooleanExpression endAgePredicate = member.birthday.goe(LocalDate.now().minusYears(endAge + 1));
            agePredicate = agePredicate != null ? agePredicate.and(endAgePredicate) : endAgePredicate;
        }
        return agePredicate;
    }

    private BooleanExpression genderEq(Gender gender) {
        return gender != null ? member.gender.eq(Gender.valueOf(gender.name())) : null;
    }

}
