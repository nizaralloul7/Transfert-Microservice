package com.example.transfereservice.repository;

import com.example.transfereservice.model.Transfere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransfereRepository extends JpaRepository<Transfere,Long>
{
    Optional<Transfere> findTransfereByReference(String reference);
    List<Transfere> findTransfereByReferenceClientDonneur(String referenceClientDonneur);
}
