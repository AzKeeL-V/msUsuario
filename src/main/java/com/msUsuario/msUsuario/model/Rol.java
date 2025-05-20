package com.msUsuario.msUsuario.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombreRol;

    // Colección de permisos para este rol. Cargados de forma EAGER.
    @ElementCollection(targetClass = Permiso.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso")
    private List<Permiso> permisosRol;
}