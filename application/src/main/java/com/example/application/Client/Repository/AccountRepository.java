package com.example.application.Client.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.Client.Entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}