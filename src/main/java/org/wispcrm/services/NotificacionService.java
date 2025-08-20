package org.wispcrm.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wispcrm.daos.NotificacionDao;
import org.wispcrm.interfaces.NotificacionInterface;
import org.wispcrm.modelo.Notificacion;

@Service
public class NotificacionService implements NotificacionInterface {

    @Autowired
    NotificacionDao notificacionDao;

    @Override
    public List<Notificacion> findAll() {
        return notificacionDao.findAll();
    }

    @Override
    public void save(Notificacion notificacion) {
        notificacionDao.save(notificacion);
    }

    @Override
    public Notificacion findOne(Integer id) {
        return notificacionDao.getOne(id);
    }

    @Override
    public void delete(Integer id) {
        notificacionDao.deleteById(id);

    }

    @Override
    public Notificacion findByTipo(String tipoSms) {

        return notificacionDao.findByTipo(tipoSms);
    }

}
