package com.example.stage2025.service;

import com.example.stage2025.dto.AddressDto;

import java.util.List;

public interface AddressService {
    List<AddressDto> getClientAddresses(Long clientId);
    AddressDto getClientAddressById(Long clientId, Long addressId);
    AddressDto addAddress(Long clientId, AddressDto addressDto);
    AddressDto updateAddress(Long clientId, Long addressId, AddressDto addressDto);
    void deleteAddress(Long clientId, Long addressId);
    void setDefaultAddress(Long clientId, Long addressId);
}
