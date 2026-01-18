package org.wispcrm.modelo.clientes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditarClienteDTO {
    private Integer id;
    private String identificacion;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private int diapago;
    private String direccion;
    private String ipAddress;
    private String pppoeUser;
    private String pppoePass;
    private Integer planesId;
    private Integer profileId;
}
