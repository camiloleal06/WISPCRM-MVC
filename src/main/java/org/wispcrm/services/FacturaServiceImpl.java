package org.wispcrm.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.excepciones.NotFoundException;
import org.wispcrm.interfaces.FacturaInterface;
import org.wispcrm.modelo.facturas.Factura;
import org.wispcrm.modelo.facturas.FacturaDto;

@Service
public class FacturaServiceImpl implements FacturaInterface {
    @Autowired
    private InterfaceFacturas facturaDao;

    @Override
    public List<Factura> findAll() {
        return (List<Factura>) facturaDao.findAll();
    }

    @Override
    public Factura findFacturabyid(Integer id) {
        return facturaDao.findById(id).orElse(null);
    }

    @Override
    public Long pendientes() {
        if (facturaDao.totalFacturasPendientesMes() != null) {
            return facturaDao.totalFacturasPendientesMes();
        } else {
            return 0L;
        }
    }

    @Override
    public Long pagadas() {
        if (facturaDao.totalFacturasPagadasMes() != null) {
            return facturaDao.totalFacturasPagadasMes();
        } else {
            return 0L;
        }
    }

    @Override
    public Long facturas() {
        if (facturaDao.totalCantidadFacturasMes() != null)
            return facturaDao.totalCantidadFacturasMes();
        else
            return 0L;
    }

    @Override
    public List<FacturaDto> listadoFacturas() {
        return facturaDao.listadoFacturasPendientes();
    }

    @Override
    public void delete(int id) {
        facturaDao.deleteById(facturaDao.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el usuario con ID : " + id)).getId());
    }

    @Override
    public Factura save(Factura factura) {
        return facturaDao.save(factura);
    }

    public boolean existeFacturaPeriodo(){
        return facturaDao.existFacturaCreada() > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

}
