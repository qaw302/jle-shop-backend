package com.smplatform.product_service.domain.member.service.impl;

import com.smplatform.product_service.domain.member.dto.*;
import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.member.entity.WithdrawMember;
import com.smplatform.product_service.domain.member.exception.IdDuplicateException;
import com.smplatform.product_service.domain.member.exception.InvalidPasswordException;
import com.smplatform.product_service.domain.member.exception.MemberNotFoundException;
import com.smplatform.product_service.domain.member.repository.MemberRepository;
import com.smplatform.product_service.domain.member.repository.WithdrawMemberRepository;
import com.smplatform.product_service.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberServiceImpl implements MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final WithdrawMemberRepository withdrawMemberRepository;

    @Override
    public String createMember(MemberRequestDto.MemberCreate memberCreateDto) {
        // 아이디 중복 검사
        if (memberRepository.findById(memberCreateDto.getMemberId()).isPresent()) {
            throw new IdDuplicateException(memberCreateDto.getMemberId());
        }

        // password 해시 처리
        String hashedPassword = passwordEncoder.encode(memberCreateDto.getPassword());

        // 저장할 멤버 엔티티 생성
        Member newMember = memberCreateDto.toEntity(hashedPassword);

        return memberRepository.save(newMember).getMemberId();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDto.MemberInfo getMember(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        return new MemberResponseDto.MemberInfo(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponseDto.MemberInfo> getMembers() {
        List<Member> members = memberRepository.findAll();

        return members.stream()
                .map(MemberResponseDto.MemberInfo::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponseDto.MemberInfo> searchMembers(MemberRequestDto.MemberSearchRequestParam searchRequestParamDto) {
        // OrderSearch, OrderDateSearch, ProductId가 null이 아니면 필요 데이터 Rest API 요청


        List<Member> members = memberRepository.searchMember(searchRequestParamDto);

        return members.stream()
                .map(MemberResponseDto.MemberInfo::new)
                .collect(Collectors.toList());
    }

    @Override
    public String updateMember(String id, MemberRequestDto.MemberUpdate memberUpdateDto) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        member.update(memberUpdateDto);

        return memberRepository.save(member).getMemberId();
    }

    @Override
    public String updatePassword(String id, MemberRequestDto.PasswordUpdate passwordUpdateDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        if (!passwordEncoder.matches(passwordUpdateDto.getOldPassword(), member.getPassword())) {
            throw new InvalidPasswordException("기존 비밀번호가 일치하지 않음");
        }

        member.updatePassword(passwordEncoder.encode(passwordUpdateDto.getNewPassword()));

        return memberRepository.save(member).getMemberId();
    }

    @Override
    public Void deleteMember(String id, MemberRequestDto.Withdraw withdrawDto) {
        return withdrawMember(id, withdrawDto);
    }

    @Override
    public Void withdrawMemberByAdmin(String memberId, MemberRequestDto.Withdraw withdrawDto) {
        return withdrawMember(memberId, withdrawDto);
    }

    private Void withdrawMember(String memberId, MemberRequestDto.Withdraw withdrawDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.delete();

        WithdrawMember withdrawMember = new WithdrawMember(member, withdrawDto.getMemo());
        withdrawMemberRepository.save(withdrawMember);

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDto.MemberCredential getMemberCredential(String id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));

        return new MemberResponseDto.MemberCredential(member);
    }
}
