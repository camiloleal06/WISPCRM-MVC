package org.wispcrm.modelo.facturas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDto {
    private int idFactura;
    private String nombres;
    private String telefonoCliente;
    private double valorFactura;
    private int diapago;
    private int mora;
}
