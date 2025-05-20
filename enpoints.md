## insertar rol: Get http://localhost:8080/roles
{
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
# rol de visitante Post http://localhost:8080/roles
{
  "nombreRol": "VISITANTE",
  "permisosRol": [
    "VER_USUARIO",
    "VER_ROL"
  ]
}

# PUT o DELETE en roles http://localhost:8080/roles/{idRol}
{
  "nombreRol": "VISIT",
  "permisosRol": [
    "VER_USUARIO",
    "VER_ROL"
  ]
}

#                           USUARIO

# Insertar un usuario Post http://localhost:8080/usuarios
{
  "nomUsuario": "Miguel",
  "apUsuario": "Fuentes administrador",
  "correoUsuario": "miguel.admin@example.com",
  "passUsuario": "AdminSeguro123",  
  "idTienda": 101,
  "rol": {
    "id": ID_DEL_ROL 
  }
}

# cambiar rol de un usuario PUT http://localhost:8080/usuarios/{idUsuario}/asignar-rol/{idRol}
    ingresa id del usuario que se modificara el rol para posteriormente asignar un rol nuevo con el id

# editar datos del usuario PUT http://localhost:8080/usuarios/1
{
  "idUsuario": 1, // si esta en la url no es necesario incluirlo
  "nomUsuario": "Felipe Nuevo Nombre", 
  "apUsuario": "Fuentes Administrador Modificado", 
  "correoUsuario": "felipe.nuevo.correo@example.com", 
  "passUsuario": "NuevaContraseñaHash", // 
  "idTienda": 101, // Mantener o modificar
  "rol": {
    "id": 1 // **¡IMPORTANTE! Solo envías el ID del rol al que pertenece.**
            // NO envíes "nombreRol" ni "permisosRol" aquí.
            // Si quieres cambiar el rol, es mejor usar el otro endpoint.
  }
} 

