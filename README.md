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

```
.\mvnw.cmd clean test
```