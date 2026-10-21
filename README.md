
# Proyecto 2 - Sistema de Reserva de Recursos

## Configuración de MySQL

Cada integrante puede tener una base de datos local para desarrollar y probar su parte.

1. Instalar MySQL Server y MySQL Workbench.
2. Abrir `database/esquema_reservas_mysql.sql` en Workbench y ejecutar el script para crear la base de datos y las tablas. 
3. Si se necesitan los datos iniciales, ejecutar el importador XML del módulo `backend` sobre una base de datos vacía.
4. El script database/esquema_reservas_mysql.sql crea la base de datos y las siete tablas, pero no
5. crea el usuario ni la contraseña para JDBC. Cada compañero debe configurar sus propias credenciales y conceder los permisos necesarios.

## Conexión

El módulo `backend` utiliza JDBC para conectarse a la base de datos `reservas_recursos`.

Configurar estas variables de entorno en IntelliJ IDEA:

- `RESERVAS_DB_URL`: `jdbc:mysql://localhost:3306/reservas_recursos`
- `RESERVAS_DB_USER`: usuario de MySQL configurado localmente.
- `RESERVAS_DB_PASSWORD`: contraseña de ese usuario.

No publicar contraseñas reales en GitHub.

## Importante

GitHub contiene el script SQL, pero no una base de datos activa. Cada integrante debe crear su propia base local si la necesita.

En la arquitectura final, únicamente el Backend se conecta a MySQL. El Frontend se comunica con el Backend mediante sockets y no debe acceder directamente a la base de datos.
