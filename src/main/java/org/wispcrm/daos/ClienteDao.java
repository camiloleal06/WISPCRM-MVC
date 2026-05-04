package org.wispcrm.daos;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.clientes.ClienteDTO;
import org.wispcrm.modelo.clientes.EditarClienteDTO;

@Repository
public interface ClienteDao extends JpaRepository<Cliente, Integer> {

    @Query(value = "SELECT count(*) FROM Cliente WHERE estado=0")
    public Long totalClientesActivos();

    @Query(value = "SELECT count(*) FROM Cliente WHERE estado=1")
    Long totalClientesInactivos();

    @Query(value = "SELECT count(*) FROM Cliente WHERE estado=2")
    Long totalClientesSuspendidos();

    @Query(value = "SELECT count(*) FROM Cliente WHERE MONTH(create_at)=MONTH(CURRENT_TIMESTAMP) AND YEAR(create_at)=YEAR(CURRENT_TIMESTAMP)")
    Long clientesNuevosMes();

    @Query(value = "SELECT COALESCE(SUM(p.precio), 0) FROM Cliente c JOIN c.planes p WHERE c.estado = 0")
    Long ingresoEsperado();

    Optional<Cliente> findFirstClienteByIdentificacion(String identificacion);

    Optional<Cliente> findFirstClienteByEmail(String email);

    Optional<Cliente> findFirstClienteByTelefono(String telefono);

    Optional<Cliente> findFirstClienteByDiapago(int diapago);

    List<Cliente> findByDiapagoBetween(int diaInicial, int diaFinal);

    @Cacheable(value = "lista-clientes")
    @Query("SELECT new org.wispcrm.modelo.clientes.ClienteDTO" + "(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.email, c.telefono,  p.precio, c.estado) " + " FROM Cliente c JOIN c.planes p")
    List<ClienteDTO> lista();

    @Query("SELECT new org.wispcrm.modelo.clientes.ClienteDTO" + "(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.email, c.telefono,  p.precio, c.estado) " + " FROM Cliente c JOIN c.planes p where c.estado=:estado")
    List<ClienteDTO> lista(int estado);

    @Query("SELECT new org.wispcrm.modelo.clientes.EditarClienteDTO (c.id, c.identificacion, c.nombres,c.apellidos, " +
            "c.email, c.telefono,c.diapago, c.direccion,c.ipAddress,c.pppoeUser,c.pppoePass,p.id,c.profileId) " +
            "FROM Cliente c JOIN c.planes p where c.id=:idCliente")
    EditarClienteDTO clienteById(int idCliente);


    @Query("SELECT new org.wispcrm.modelo.clientes.ClienteDTO(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.email , c.telefono, p.precio, c.estado) " + " FROM Cliente c JOIN c.planes p ")
    Page<ClienteDTO> listaPaginada(Pageable pageable);

}