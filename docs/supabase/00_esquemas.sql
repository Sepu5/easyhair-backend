-- Un esquema por microservicio
-- (no usar "auth": es un esquema reservado de Supabase)
CREATE SCHEMA IF NOT EXISTS autenticacion;
CREATE SCHEMA IF NOT EXISTS catalogo;
CREATE SCHEMA IF NOT EXISTS reservas;
CREATE SCHEMA IF NOT EXISTS inventario;
CREATE SCHEMA IF NOT EXISTS finanzas;