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

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    // Colección de permisos para este rol. Cargados de forma EAGER.
    @ElementCollection(targetClass = Permiso.class, fetch = FetchType.EAGER) //Indica que permisosRol no es otra entidad (como @OneToMany), sino una colección de valores simples o enums (en este caso, enums).
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso", length = 50) 
    private List<Permiso> permisosRol;
}