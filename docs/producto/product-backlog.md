# Product Backlog

**Actualizado:** planificación del Sprint 6 (26/10/2026). **Total:** 98 pts, 57 completados (58 %) al cierre del Sprint 5. Durante el sprint, cada historia cambia su estado en su propio Pull Request; los totales (encabezado, épicas y resumen) se actualizan una sola vez al cierre, para que los Pull Requests del sprint no choquen entre sí.

Estructura: Product Backlog → épicas (EP-0X) → historias de usuario (HU-X.Y) → tareas (T-X.Y.Z). Cada historia es la unidad asignable, tiene un dueño y cabe en un sprint; los puntos y la prioridad se asignan a la historia, y el peso de una épica es la suma de sus historias. Tipos: Funcional, Técnica, Investigación y Entorno. Cada historia se asocia a al menos uno de los tres requisitos del cliente ([visión y MVP](vision-y-mvp.md)): Precisión, Seguridad o Verificabilidad internacional.

Las historias que ya tienen criterios de aceptación detallados enlazan a su archivo en [`historias/`](historias/). El estado «Completo (Sn)» indica el sprint en que se terminó.


## EP-01 · Registro y validación de títulos — 37 pts (26 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| HU-1.1 | Entorno | Como equipo, queremos un proyecto base con Spring Boot, PostgreSQL y GitHub para trabajar sobre un entorno común. | Precisión | Alta | 3 | Completo (S2) |
| HU-1.2 | Funcional | Como personal autorizado, quiero iniciar sesión según mi rol para que solo yo pueda registrar o aprobar títulos. | Seguridad | Alta | 5 | Completo (S2) |
| HU-1.3 | Funcional | Como registrador, quiero registrar graduado y título con campos obligatorios validados para reducir errores antes de la emisión. | Precisión | Alta | 5 | Completo (S2) |
| HU-1.4 | Funcional | Como registrador, quiero una interfaz clara y consistente en la pantalla de registro para reducir errores al ingresar los datos. | Precisión | Media | 2 | Completo (S3) |
| HU-1.5 | Funcional | Como usuario del sistema, quiero una página de inicio de sesión propia y coherente con el resto de la aplicación, sin estilos en línea, para iniciar sesión con confianza y permitir una política de seguridad más estricta. | Seguridad | Media | 3 | Pendiente |
| HU-1.6 | Funcional | Como usuario del sistema, quiero cerrar sesión desde cualquier pantalla para que nadie más pueda usar mi cuenta en un equipo compartido. | Seguridad | Media | 1 | Completo (S4) |
| HU-1.7 | Funcional | Como usuario del sistema, quiero un encabezado común con un menú que solo muestre lo que mi rol puede usar, para navegar sin llegar a páginas prohibidas. | Seguridad | Alta | 2 | Completo (S4) |
| HU-1.8 | Funcional | Como personal autorizado, quiero buscar los títulos por nombre o documento del graduado y filtrarlos por estado, con paginación, para encontrar un registro entre miles. | Precisión | Media | 3 | Pendiente |
| HU-1.9 | Funcional | Como institución, necesito que solo el rol REGISTRADOR registre títulos, en una página propia, para que nadie apruebe un título que él mismo registró. | Precisión / Seguridad | Alta | 3 | Completo (S4) |
| HU-1.10 | Funcional | Como registrador, quiero identificar al graduado por su tipo y número de documento (cédula o pasaporte) para no confundir personas con el mismo nombre ni registrar dos veces el mismo título. | Precisión | Alta | 5 | Completo (S4) |
| HU-1.11 | Funcional | Como registrador, quiero elegir el programa de un catálogo oficial con búsqueda para que un mismo título no quede escrito de formas distintas. | Precisión | Alta | 5 | Pendiente |

## EP-02 · Aprobación y emisión de la credencial — 24 pts (15 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| HU-2.1 | Funcional | Como aprobador, quiero revisar y aprobar o rechazar un título registrado, guardando usuario, fecha y estado. | Precisión / Seguridad | Alta | 5 | Completo (S3) |
| HU-2.2 | Técnica | Como sistema, necesito asignar un identificador único a cada título aprobado para poder consultarlo después. | Verificabilidad internacional | Media | 3 | Completo (S3) |
| HU-2.3 | Investigación | Como equipo, queremos una prueba de concepto con Apache PDFBox y ZXing para conocer su uso antes de desarrollar la credencial. | Verificabilidad internacional | Alta | 2 | Completo (S3) |
| [HU-2.4](historias/HU-2.4.md) | Funcional | Como personal autorizado, quiero descargar en PDF la credencial de un título aprobado, con el nombre y el documento del graduado, para entregarla al graduado y que este la presente ante terceros. | Verificabilidad internacional | Alta | 5 | Completo (S5) |
| [HU-2.5](historias/HU-2.5.md) | Funcional | Como verificador (un empleador u otra institución), quiero que la credencial tenga un código QR que abra su verificación pública, para comprobar desde el celular que el título es auténtico sin pedirle nada a la universidad. | Verificabilidad internacional | Alta | 3 | En curso (Sprint 6) |
| HU-2.6 | Funcional | Como personal autorizado, quiero ver el detalle de un título (estado, revisión, identificador e historial) para revisarlo antes de actuar sobre él. | Precisión | Alta | 3 | Pendiente |
| HU-2.7 | Funcional | Como aprobador, quiero confirmar antes de rechazar, revocar o sustituir un título para evitar errores que no se pueden deshacer. | Precisión | Media | 1 | Pendiente |
| HU-2.8 | Funcional | Como verificador, quiero comparar el hash de integridad impreso en la credencial con el que muestra la verificación pública, para detectar si el documento fue alterado. | Precisión / Verificabilidad internacional | Alta | 2 | Pendiente (nueva) |

## EP-03 · Verificación pública y ciclo de vida del título — 14 pts (3 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| [HU-3.1](historias/HU-3.1.md) | Funcional | Como empleador, institución o el propio graduado, quiero consultar un título con su código de verificación, escribiéndolo o desde el QR de la credencial, sin iniciar sesión, para comprobar que es auténtico y válido. | Verificabilidad internacional | Alta | 5 | Completo (S6) |
| HU-3.2 | Funcional | Como aprobador, quiero revocar o sustituir un título emitido para que la verificación muestre su estado real. | Verificabilidad internacional | Media | 3 | Pendiente |
| [HU-3.3](historias/HU-3.3.md) | Técnica | Como institución, necesito que cada registro, aprobación y rechazo de un título quede en un registro de auditoría que no se pueda modificar, con quién lo hizo y cuándo, para rastrear cualquier cambio en un título. | Seguridad | Alta (antes Media) | 3 | Completo (S5) |
| [HU-3.4](historias/HU-3.4.md) | Funcional | Como aprobador, quiero consultar el registro de auditoría filtrando por fechas, acción y usuario, para rastrear quién registró, aprobó o rechazó cada título y cuándo. | Seguridad | Alta (antes Media) | 3 | Pendiente (Sprint 6) |

## EP-04 · Calidad, seguridad y operación — 23 pts (13 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| HU-4.1 | Entorno | Como equipo, queremos un pipeline en CircleCI que compile y ejecute las pruebas JUnit en cada cambio. | Precisión | Media | 3 | Completo (S1) |
| HU-4.2 | Técnica | Como equipo, queremos escanear la aplicación con Nikto en cada sprint para medir la evolución de los hallazgos. | Seguridad | Media | — | Retirada (S4): actividad fija del cierre |
| HU-4.3 | Técnica | Como institución, necesito que la aplicación envíe las cabeceras de seguridad HTTP pendientes señaladas por Nikto. | Seguridad | Media | 2 | Completo (S3) |
| HU-4.4 | Entorno | Como equipo, queremos exponer métricas con Actuator y recolectarlas en Prometheus para monitorear la aplicación. | Seguridad | Media | 3 | Completo (S4) |
| HU-4.5 | Técnica | Como institución, necesito páginas de error propias (403, 404 y 500) que no muestren detalles técnicos del servidor. | Seguridad | Media | 2 | Pendiente |
| HU-4.6 | Entorno | Como equipo, queremos un modo demostración, apagado por defecto, con usuarios de prueba y botones que llenen el acceso por rol, para presentar la aplicación sin dejar credenciales de prueba en la versión oficial. | Seguridad | Alta | 2 | Pendiente |
| [HU-4.7](historias/HU-4.7.md) | Técnica | Como institución, necesito que el servidor valide y normalice cada campo del formulario de registro y que la base de datos lo respalde con restricciones, para no guardar títulos con datos inválidos, duplicados o manipulados. | Seguridad / Precisión | Alta | 3 | Completo (S5) |
| HU-4.8 | Técnica | Como equipo, queremos pruebas de integración con MockMvc de las reglas por rol y de las cabeceras de seguridad, para detectar automáticamente si un cambio abre un acceso indebido. | Seguridad | Media | 3 | Pendiente |
| HU-4.9 | Técnica | Como institución, necesito una política de seguridad de contenido (CSP) que no permita estilos en línea, para reducir el riesgo de inyección de código. | Seguridad | Baja | 2 | Pendiente |
| [HU-4.10](historias/HU-4.10.md) | Técnica | Como institución, necesito que las métricas solo las lea un usuario técnico de Prometheus, sin acceso a las pantallas, y que HTTP Basic solo funcione en `/actuator/**`, para que una contraseña filtrada de Prometheus no permita registrar ni aprobar títulos. | Seguridad | Alta | 2 | Completo (S5) |
| [HU-4.11](historias/HU-4.11.md) | Técnica | Como usuario del sistema, quiero que al iniciar sesión la aplicación me lleve a una pantalla válida aunque el navegador haya pedido archivos automáticos, para no ver un error 404 después de entrar. | Seguridad | Alta | 1 | Completo (S6) |

**Total del Product Backlog: 98 pts (97 del Sprint 4 + 1 de HU-4.11, nueva) — completados 57 pts (58 %).**

## Resumen por épica

| Épica | Pts | Completados |
|---|---|---|
| EP-01 · Registro y validación de títulos | 37 | 26 |
| EP-02 · Aprobación y emisión de la credencial | 24 | 15 |
| EP-03 · Verificación pública y ciclo de vida del título | 14 | 3 |
| EP-04 · Calidad, seguridad y operación | 23 | 13 |
| **Total** | **98** | **57 (58 %)** |

## Cobertura de los tres requisitos del cliente

- **Precisión:** HU-1.1, HU-1.3, HU-1.4, HU-1.8, HU-1.9, HU-1.10, HU-1.11, HU-2.1, HU-2.6, HU-2.7, HU-2.8, HU-4.1 y HU-4.7.
- **Seguridad:** HU-1.2, HU-1.5, HU-1.6, HU-1.7, HU-1.9, HU-2.1, HU-3.3, HU-3.4 y HU-4.3 a HU-4.11.
- **Verificabilidad internacional:** HU-2.2, HU-2.3, HU-2.4, HU-2.5, HU-2.8, HU-3.1 y HU-3.2.

Ningún pilar queda sin al menos una historia asociada.

## Cambios recientes

- **Sprint 6 (planificación):** se corrigieron HU-2.5, HU-3.1, HU-3.4 y HU-4.11 con la Definición de Listo. HU-2.5 se dividió: se queda con el QR (3 pts) y el hash pasa a la nueva HU-2.8 (2 pts, Sprint 7), porque solo se puede comparar en la página de verificación que llega con HU-3.1; así ninguna historia del sprint espera a otra. El total sigue en 98 pts. HU-3.4 sube a prioridad Alta por ser parte del MVP.
- **Sprint 5:** se corrigieron HU-2.4, HU-3.3, HU-4.7 y HU-4.10 con la Definición de Listo (HU-3.3 sube de Media a Alta por ser parte del MVP); se agregó HU-4.11 (1 pt) a partir de la verificación de cierre.
- **Sprint 4:** se retiró HU-4.2, porque Nikto pasó a ser una actividad fija del cierre de cada sprint, y se agregó HU-4.10.

Este archivo se actualiza al cerrar cada sprint, junto con el documento del equipo.
