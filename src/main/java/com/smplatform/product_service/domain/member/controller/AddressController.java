package com.smplatform.product_service.domain.member.controller;

import com.smplatform.product_service.domain.member.dto.AddressRequestDto;
import com.smplatform.product_service.domain.member.dto.AddressResponseDto;
import com.smplatform.product_service.domain.member.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@Tag(name = "Address", description = "Address management APIs")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/address")
    @Operation(summary = "배송지 등록", description = "회원 배송지를 등록합니다")
    public ResponseEntity<Long> createAddress(@RequestHeader(name = "X-MEMBER-ID") String memberId,
                                              @Valid @RequestBody AddressRequestDto.AddressSave requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.saveAddress(memberId, requestDto));
    }

    @GetMapping("/address")
    @Operation(summary = "배송지 조회", description = "회원 배송지 목록을 조회합니다")
    public ResponseEntity<List<AddressResponseDto.AddressGet>> getAddress(@RequestHeader(name = "X-MEMBER-ID") String memberId) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.getAddresses(memberId));
    }

    @PostMapping("/update-address")
    @Operation(summary = "배송지 수정", description = "회원 배송지 정보를 수정합니다")
    public ResponseEntity<Long> updateAddress(@RequestHeader(name = "X-MEMBER-ID") String memberId,
                                              @Valid @RequestBody AddressRequestDto.AddressUpdate requestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.updateAddress(memberId, requestDto));
    }

    @PostMapping("/delete-address")
    @Operation(summary = "배송지 삭제", description = "회원 배송지를 삭제합니다")
    public ResponseEntity<Void> deleteAddress(@RequestHeader(name = "X-MEMBER-ID") String memberId,
                                              @RequestBody AddressRequestDto.AddressDelete requestDto) {
        addressService.deleteAddress(memberId, requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
