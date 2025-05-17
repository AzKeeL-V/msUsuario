package com.msUsuario.msUsuario.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    @ManyToOne(fetch = FetchType.EAGER) // Carga el rol junto con el usuario
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false, unique = true)
    private String correoUsuario;

    @Column(nullable = false)
    private String passUsuario;

    @Column(nullable = false)
    private Integer idTienda;
}