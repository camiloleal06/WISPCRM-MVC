/**
 * 
 */
package org.wispcrm.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.wispcrm.daos.OperarioDao;
import org.wispcrm.daos.OrdenDao;
import org.wispcrm.daos.TipoOrdenDao;
import org.wispcrm.excepciones.NotFoundException;
import org.wispcrm.interfaces.OrdenInterface;
import org.wispcrm.modelo.ordenes.Operario;
import org.wispcrm.modelo.ordenes.Orden;
import org.wispcrm.modelo.ordenes.TipoOrden;

import lombok.AllArgsConstructor;

import org.wispcrm.modelo.ordenes.EstadoOrden;

/**
 * @author camilo.leal
 *
 */
@Service
@AllArgsConstructor
public class OrdenService implements OrdenInterface {

    private final OrdenDao ordenDao;
    private final TipoOrdenDao tipoOrdenDao;
    private final OperarioDao operarioDao;

    @Override
    public List<Orden> findAll() {
        return this.ordenDao.findAll();
    }

    @Override
    public Orden createOrden(Orden orden) {
        return this.ordenDao.save(orden);
    }

    @Override
    public Orden findOrdenById(Integer id) {
        return this.ordenDao.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontraron ordenes con este ID"));
    }

    @Override
    public List<TipoOrden> findAllTipoOrden() {
        return this.tipoOrdenDao.findAll();
    }

    @Override
    public List<Operario> findAllOperario() {
        return this.operarioDao.findAll();
    }

    @Override
    public Orden cerrarOrden(Integer id, String comentario) {
        Orden orden = findOrdenById(id);
        orden.setEstado(EstadoOrden.CERRADA);
        orden.setComentarioCierre(comentario);
        orden.setFechaFin(new java.util.Date());
        return this.ordenDao.save(orden);
    }

    @Override
    public List<Orden> cerrarTodasAbiertas() {
        List<Orden> abiertas = this.ordenDao.findByEstado(EstadoOrden.ABIERTA);
        abiertas.forEach(o -> {
            o.setEstado(EstadoOrden.CERRADA);
            o.setComentarioCierre("Cerrada masivamente");
            o.setFechaFin(new java.util.Date());
        });
        return this.ordenDao.saveAll(abiertas);
    }

}
