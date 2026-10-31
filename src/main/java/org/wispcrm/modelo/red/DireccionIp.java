package org.wispcrm.modelo.red;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "direcciones_ip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String ip;

    @Column(nullable = false)
    private boolean disponible = true;
}
