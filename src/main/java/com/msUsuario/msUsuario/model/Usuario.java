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

    // Relación Muchos-a-Uno con Rol. Un usuario tiene un rol.
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
    private Boolean estadoUsuario; // Nuevo campo para la eliminación lógica

}