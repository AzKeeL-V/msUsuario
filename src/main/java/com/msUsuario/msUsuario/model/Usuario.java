package com.msUsuario.msUsuario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data
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

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false, unique = true)
    private String correoUsuario;

    @Column(nullable = false)
    private String passUsuario;

    @Column(nullable = false)
    private Integer idTienda;
}