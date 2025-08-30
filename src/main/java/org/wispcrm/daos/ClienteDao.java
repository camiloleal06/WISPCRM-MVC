package org.wispcrm.daos;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.Cliente;
import org.wispcrm.modelo.ClienteDTO;

@Repository
public interface ClienteDao extends JpaRepository<Cliente, Integer> {

    @Query(value = "SELECT count(*) FROM Cliente WHERE estado=0")
    public Long totalClientesActivos();

    Optional<Cliente> findFirstClienteByIdentificacion(String identificacion);

    Optional<Cliente> findFirstClienteByEmail(String email);

    Optional<Cliente> findFirstClienteByTelefono(String telefono);

    Optional<Cliente> findFirstClienteByDiapago(int diapago);

    List<Cliente> findByDiapagoBetween(int diaInicial, int diaFinal);

    @Cacheable(value = "lista-clientes")
    @Query("SELECT new org.wispcrm.modelo.ClienteDTO" + "(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.email, c.telefono,  p.precio, c.estado) " + " FROM Cliente c JOIN c.planes p")
    List<ClienteDTO> lista();

    @Query("SELECT new org.wispcrm.modelo.ClienteDTO" + "(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.email, c.telefono,  p.precio, c.estado) " + " FROM Cliente c JOIN c.planes p where c.estado=:estado")
    List<ClienteDTO> lista(int estado);

    @Query("SELECT new org.wispcrm.modelo.ClienteDTO(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.email , c.telefono, p.precio, c.estado) " + " FROM Cliente c JOIN c.planes p ")
    Page<ClienteDTO> listaPaginada(Pageable pageable);

    @Query("SELECT c FROM Cliente c JOIN FETCH c.planes")
    List<Cliente> findAllWithPlan();

}