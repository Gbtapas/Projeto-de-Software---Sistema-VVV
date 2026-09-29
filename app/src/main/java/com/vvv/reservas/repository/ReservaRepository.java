package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.enums.CanalReserva;
import com.vvv.reservas.model.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByCanalOrderByDataCriacaoDesc(CanalReserva canal);

    @Query("SELECT r FROM Reserva r JOIN FETCH r.programacao p JOIN FETCH p.modal m JOIN FETCH m.transportadora " +
           "WHERE r.status = :status ORDER BY r.dataCriacao DESC")
    List<Reserva> findByStatusOrderByDataCriacaoDesc(@Param("status") StatusReserva status);

    // JOIN FETCH necessário porque open-in-view=false e cidadeOrigem/Destino são LAZY
    @Query("SELECT r FROM Reserva r " +
           "JOIN FETCH r.passageiro p " +
           "JOIN FETCH r.programacao prog " +
           "JOIN FETCH prog.rota rota " +
           "JOIN FETCH rota.cidadeOrigem " +
           "JOIN FETCH rota.cidadeDestino " +
           "JOIN FETCH prog.modal " +
           "WHERE p.cliente.usuario.email = :email " +
           "ORDER BY r.dataCriacao DESC")
    List<Reserva> findMinhasReservas(@Param("email") String email);

    boolean existsByIdAndPassageiro_Cliente_Usuario_Email(Long id, String email);
}
