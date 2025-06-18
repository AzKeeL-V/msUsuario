package com.usuario.usuario.model;

import jakarta.persistence.*;
import lombok.Data; // Vuelve a @Data para simplificar si prefieres
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// YA NO NECESITAS RepresentationModel aquí
// import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "usuarios")
@Data // Volvemos a @Data para simplificar, el warning no aparecerá al no extender RepresentationModel
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(nullable = false)
    private String nomUsuario;

    @Column(nullable = false)
    private String apUsuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @Column(nullable = false, unique = true)
    private String correoUsuario;

    @Column(nullable = false, length = 30)
    private String passUsuario; 

    @Column(nullable = false)
    private Integer idTienda;

    @Column(nullable = false)
    private Boolean estadoUsuario;
}