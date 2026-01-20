package com.smplatform.product_service.domain.member.controller;

import com.smplatform.product_service.annotation.AdminOnly;
import com.smplatform.product_service.domain.member.dto.*;
import com.smplatform.product_service.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/members")
@Tag(name = "Member", description = "Member management APIs")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @PostMapping
    @Operation(summary = "member 생성", description = "사용자 정보 생성")
    public ResponseEntity<String> createMember(@RequestBody @Valid final MemberRequestDto.MemberCreate newMember) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(newMember));
    }

    @GetMapping("/me")
    @Operation(summary = "member 조회", description = "해당 아이디의 사용자 정보 조회")
    public ResponseEntity<MemberResponseDto.MemberInfo> getMember(@RequestHeader(name = "X-MEMBER-ID") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getMember(id));
    }

    @GetMapping
    @Operation(summary = "member 리스트 조회", description = "모든 사용자 정보 조회")
    public ResponseEntity<List<MemberResponseDto.MemberInfo>> getMembers() {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getMembers());
    }

    @PostMapping("/search")
    @Operation(summary = "member 검색", description = "검색파라미터를 이용한 사용자 정보 리스트 조회")
    public ResponseEntity<List<MemberResponseDto.MemberInfo>> searchMembers(@RequestBody @Valid final MemberRequestDto.MemberSearchRequestParam searchParam) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.searchMembers(searchParam));
    }

    @PostMapping("/me/update")
    @Operation(summary = "member 정보 수정", description = "사용자 정보 수정")
    public ResponseEntity<String> updateMember(
            @RequestHeader(name = "X-MEMBER-ID") String id,
            @RequestBody @Valid final MemberRequestDto.MemberUpdate updateMember
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.updateMember(id, updateMember));
    }

    @PostMapping("/me/update/auth")
    @Operation(summary = "member 정보 수정", description = "사용자 비밀번호 수정")
    public ResponseEntity<String> updatePassword(
            @RequestHeader(name = "X-MEMBER-ID") String id,
            @RequestBody @Valid final MemberRequestDto.PasswordUpdate updatePassword
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.updatePassword(id, updatePassword));
    }

    @PostMapping("/me/withdraw")
    @Operation(summary = "member 탈퇴", description = "사용자 탈퇴 상태로 변경")
    public ResponseEntity<Void> deleteMember(
            @RequestHeader(name = "X-MEMBER-ID") String id,
            @RequestBody MemberRequestDto.Withdraw withdrawDto
    ) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(memberService.deleteMember(id, withdrawDto));
    }

    @AdminOnly
    @PostMapping("/{id}/withdraw")
    @Operation(summary = "member 탈퇴(관리자)", description = "관리자가 사용자 탈퇴 상태로 변경")
    public ResponseEntity<Void> withdrawMemberByAdmin(
            @PathVariable("id") String id,
            @RequestBody MemberRequestDto.Withdraw withdrawDto
    ) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(memberService.withdrawMemberByAdmin(id, withdrawDto));
    }

    @GetMapping("/credential/{id}")
    @Operation(summary = "member 로그인 정보 조회", description = "사용자 로그인 정보 조회")
    public ResponseEntity<MemberResponseDto.MemberCredential> getMemberCredential(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getMemberCredential(id));
    }
}
