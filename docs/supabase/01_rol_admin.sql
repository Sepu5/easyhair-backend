-- Rol ADMIN para la web de administración (DSY1104 Fullstack II)
-- El registro de la app nunca crea un ADMIN (solo CLIENTE o STAFF),
-- así que la primera cuenta de administradora se crea a mano en Supabase:
--
-- 1. Registrar la cuenta normal desde la app o desde Swagger (POST /api/auth/registro)
-- 2. Correr esto en el SQL Editor de Supabase, cambiando el correo:

UPDATE autenticacion.usuarios
SET rol = 'ADMIN'
WHERE correo = 'admin@easyhair.cl';

-- 3. Cerrar sesión y volver a entrar: el token nuevo ya trae rol = ADMIN.
--    Desde ahí la administradora puede cambiar roles desde la web (Usuarios).

-- Para revisar los roles:
-- SELECT id, nombre, correo, rol FROM autenticacion.usuarios ORDER BY id;
