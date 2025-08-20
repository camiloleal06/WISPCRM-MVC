package org.wispcrm.modelo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String identificacion;
    private int diapago;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String direccion;
    private String ipaddres;
    private EstadoCliente estado = EstadoCliente.ACTIVO;
    @Column(name = "create_at")
    @Temporal(TemporalType.DATE)

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Plan planes;

    @PrePersist
    public void prePersist() {
        createAt = new Date();
    }

}
