package org.wispcrm.modelo.clientes;

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

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.wispcrm.modelo.planes.Plan;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Cliente {
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
    private String ipAddress;
    private String pppoeUser;
    private String pppoePass;
    private EstadoCliente estado = EstadoCliente.ACTIVO;
    @Column(name = "create_at")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private Plan planes;
    @Column(name = "profile_id")
    private Integer profileId;
    @PrePersist
    public void prePersist() {
        createAt = new Date();
    }
}
