package org.wispcrm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wispcrm.modelo.Notificacion;

public interface NotificacionDao extends JpaRepository<Notificacion, Integer> {

    Notificacion findByTipo(String tipoSms);

}
