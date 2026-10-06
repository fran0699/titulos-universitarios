# Títulos Universitarios Digitales

Proyecto del curso Ingeniería de Software Aplicada III — Grupo 1GS231.

## Requisitos

- JDK 21
- Git
- PostgreSQL (para correr la app localmente)
- IntelliJ IDEA (recomendado) o VS Code

## Configuración de base de datos (PostgreSQL)

Cada integrante debe:

1. Tener PostgreSQL instalado localmente, con una base de datos llamada `titulos_db`.
2. En IntelliJ: **Edit Configurations** → tu configuración de ejecución → **Environment variables** → agregar:
```
   DB_PASSWORD=tu_contraseña_local
```
3. El usuario por defecto es `postgres`. Si usas otro usuario, agrega también:
```
   DB_USERNAME=tu_usuario
```

Las pruebas (`mvnw test`) usan H2 en memoria automáticamente y **no** requieren PostgreSQL corriendo.

## Correr la aplicación

```
.\mvnw.cmd spring-boot:run
```

## Correr las pruebas

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


```
.\mvnw.cmd clean test
```