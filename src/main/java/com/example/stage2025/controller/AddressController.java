package com.example.stage2025.controller;

import com.example.stage2025.dto.AddressDto;
import com.example.stage2025.entity.Client;
import com.example.stage2025.service.AddressService;
import com.example.stage2025.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@PreAuthorize("hasRole('CLIENT')")
public class AddressController {

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressDto>> getClientAddresses() {
        Long clientId = SecurityUtils.getCurrentUserId();
        List<AddressDto> addresses = addressService.getClientAddresses(clientId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> getAddressById(@PathVariable Long id) {
        Long clientId = SecurityUtils.getCurrentUserId();
        AddressDto address = addressService.getClientAddressById(clientId, id);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<AddressDto> addAddress(@Valid @RequestBody AddressDto addressDto) {
        Long clientId = SecurityUtils.getCurrentUserId();
        AddressDto newAddress = addressService.addAddress(clientId, addressDto);
        return new ResponseEntity<>(newAddress, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDto addressDto) {
        Long clientId = SecurityUtils.getCurrentUserId();
        AddressDto updatedAddress = addressService.updateAddress(clientId, id, addressDto);
        return ResponseEntity.ok(updatedAddress);
    }

    @PatchMapping("/{id}/set-default")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable Long id) {
        Long clientId = SecurityUtils.getCurrentUserId();
        addressService.setDefaultAddress(clientId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        Long clientId = SecurityUtils.getCurrentUserId();
        addressService.deleteAddress(clientId, id);
        return ResponseEntity.noContent().build();
    }
}
