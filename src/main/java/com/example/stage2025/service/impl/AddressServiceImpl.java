package com.example.stage2025.service.impl;

import com.example.stage2025.dto.AddressDto;
import com.example.stage2025.entity.Address;
import com.example.stage2025.entity.Client;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.AddressMapper;
import com.example.stage2025.repository.AddressRepository;
import com.example.stage2025.repository.ClientRepository;
import com.example.stage2025.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final ClientRepository clientRepository;
    private final AddressMapper addressMapper;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, ClientRepository clientRepository, AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.clientRepository = clientRepository;
        this.addressMapper = addressMapper;
    }

    @Override
    public List<AddressDto> getClientAddresses(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        return addressRepository.findByClient(client).stream()
                .map(addressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDto getClientAddressById(Long clientId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        if (!address.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Address with id " + addressId + " does not belong to client with id " + clientId);
        }
        return addressMapper.toDto(address);
    }

    @Override
    @Transactional
    public AddressDto addAddress(Long clientId, AddressDto addressDto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // If new address is set as default, unset current default
        if (addressDto.isDefault()) {
            addressRepository.findByClientIdAndIsDefaultTrue(clientId)
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        } else {
            // If no addresses exist, make this the default regardless of the flag
            if (addressRepository.findByClient(client).isEmpty()) {
                addressDto.setDefault(true);
            }
        }

        Address address = addressMapper.toEntity(addressDto);
        address.setClient(client);
        return addressMapper.toDto(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressDto updateAddress(Long clientId, Long addressId, AddressDto addressDto) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!existingAddress.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Address with id " + addressId + " does not belong to client with id " + clientId);
        }

        // If updated address is set as default, unset current default
        if (addressDto.isDefault() && !existingAddress.isDefault()) {
            addressRepository.findByClientIdAndIsDefaultTrue(clientId)
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        } else if (!addressDto.isDefault() && existingAddress.isDefault()) {
            // If current default is being unset, ensure there's another default or handle logic
            // For simplicity, we'll allow unsetting, but a more robust system might require a new default to be set.
        }


        existingAddress.setLabel(addressDto.getLabel());
        existingAddress.setStreet(addressDto.getStreet());
        existingAddress.setCity(addressDto.getCity());
        existingAddress.setState(addressDto.getState());
        existingAddress.setZipCode(addressDto.getZipCode());
        existingAddress.setCountry(addressDto.getCountry());
        existingAddress.setDefault(addressDto.isDefault());

        return addressMapper.toDto(addressRepository.save(existingAddress));
    }

    @Override
    @Transactional
    public void deleteAddress(Long clientId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Address with id " + addressId + " does not belong to client with id " + clientId);
        }

        if (address.isDefault()) {
            // Prevent deleting the last default address without setting a new one
            List<Address> clientAddresses = addressRepository.findByClient(address.getClient());
            if (clientAddresses.size() > 1) {
                // Find another address and set it as default
                clientAddresses.stream()
                        .filter(addr -> !addr.getId().equals(addressId))
                        .findFirst()
                        .ifPresent(newDefault -> {
                            newDefault.setDefault(true);
                            addressRepository.save(newDefault);
                        });
            }
        }
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long clientId, Long addressId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        Address newDefaultAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!newDefaultAddress.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Address with id " + addressId + " does not belong to client with id " + clientId);
        }

        // Unset current default address for the client
        addressRepository.findByClientIdAndIsDefaultTrue(clientId)
                .ifPresent(currentDefault -> {
                    currentDefault.setDefault(false);
                    addressRepository.save(currentDefault);
                });

        // Set the new default address
        newDefaultAddress.setDefault(true);
        addressRepository.save(newDefaultAddress);
    }
}
