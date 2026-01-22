package com.smplatform.product_service.domain.member.service.impl;

import com.smplatform.product_service.domain.member.dto.AddressRequestDto;
import com.smplatform.product_service.domain.member.dto.AddressResponseDto;
import com.smplatform.product_service.domain.member.entity.Address;
import com.smplatform.product_service.domain.member.exception.AddressNotFoundException;
import com.smplatform.product_service.domain.member.exception.MemberNotFoundException;
import com.smplatform.product_service.domain.member.repository.AddressRepository;
import com.smplatform.product_service.domain.member.repository.MemberRepository;
import com.smplatform.product_service.domain.member.service.AddressService;
import com.smplatform.product_service.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Long saveAddress(String memberId, AddressRequestDto.AddressSave requestDto) {
        Address address = requestDto.toEntity();
        address.setMember(
                memberRepository.findById(memberId)
                        .orElseThrow(() -> new MemberNotFoundException("멤버 " + memberId + "를 찾을 수 없습니다."))
        );
        if (requestDto.getIsDefault() == 1) {
            addressRepository.findByMember_MemberIdAndIsDefault(memberId, true)
                    .ifPresent(existingDefault -> existingDefault.setIsDefault(false));
        }
        Address addressResult = addressRepository.save(address);
        return addressResult.getAddressId();
    }

    @Override
    public List<AddressResponseDto.AddressGet> getAddresses(String memberId) {
        List<Address> addresses = addressRepository.findAllByMemberMemberId(memberId);
        return addresses.stream()
                .map(AddressResponseDto.AddressGet::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long updateAddress(String memberId, AddressRequestDto.AddressUpdate requestDto) {
        Address address = addressRepository.findById(requestDto.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException(String.format("주소 : %s 를 찾지 못했습니다.", requestDto.getAddressId())));
        if (!address.getMember().getMemberId().equals(memberId)) {
            throw new UnauthorizedException("본인이 아닌 경우 수정할 수 없습니다.");
        }
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByMember_MemberIdAndIsDefault(memberId, true)
                    .ifPresent(existingDefault -> existingDefault.setIsDefault(false));
        }
        address.update(requestDto);
        return address.getAddressId();
    }

    @Override
    public void deleteAddress(String memberId, AddressRequestDto.AddressDelete requestDto) {
        Address address = addressRepository.findById(requestDto.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException(String.format("주소 : %s 를 찾지 못했습니다.", requestDto.getAddressId())));
        if (!memberId.equals(address.getMember().getMemberId())) {
            throw new UnauthorizedException("본인이 아닌 경우 삭제 할 수 없습니다.");
        }
        addressRepository.delete(address);
    }

}
