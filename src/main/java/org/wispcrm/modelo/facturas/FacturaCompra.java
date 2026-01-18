package org.wispcrm.modelo.facturas;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="factura_compras")
public class FacturaCompra {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;	
	@Column(name = "estado")
    private boolean estado;
	@Column(name = "create_at")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createAt;
}
