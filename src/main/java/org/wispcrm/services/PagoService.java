package org.wispcrm.services;

import java.util.List;

import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wispcrm.daos.InterfacePagos;
import org.wispcrm.interfaces.InterfacePagoService;
import org.wispcrm.modelo.pagos.Pago;

@Service
public class PagoService implements InterfacePagoService {

    @Autowired
    InterfacePagos pagosDAO;

    @Override
    public List<Pago> findAll() {
        return pagosDAO.findAll();
    }

    @Override
    public void save(Pago pago) {
        pagosDAO.save(pago);
    }

    @Override
    public Pago findOne(Integer id) {
        return pagosDAO.findById(id).orElseThrow(NoResultException::new);
    }

    @Override
    public void delete(Integer id) {
        pagosDAO.deleteById(id);
    }

}
