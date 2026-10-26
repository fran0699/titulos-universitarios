# Stack tecnológico y versiones

Qué usa el proyecto, en qué versión, dónde se define esa versión y qué tiene que instalar cada integrante.

## Resumen

| Área | Herramienta | Versión | Dónde se define |
|---|---|---|---|
| Lenguaje | Java (JDK) | 21 | `pom.xml` (`java.version`) |
| Framework | Spring Boot (arquitectura MVC) | 4.1.1 | `pom.xml` (padre `spring-boot-starter-parent`) |
| Interfaz web | Thymeleaf + CSS propio (`static/css/titulos.css`) | La de Spring Boot | `pom.xml` |
| Seguridad | Spring Security (formulario, HTTP Basic en `/actuator/**`, BCrypt, CSRF, cabeceras) | La de Spring Boot | `pom.xml` |
| Datos | Spring Data JPA / Hibernate | La de Spring Boot | `pom.xml` |
| Validación | Bean Validation (`spring-boot-starter-validation`) | La de Spring Boot | `pom.xml` |
| Base de datos | PostgreSQL + pgAdmin 4 | 17 (referencia del equipo) | Instalación local de cada integrante |
| Dependencias y compilación | Maven, con el wrapper `mvnw` | Maven 3.9.16 (wrapper 3.3.4) | `.mvn/wrapper/maven-wrapper.properties` |
| Pruebas | JUnit 5, Mockito, MockMvc, `spring-security-test` y H2 en memoria | Las de Spring Boot | `pom.xml` |
| PDF | Apache PDFBox | 3.0.8 | `pom.xml` |
| QR | ZXing | 3.5.4 (planificado para HU-2.5; aún no está en el `pom.xml`) | `docs/investigacion-pdf-qr.md` |
| Métricas | Spring Boot Actuator + Micrometer (`micrometer-registry-prometheus`) | Las de Spring Boot | `pom.xml` |
| Monitoreo | Prometheus, en Docker | 3.15.0 (imagen `prom/prometheus:v3.15.0`) | `docker-compose.prometheus.yml` |
| Contenedores | Docker Desktop con motor WSL 2 | Probado con 4.96.1 y Docker Compose v5.6.0 | Instalación local |
| Seguridad (escaneo) | Nikto, en Ubuntu (WSL) | **2.6.1** exacta (tag de Git) | Acuerdo del equipo (Sprint 4) |
| CI/CD | CircleCI | Imagen `cimg/openjdk:21.0`; ejecuta `./mvnw clean test` | `.circleci/config.yml` |
| Control de versiones | Git y GitHub | — | — |
| IDE | IntelliJ IDEA | — | — |

**No se usan** Bootstrap ni JavaScript propio por ahora: la interfaz usa solo la hoja de estilos del proyecto, y la política de seguridad de contenido (CSP) permite scripts solo de la propia aplicación.

## Qué instala cada integrante

**Para desarrollar**

- JDK 21. Recomendado: Eclipse Temurin 21, con la variable `JAVA_HOME` configurada.
- Git, con tu nombre y correo configurados (`git config --global user.name` y `user.email`).
- IntelliJ IDEA.
- PostgreSQL 17 con pgAdmin 4, y una base de datos vacía llamada `titulos_db`.

**Para el monitoreo**

- Docker Desktop con el motor WSL 2. Prometheus no se instala: lo levanta `monitoring/prometheus/Start-Prometheus.ps1` en un contenedor ([docs/prometheus.md](../prometheus.md)).

**Para el cierre del sprint (escaneo de seguridad)**

- WSL con Ubuntu, Perl y Nikto 2.6.1:

```bash
sudo apt update && sudo apt install -y perl git
git clone https://github.com/sullo/nikto.git ~/nikto
cd ~/nikto && git checkout 2.6.1
perl program/nikto.pl -Version        # debe decir "Nikto 2.6.1"
```

Desde Ubuntu, la aplicación que corre en Windows no responde en `localhost`. Se usa la IP de Windows vista desde WSL:

```bash
ip route show default                 # "default via 172.x.x.1 ..." → esa es la IP
cd ~/nikto/program && perl nikto.pl -h http://172.x.x.1:8080 -o ~/nikto-sprintN.txt -Format txt
```

La IP puede cambiar al reiniciar WSL. Al dejar la copia en el tag 2.6.1, Nikto avisa que hay una versión más nueva: es esperado, porque el equipo compara siempre con la misma versión.

**Lo que no se instala:** Maven (lo descarga `mvnw` la primera vez), H2 (es una dependencia de las pruebas) y Prometheus (corre en Docker).

## Comprobación rápida de un equipo

En PowerShell:

```powershell
java -version                 # 21
git --version
.\mvnw.cmd -v                 # Maven 3.9.16 y Java 21
docker --version              # solo si vas a usar Prometheus
wsl -l -v                     # Ubuntu, VERSION 2 (solo para Nikto)
Get-Service *postgres*        # el servicio de PostgreSQL en marcha
```

Y en el proyecto: `.\mvnw.cmd clean test` debe terminar en `BUILD SUCCESS`, con todas las pruebas en verde.

## Cómo cambiar una versión

1. Cambiarla en su archivo (tabla de arriba) en una rama propia.
2. Correr `.\mvnw.cmd clean test` y verificar la aplicación.
3. Actualizar este documento en el mismo Pull Request.
4. Si cambia algo que cada integrante instala, avisarlo en la sección «Acción requerida para el equipo» del Pull Request.
