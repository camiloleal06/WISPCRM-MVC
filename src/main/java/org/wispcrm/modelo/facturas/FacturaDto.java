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
    private int clienteId;

    public FacturaDto(int idFactura, String nombres, String telefonoCliente, double valorFactura, int diapago, int mora) {
        this.idFactura = idFactura;
        this.nombres = nombres;
        this.telefonoCliente = telefonoCliente;
        this.valorFactura = valorFactura;
        this.diapago = diapago;
        this.mora = mora;
    }
}
