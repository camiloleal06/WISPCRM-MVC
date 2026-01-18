package org.wispcrm.modelo.pagos;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wispcrm.modelo.facturas.Factura;

@Entity
@Table(name = "Pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * 
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Factura factura;
    @Column(name = "fecha_pago")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaPago;
    private double pago;
    private double saldo;

    @PrePersist
    public void prePersist() {
        fechaPago = new Date();
    }

}
