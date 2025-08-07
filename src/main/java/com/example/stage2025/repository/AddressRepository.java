package com.example.stage2025.repository;

import com.example.stage2025.entity.Address;
import com.example.stage2025.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByClient(Client client);
    Optional<Address> findByClientIdAndIsDefaultTrue(Long clientId);
}
