package org.wispcrm.modelo;


public class ResumenFacturasDTO {
    private Long cantidadFacturas;
    private Double totalValor;

    public ResumenFacturasDTO(Long cantidadFacturas, Double totalValor) {
        this.cantidadFacturas = cantidadFacturas;
        this.totalValor = totalValor;
    }

    // Getters y setters
    public Long getCantidadFacturas() {
        return cantidadFacturas;
    }

    public Double getTotalValor() {
        return totalValor;
    }
}
