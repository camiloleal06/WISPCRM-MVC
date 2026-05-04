package org.wispcrm.modelo.pagos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PagoMensualDTO {
    private final int anio;
    private final int mes;
    private final Double total;
}
