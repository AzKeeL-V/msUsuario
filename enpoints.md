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

