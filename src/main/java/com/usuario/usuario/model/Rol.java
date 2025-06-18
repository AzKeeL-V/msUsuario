package com.usuario.usuario.model;

import jakarta.persistence.*;
import lombok.Data; // Vuelve a @Data para simplificar si prefieres
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

// YA NO NECESITAS RepresentationModel aquí
// import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "roles")
@Data // Volvemos a @Data para simplificar, el warning no aparecerá al no extender RepresentationModel
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    @ElementCollection(targetClass = Permiso.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso", length = 50)
    private List<Permiso> permisosRol;

    @Column(nullable = false)
    private Boolean estadoRol;
}