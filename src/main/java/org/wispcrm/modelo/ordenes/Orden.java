package org.wispcrm.modelo.ordenes;

import java.util.Date;

import javax.persistence.*;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wispcrm.modelo.clientes.Cliente;

@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.EAGER)
    private TipoOrden tipoOrden;
    @ManyToOne(fetch = FetchType.EAGER)
    private Operario operario;
    @ManyToOne(fetch = FetchType.EAGER)
    private Cliente cliente;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaInicio;
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaFin;
    private String observacion;
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    @Enumerated(EnumType.STRING)
    private EstadoOrden estado = EstadoOrden.ABIERTA;
    @Column(columnDefinition = "TEXT")
    private String comentarioCierre;
}
