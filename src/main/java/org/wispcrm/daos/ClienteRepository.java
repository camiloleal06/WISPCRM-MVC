package org.wispcrm.daos;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.clientes.ClienteDTO;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    @Query("SELECT p FROM Cliente p WHERE p.nombres LIKE %?1%")
    List<Cliente> search(String keyword);
    
	@Query("SELECT new org.wispcrm.modelo.clientes.ClienteDTO(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
			+ "c.email, c.telefono,  p.precio, c.estado) "
			+ " FROM Cliente c JOIN c.planes p ")
	List<ClienteDTO> lista();
	
	@Query("SELECT new org.wispcrm.modelo.clientes.ClienteDTO(c.id, c.identificacion, CONCAT(c.nombres,' ',c.apellidos), "
			+ "c.email , c.telefono, p.precio, c.estado) "
			+ " FROM Cliente c JOIN c.planes p ")
	Page<ClienteDTO> lista(Pageable pageable);
	
}