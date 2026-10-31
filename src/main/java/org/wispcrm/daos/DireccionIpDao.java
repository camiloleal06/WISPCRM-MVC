package org.wispcrm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.red.DireccionIp;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionIpDao extends JpaRepository<DireccionIp, Integer> {

    List<DireccionIp> findByDisponibleTrue();

    Optional<DireccionIp> findByIp(String ip);
}
