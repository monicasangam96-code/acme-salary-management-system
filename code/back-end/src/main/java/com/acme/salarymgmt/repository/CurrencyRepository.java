package com.acme.salarymgmt.repository;

import com.acme.salarymgmt.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    List<Currency> findAllByIsActiveTrue();

    Optional<Currency> findByCode(String code);
}
