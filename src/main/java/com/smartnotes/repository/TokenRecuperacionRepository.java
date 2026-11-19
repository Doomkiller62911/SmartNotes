package com.smartnotes.repository;

import com.smartnotes.domain.TokenRecuperacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {

    public Optional<TokenRecuperacion> findByToken(String token);

}
