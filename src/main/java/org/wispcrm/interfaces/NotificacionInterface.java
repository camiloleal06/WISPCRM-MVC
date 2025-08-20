package org.wispcrm.interfaces;

import java.util.List;

import org.wispcrm.modelo.Notificacion;

public interface NotificacionInterface {

    List<Notificacion> findAll();

    void save(Notificacion notificacion);

    Notificacion findOne(Integer id);

    Notificacion findByTipo(String tipoSms);

    void delete(Integer id);

}
