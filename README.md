# Títulos Universitarios Digitales

Proyecto del curso Ingeniería de Software Aplicada III — Grupo 1GS231.

## Requisitos

- JDK 21
- Git
- PostgreSQL (para correr la app localmente)
- IntelliJ IDEA (recomendado) o VS Code
- Docker Desktop con el motor WSL 2 (solo para el monitoreo con Prometheus)

## Configuración de base de datos (PostgreSQL)

Cada integrante debe:

1. Tener PostgreSQL instalado localmente, con una base de datos llamada `titulos_db`.
2. En IntelliJ: **Edit Configurations** → tu configuración de ejecución → **Environment variables** → agregar las variables, separadas por `;`:
```
   DB_PASSWORD=tu_contraseña_local;PROMETHEUS_PASSWORD=contraseña_de_12_o_mas_caracteres
```
3. El usuario por defecto es `postgres`. Si usas otro usuario, agrega también:
```
   DB_USERNAME=tu_usuario
```

### Variables de entorno

| Variable | Obligatoria | Para qué sirve |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL. |
| `DB_USERNAME` | No (por defecto `postgres`) | Usuario de PostgreSQL. |
| `PROMETHEUS_PASSWORD` | Para el monitoreo | Contraseña del usuario técnico `prometheus` (rol `MONITOREO`), mínimo 12 caracteres. Sin ella la app arranca, pero no crea ese usuario y Prometheus no puede leer las métricas (HU-4.10). |

Las contraseñas nunca se escriben en el repositorio.

Las pruebas (`mvnw test`) usan H2 en memoria automáticamente y **no** requieren PostgreSQL corriendo ni estas variables.

## Cambios manuales en la base de datos local

La app usa `spring.jpa.hibernate.ddl-auto=update`: Hibernate crea las tablas y columnas nuevas, pero **no modifica las que ya existen** (restricciones, longitudes, columnas obligatorias). Por eso algunos cambios necesitan un paso manual en una base creada antes.

**Después de cada `git pull`, revisa esta lista.** Si tu base de datos es anterior al cambio, aplica el SQL en pgAdmin (Query Tool sobre `titulos_db`). Alternativa: borrar y volver a crear `titulos_db` (se pierden los datos locales).

> **Regla del equipo:** todo PR que necesite un cambio manual en la base de datos agrega aquí una entrada con la fecha, la historia, el PR, el síntoma si no se aplica, cómo comprobarlo y el SQL. Las entradas nuevas van arriba.


### 2026-10-22 · HU-4.7 (PR #23): restricciones en `titulo` y `graduados`

**Síntoma si no se aplica:** ninguno visible. La app funciona, pero la base de datos acepta valores nulos o más largos de lo permitido y CA4 no se cumple en tu equipo.

**Comprobar:**
```sql
   SELECT table_name, column_name, is_nullable, character_maximum_length
   FROM information_schema.columns
   WHERE (table_name = 'titulo'
          AND column_name IN ('nombre_graduado', 'nombre_titulo', 'fecha_registro', 'graduado_id', 'identificador'))
      OR (table_name = 'graduados' AND column_name = 'numero_documento')
   ORDER BY table_name, column_name;
```
Correcto: `is_nullable = NO` en todas menos `identificador`, y longitudes 120 (`nombre_graduado`), 150 (`nombre_titulo`), 100 (`identificador`) y 20 (`numero_documento`).

**Si no coincide, aplica:**
```sql
   ALTER TABLE titulo
       ALTER COLUMN nombre_graduado TYPE varchar(120),
       ALTER COLUMN nombre_graduado SET NOT NULL,
       ALTER COLUMN nombre_titulo TYPE varchar(150),
       ALTER COLUMN nombre_titulo SET NOT NULL,
       ALTER COLUMN fecha_registro SET NOT NULL,
       ALTER COLUMN identificador TYPE varchar(100),
       ALTER COLUMN graduado_id SET NOT NULL;
   ALTER TABLE graduados
       ALTER COLUMN numero_documento TYPE varchar(20);
```
Si falla porque hay filas con valores nulos o más largos (por ejemplo, títulos de antes de HU-1.10 sin graduado), usa la alternativa: recrear `titulos_db`.

### 2026-10-21 · HU-4.10 (PR #24): rol `MONITOREO`

**Síntoma si no se aplica:** con `PROMETHEUS_PASSWORD` definida, la app no arranca y el log muestra un error que menciona `usuarios_rol_check`.

**Comprobar:**
```sql
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'usuarios'::regclass AND contype = 'c';
```

**Si `usuarios_rol_check` no incluye `'MONITOREO'`, reemplázala:**
```sql
ALTER TABLE usuarios DROP CONSTRAINT usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('APROBADOR', 'REGISTRADOR', 'MONITOREO'));
```

## Correr la aplicación

```
.\mvnw.cmd spring-boot:run
```

## Correr las pruebas

```
.\mvnw.cmd clean test
```

## Monitoreo con Prometheus

Requiere Docker Desktop y la variable `PROMETHEUS_PASSWORD`. Pasos, usuarios con acceso a las métricas y verificación: [`docs/prometheus.md`](docs/prometheus.md).

## Generador de identificadores

Para los títulos aprobados se utiliza un generador basado en UUID
(`UUID.randomUUID()`).

Se eligió UUID porque permite generar identificadores no secuenciales,
sin depender de un contador incremental de la base de datos. Cada
identificador generado tiene una probabilidad extremadamente baja de
repetirse, lo que permite identificar los títulos de forma única.

El identificador se genera mediante la clase `IdentificadorGenerator`,
ubicada en:

`src/main/java/pa/edu/utp/titulos_universitarios/IdentificadorGenerator.java`