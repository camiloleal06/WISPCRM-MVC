package org.wispcrm.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditarClienteDTO {
    private int id;
    private String identificacion;
    private String nombres;
    private String apellidos;
    private String email;
    private String direccion;
    private String telefono;
    private int plan;
    private int diaPago;
}
