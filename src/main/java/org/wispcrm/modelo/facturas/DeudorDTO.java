package org.wispcrm.modelo.facturas;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeudorDTO {
    private final int clienteId;
    private final String nombres;
    private final String telefono;
    private final long cantidadFacturas;
    private final double totalDeuda;
}
