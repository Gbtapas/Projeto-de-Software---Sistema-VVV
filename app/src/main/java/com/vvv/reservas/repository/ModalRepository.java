package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Modal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModalRepository extends JpaRepository<Modal, Integer> {
    @Query("SELECT m FROM Modal m JOIN FETCH m.transportadora ORDER BY m.codigo ASC")
    List<Modal> findAllByOrderByCodigoAsc();
}
