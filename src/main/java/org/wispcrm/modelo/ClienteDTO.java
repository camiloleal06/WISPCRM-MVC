package org.wispcrm.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private int id;
    private String identificacion;
    private String nombres;
    private String email;
    private String telefono;
    private double valor;
    private EstadoCliente estado;
}
