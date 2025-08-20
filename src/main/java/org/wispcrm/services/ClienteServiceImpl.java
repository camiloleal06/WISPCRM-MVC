package org.wispcrm.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.wispcrm.daos.ClienteDao;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.excepciones.NotFoundException;
import org.wispcrm.interfaces.ClienteInterface;
import org.wispcrm.modelo.Cliente;
import org.wispcrm.modelo.ClienteDTO;
import org.wispcrm.modelo.Factura;

@Service
public class ClienteServiceImpl implements ClienteInterface {

    private ClienteDao clienteDao;
    private InterfaceFacturas facturaDao;

    public ClienteServiceImpl(ClienteDao clienteDao, InterfaceFacturas facturaDao) {
        this.clienteDao = clienteDao;
        this.facturaDao = facturaDao;
    }

    @Override
    public List<Cliente> findAll() {
        return clienteDao.findAll();
    }

    @Override
    public Page<Cliente> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public void save(Cliente cliente) {
        clienteDao.save(cliente);
    }

    @Override
    public Cliente findOne(Integer id) {
        return clienteDao.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el cliente"));
    }

    @Override
    public void delete(Integer id) {
        Cliente cliente = this.findById(id);
        clienteDao.deleteById(cliente.getId());
    }

    @Override
    public void saveFactura(Factura factura) {
        facturaDao.save(factura);
    }

    @Override
    public List<ClienteDTO> listaClientes() {
        return clienteDao.lista();
    }

    @Override
    public Page<ClienteDTO> listaPageable(Pageable pageable) {
        return clienteDao.listaPaginada(pageable);
    }

    @Override
    public Cliente findById(Integer id) {
        return clienteDao.findById(id).orElseThrow(() -> new NotFoundException("No Existe el cliente con ID : " + id));
    }

    @Override
    public Cliente findFirstClienteByIdentificacion(String identificacion) {
        return clienteDao.findFirstClienteByIdentificacion(identificacion).orElseThrow(
                () -> new NotFoundException("No existe el cliente con Identificacion : " + identificacion));
    }

    @Override
    public Cliente findFirstClienteByEmail(String email) {
        return clienteDao.findFirstClienteByEmail(email)
                .orElseThrow(() -> new NotFoundException("No Existe el cliente con EMAIL : " + email));
    }

    @Override
    public Cliente findFirstClienteByTelefono(String telefono) {
        return clienteDao.findFirstClienteByTelefono(telefono)
                .orElseThrow(() -> new NotFoundException("No existe cliente con Telefono : " + telefono));
    }

    @Override
    public Cliente findFirstClienteByDiapago(int diapago) {
        return clienteDao.findFirstClienteByDiapago(diapago)
                .orElseThrow(() -> new NotFoundException("No existe cliente con Dia de Pago: " + diapago));
    }

    /*
     * @Override public Cliente editarCliente(Integer id) { return
     * clienteDao.editarCliente(id); }
     */
    public Cliente updateCliente(Integer id) {
        return clienteDao.save(this.findById(id));
    }

    @Override
    public List<Cliente> findByDiaPagoBetween(int diaInicia, int diaFinal) {
        return clienteDao.findByDiapagoBetween(diaInicia, diaFinal);
    }

}
