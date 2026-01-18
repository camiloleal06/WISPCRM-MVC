package org.wispcrm.modelo.facturas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResumenFacturasDTO {
    private final Long cantidadFacturas;
    private final Double totalValor;
}
