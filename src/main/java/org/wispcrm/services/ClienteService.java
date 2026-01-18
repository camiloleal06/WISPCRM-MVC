package org.wispcrm.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.wispcrm.daos.ClienteRepository;
import org.wispcrm.modelo.clientes.Cliente;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class ClienteService {

    private ClienteRepository repo;

    public List<Cliente> listAll(String keyword) {
        if (keyword != null) {
            return repo.search(keyword);
        }
        return repo.search("a").subList(0, 10);

    }



}