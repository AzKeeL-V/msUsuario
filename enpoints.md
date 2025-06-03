# Endpoints usuario
// falta filtrar por rol a todos los usuarios
poblar datos con un script 


| Método HTTP | Ruta                                 | Acción                                                     |
| ----------- | ------------------------------------ | ---------------------------------------------------------- |
| `POST`      | `/usuarios`                          | Crea un nuevo usuario                                      |
| `GET`       | `/usuarios`                          | Lista todos los usuarios                                   |
| `GET`       | `/usuarios/{id}`                     | Obtiene un usuario por su ID                               |
| `PUT`       | `/usuarios/{id}`                     | Actualiza los datos de un usuario                          |
| `DELETE`    | `/usuarios/{id}`                     | Elimina un usuario                                         |
| `PUT`       | `/usuarios/{id}/asignar-rol/{rolId}` | Asigna un rol a un usuario                                 |
| `GET`       | `/usuarios/tienda/{idTienda}`        | Lista todos los usuarios asociados a una tienda específica |

# JSON usuario

    {
    "nomUsuario": "filip",
    "apUsuario": "rubio",
     "rol": {
      "id": 3  
     },
     "correoUsuario": "filip.rubio@example.com", 
     "passUsuario": "passwordnazzi",
    "idTienda": 1
    }

# Endpoints Roles

| Método HTTP | Ruta          | Acción                        |
| ----------- | ------------- | ----------------------------- |
| `POST`      | `/roles`      | Crea un nuevo rol             |
| `GET`       | `/roles`      | Lista todos los roles         |
| `GET`       | `/roles/{id}` | Obtiene un rol por su ID      |
| `PUT`       | `/roles/{id}` | Actualiza los datos de un rol |
| `DELETE`    | `/roles/{id}` | Elimina un rol                |

# JSON Rol de Administrador

{
  "id": 1,
  "nombreRol": "ADMINISTRADOR",
  "permisosRol": [
    "CREAR_USUARIO",
    "ELIMINAR_USUARIO",
    "ACTUALIZAR_USUARIO",
    "GESTIONAR_PERMISOS",
    "VER_USUARIO",
    "CREAR_ROL",
    "ACTUALIZAR_ROL",
    "ELIMINAR_ROL",
    "VER_ROL"
  ]
}

# Json Rol de Visitante

{
  "id": 2,
  "nombreRol": "VISITANTE",
  "permisosRol": [
    "VER_USUARIO",
    "VER_ROL"
  ]
}


------------------------------------------------------------------
# INSERTS PARA ROLES

INSERT INTO roles (id, nombre_rol) VALUES (1, 'ADMIN');
INSERT INTO roles (id, nombre_rol) VALUES (2, 'EMPLEADO');
INSERT INTO roles (id, nombre_rol) VALUES (3, 'VISITANTE');

# inserts para rol_permisos

-- Permisos para ADMIN (rol_id = 1)
INSERT INTO rol_permisos (rol_id, permiso) VALUES
(1, 'CREAR_USUARIO'),
(1, 'ELIMINAR_USUARIO'),
(1, 'ACTUALIZAR_USUARIO'),
(1, 'GESTIONAR_PERMISOS'),
(1, 'VER_USUARIO'),
(1, 'CREAR_ROL'),
(1, 'ACTUALIZAR_ROL'),
(1, 'ELIMINAR_ROL'),
(1, 'VER_ROL'),
(1, 'ASIG_ROL'),
(1, 'CREAR_RESPALDO'),
(1, 'RESTAURAR_DATOS'),
(1, 'MONITOREAR_SISTEMA');

-- Permisos para EMPLEADO (rol_id = 2)
INSERT INTO rol_permisos (rol_id, permiso) VALUES
(2, 'CREAR_USUARIO'),
(2, 'ACTUALIZAR_USUARIO'),
(2, 'VER_USUARIO'),
(2, 'VER_ROL');

-- Permisos para VISITANTE (rol_id = 3)
INSERT INTO rol_permisos (rol_id, permiso) VALUES
(3, 'VER_USUARIO'),
(3, 'VER_ROL');

# insert de usuarios 
INSERT INTO usuarios (nom_usuario, ap_usuario, rol_id, correo_usuario, pass_usuario, id_tienda) VALUES
('wacoldo', 'soto', 1, 'wacoldito@example.com', 'password123', 101),
('María', 'González', 2, 'maria.gonzalez@example.com', 'securePass456', 102),
('zoila', 'Ramírez', 3, 'zoila.ramirez@example.com', 'myPassword789', 103);

# inconsistencia en base de datos con el estado de el usuario y roles