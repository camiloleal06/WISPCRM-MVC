package org.wispcrm.modelo.pagos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {
private int id;
private double pago;
private int facturaId;
private String nombre;
private Date fechapago;
private boolean estado;
 }
