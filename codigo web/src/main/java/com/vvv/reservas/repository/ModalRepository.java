package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Modal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModalRepository extends JpaRepository<Modal, Integer> {
    List<Modal> findAllByOrderByCodigoAsc();
}
