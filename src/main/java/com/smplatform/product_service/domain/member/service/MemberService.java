package com.smplatform.product_service.domain.member.service;

import com.smplatform.product_service.domain.member.dto.*;

import java.util.List;

public interface MemberService {

    /**
     * Create
     */
    String createMember(MemberRequestDto.MemberCreate newMemberDto);

    /**
     * Read
     */
    MemberResponseDto.MemberInfo getMember(String id);
    List<MemberResponseDto.MemberInfo> getMembers();
    List<MemberResponseDto.MemberInfo> searchMembers(MemberRequestDto.MemberSearchRequestParam searchRequestParamDto);

    /**
     * Update
     */
    String updateMember(String id, MemberRequestDto.MemberUpdate updateMemberDto);

    String updatePassword(String id, MemberRequestDto.PasswordUpdate password);

    /**
     * Delete
     */
    Void deleteMember(String id, MemberRequestDto.Withdraw withdrawDto);
    Void withdrawMemberByAdmin(String memberId, MemberRequestDto.Withdraw withdrawDto);

    /**
     * Actions
     */
    MemberResponseDto.MemberCredential getMemberCredential(String id);
}
