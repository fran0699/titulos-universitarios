# Product Backlog

**Actualizado:** planificación del Sprint 7 (16/11/2026). **Total:** 102 pts, 69 completados (68 %). Durante el sprint, cada historia cambia su estado en su propio Pull Request; los totales (encabezado, épicas y resumen) se actualizan una sola vez al cierre, para que los Pull Requests del sprint no choquen entre sí ([cómo trabajamos](../proceso/como-trabajamos.md#qué-documentación-actualiza-cada-pull-request)).

Estructura: Product Backlog → épicas (EP-0X) → historias de usuario (HU-X.Y) → tareas (T-X.Y.Z). Cada historia es la unidad asignable, tiene un dueño y cabe en un sprint; los puntos y la prioridad se asignan a la historia, y el peso de una épica es la suma de sus historias. Tipos: Funcional, Técnica, Investigación y Entorno. Cada historia se asocia a al menos uno de los tres requisitos del cliente ([visión y MVP](vision-y-mvp.md)): Precisión, Seguridad o Verificabilidad internacional.

Las historias que ya tienen criterios de aceptación detallados enlazan a su archivo en [`historias/`](historias/). El estado «Completo (Sn)» indica el sprint en que se terminó.


## EP-01 · Registro y validación de títulos — 37 pts (26 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| HU-1.1 | Entorno | Como equipo, queremos un proyecto base con Spring Boot, PostgreSQL y GitHub para trabajar sobre un entorno común. | Precisión | Alta | 3 | Completo (S2) |
| HU-1.2 | Funcional | Como personal autorizado, quiero iniciar sesión según mi rol para que solo yo pueda registrar o aprobar títulos. | Seguridad | Alta | 5 | Completo (S2) |
| HU-1.3 | Funcional | Como registrador, quiero registrar graduado y título con campos obligatorios validados para reducir errores antes de la emisión. | Precisión | Alta | 5 | Completo (S2) |
| HU-1.4 | Funcional | Como registrador, quiero una interfaz clara y consistente en la pantalla de registro para reducir errores al ingresar los datos. | Precisión | Media | 2 | Completo (S3) |
| [HU-1.5](historias/HU-1.5.md) | Funcional | Como usuario del sistema, quiero una página de inicio de sesión propia y coherente con el resto de la aplicación, sin estilos en línea, para iniciar sesión con confianza y permitir una política de seguridad más estricta. | Seguridad | Media | 3 | Completo (S7) |
| HU-1.6 | Funcional | Como usuario del sistema, quiero cerrar sesión desde cualquier pantalla para que nadie más pueda usar mi cuenta en un equipo compartido. | Seguridad | Media | 1 | Completo (S4) |
| HU-1.7 | Funcional | Como usuario del sistema, quiero un encabezado común con un menú que solo muestre lo que mi rol puede usar, para navegar sin llegar a páginas prohibidas. | Seguridad | Alta | 2 | Completo (S4) |
| [HU-1.8](historias/HU-1.8.md) | Funcional | Como personal autorizado (registrador o aprobador), quiero buscar los títulos por nombre o documento del graduado, o por el nombre del título, filtrarlos por estado y verlos por páginas, para encontrar un registro entre miles sin recorrer la lista completa. | Precisión | Media | 3 | Pendiente |
| HU-1.9 | Funcional | Como institución, necesito que solo el rol REGISTRADOR registre títulos, en una página propia, para que nadie apruebe un título que él mismo registró. | Precisión / Seguridad | Alta | 3 | Completo (S4) |
| HU-1.10 | Funcional | Como registrador, quiero identificar al graduado por su tipo y número de documento (cédula o pasaporte) para no confundir personas con el mismo nombre ni registrar dos veces el mismo título. | Precisión | Alta | 5 | Completo (S4) |
| [HU-1.11](historias/HU-1.11.md) | Funcional | Como registrador, quiero elegir el programa de un catálogo oficial, con sugerencias mientras escribo, para que un mismo título no quede escrito de formas distintas. | Precisión | Alta | 5 | Pendiente |

## EP-02 · Aprobación y emisión de la credencial — 25 pts (18 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| HU-2.1 | Funcional | Como aprobador, quiero revisar y aprobar o rechazar un título registrado, guardando usuario, fecha y estado. | Precisión / Seguridad | Alta | 5 | Completo (S3) |
| HU-2.2 | Técnica | Como sistema, necesito asignar un identificador único a cada título aprobado para poder consultarlo después. | Verificabilidad internacional | Media | 3 | Completo (S3) |
| HU-2.3 | Investigación | Como equipo, queremos una prueba de concepto con Apache PDFBox y ZXing para conocer su uso antes de desarrollar la credencial. | Verificabilidad internacional | Alta | 2 | Completo (S3) |
| [HU-2.4](historias/HU-2.4.md) | Funcional | Como personal autorizado, quiero descargar en PDF la credencial de un título aprobado, con el nombre y el documento del graduado, para entregarla al graduado y que este la presente ante terceros. | Verificabilidad internacional | Alta | 5 | Completo (S5) |
| [HU-2.5](historias/HU-2.5.md) | Funcional | Como verificador (un empleador u otra institución), quiero que la credencial tenga un código QR que abra su verificación pública, para comprobar desde el celular que el título es auténtico sin pedirle nada a la universidad. | Verificabilidad internacional | Alta | 3 | Completo (S6) |
| [HU-2.6](historias/HU-2.6.md) | Funcional | Como personal autorizado (registrador o aprobador), quiero ver en una sola pantalla todos los datos de un título, su revisión, su identificador y su historial, para revisarlo antes de actuar sobre él. | Precisión | Alta | 3 | Pendiente |
| [HU-2.7](historias/HU-2.7.md) | Funcional | Como aprobador, quiero confirmar en una página aparte antes de rechazar un título, para no rechazar uno por error, porque el rechazo no se puede deshacer. | Precisión | Media | 1 | Pendiente |
| [HU-2.8](historias/HU-2.8.md) | Funcional | Como verificador (un empleador u otra institución), quiero que la credencial y la verificación pública muestren la misma huella de integridad, y que la verificación me avise si los datos del título cambiaron después de emitir la credencial, para confiar en que lo que tengo en la mano corresponde al registro oficial. | Precisión / Verificabilidad internacional | Alta | 3 | Pendiente |

## EP-03 · Verificación pública y ciclo de vida del título — 16 pts (11 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| [HU-3.1](historias/HU-3.1.md) | Funcional | Como empleador, institución o el propio graduado, quiero consultar un título con su código de verificación, escribiéndolo o desde el QR de la credencial, sin iniciar sesión, para comprobar que es auténtico y válido. | Verificabilidad internacional | Alta | 5 | Completo (S6) |
| [HU-3.2](historias/HU-3.2.md) | Funcional | Como aprobador, quiero revocar un título emitido, o marcarlo como sustituido por otro corregido, indicando el motivo, para que la verificación pública muestre su estado real. | Verificabilidad internacional | Media | 5 | Pendiente |
| [HU-3.3](historias/HU-3.3.md) | Técnica | Como institución, necesito que cada registro, aprobación y rechazo de un título quede en un registro de auditoría que no se pueda modificar, con quién lo hizo y cuándo, para rastrear cualquier cambio en un título. | Seguridad | Alta (antes Media) | 3 | Completo (S5) |
| [HU-3.4](historias/HU-3.4.md) | Funcional | Como aprobador, quiero consultar el registro de auditoría filtrando por fechas, acción y usuario, para rastrear quién registró, aprobó o rechazó cada título y cuándo. | Seguridad | Alta (antes Media) | 3 | Completo (S6) |

## EP-04 · Calidad, seguridad y operación — 24 pts (14 completados)

| ID | Tipo | Historia de usuario | Pilar | Prioridad | Pts | Estado |
|---|---|---|---|---|---|---|
| HU-4.1 | Entorno | Como equipo, queremos un pipeline en CircleCI que compile y ejecute las pruebas JUnit en cada cambio. | Precisión | Media | 3 | Completo (S1) |
| HU-4.2 | Técnica | Como equipo, queremos escanear la aplicación con Nikto en cada sprint para medir la evolución de los hallazgos. | Seguridad | Media | — | Retirada (S4): actividad fija del cierre |
| HU-4.3 | Técnica | Como institución, necesito que la aplicación envíe las cabeceras de seguridad HTTP pendientes señaladas por Nikto. | Seguridad | Media | 2 | Completo (S3) |
| HU-4.4 | Entorno | Como equipo, queremos exponer métricas con Actuator y recolectarlas en Prometheus para monitorear la aplicación. | Seguridad | Media | 3 | Completo (S4) |
| [HU-4.5](historias/HU-4.5.md) | Técnica | Como institución, necesito páginas de error propias (403, 404 y 500) que no muestren detalles técnicos del servidor. | Seguridad | Media | 2 | Pendiente |
| [HU-4.6](historias/HU-4.6.md) | Entorno | Como equipo, queremos un modo demostración, apagado por defecto, con usuarios de prueba y botones que llenen el acceso de cada rol, y que fuera de ese modo los usuarios iniciales se creen con datos de variables de entorno, para presentar la aplicación con rapidez sin dejar credenciales de prueba en la versión oficial. | Seguridad | Alta | 3 | Pendiente |
| [HU-4.7](historias/HU-4.7.md) | Técnica | Como institución, necesito que el servidor valide y normalice cada campo del formulario de registro y que la base de datos lo respalde con restricciones, para no guardar títulos con datos inválidos, duplicados o manipulados. | Seguridad / Precisión | Alta | 3 | Completo (S5) |
| [HU-4.8](historias/HU-4.8.md) | Técnica | Como equipo, queremos pruebas de integración con MockMvc de las reglas por rol y de las cabeceras de seguridad, para detectar automáticamente si un cambio abre un acceso indebido. | Seguridad | Media | 3 | Pendiente |
| [HU-4.9](historias/HU-4.9.md) | Técnica | Como institución, necesito una política de seguridad de contenido (CSP) que no permita estilos en línea, para reducir el riesgo de inyección de código. | Seguridad | Baja | 2 | Pendiente |
| [HU-4.10](historias/HU-4.10.md) | Técnica | Como institución, necesito que las métricas solo las lea un usuario técnico de Prometheus, sin acceso a las pantallas, y que HTTP Basic solo funcione en `/actuator/**`, para que una contraseña filtrada de Prometheus no permita registrar ni aprobar títulos. | Seguridad | Alta | 2 | Completo (S5) |
| [HU-4.11](historias/HU-4.11.md) | Técnica | Como usuario del sistema, quiero que al iniciar sesión la aplicación me lleve a una pantalla válida aunque el navegador haya pedido archivos automáticos, para no ver un error 404 después de entrar. | Seguridad | Alta | 1 | Completo (S6) |

**Total del Product Backlog: 102 pts (98 del Sprint 6 + 4 de la reestimación del Sprint 7) — completados 69 pts (68 %).**

## Resumen por épica

| Épica | Pts | Completados |
|---|---|---|
| EP-01 · Registro y validación de títulos | 37 | 26 |
| EP-02 · Aprobación y emisión de la credencial | 25 | 18 |
| EP-03 · Verificación pública y ciclo de vida del título | 16 | 11 |
| EP-04 · Calidad, seguridad y operación | 24 | 14 |
| **Total** | **102** | **69 (68 %)** |

## Cobertura de los tres requisitos del cliente

- **Precisión:** HU-1.1, HU-1.3, HU-1.4, HU-1.8, HU-1.9, HU-1.10, HU-1.11, HU-2.1, HU-2.6, HU-2.7, HU-2.8, HU-4.1 y HU-4.7.
- **Seguridad:** HU-1.2, HU-1.5, HU-1.6, HU-1.7, HU-1.9, HU-2.1, HU-3.3, HU-3.4 y HU-4.3 a HU-4.11.
- **Verificabilidad internacional:** HU-2.2, HU-2.3, HU-2.4, HU-2.5, HU-2.8, HU-3.1 y HU-3.2.

Ningún pilar queda sin al menos una historia asociada.

## Cambios recientes

- **Sprint 7 (planificación):** se refinaron las 11 historias pendientes con la Definición de Listo; todas tienen su archivo en [`historias/`](historias/README.md). Cambiaron la redacción de HU-1.8, HU-1.11, HU-2.6, HU-2.7, HU-2.8, HU-3.2 y HU-4.6. Con los criterios a la vista se reestimaron HU-2.8 (de 2 a 3 pts: guarda la huella al aprobar y la verificación avisa si los datos cambiaron), HU-3.2 (de 3 a 5: dos acciones, estados nuevos, regla de duplicados y dos estados en la verificación) y HU-4.6 (de 2 a 3: usuarios iniciales por variables de entorno). El total sube de 98 a 102 pts. Las decisiones de alcance están en [visión y MVP](vision-y-mvp.md#decisiones-de-alcance).
- **Sprint 6 (cierre):** HU-2.5, HU-3.1, HU-3.4 y HU-4.11 completas (12 pts); el MVP quedó completo. HU-4.11 sumó un criterio (CA6: el ícono del sitio no da 404) sin cambiar su punto.
- **Sprint 6 (planificación):** se corrigieron HU-2.5, HU-3.1, HU-3.4 y HU-4.11 con la Definición de Listo. HU-2.5 se dividió: se queda con el QR (3 pts) y el hash pasa a la nueva HU-2.8 (2 pts, Sprint 7), porque solo se puede comparar en la página de verificación que llega con HU-3.1; así ninguna historia del sprint espera a otra. El total sigue en 98 pts. HU-3.4 sube a prioridad Alta por ser parte del MVP.
- **Sprint 5:** se corrigieron HU-2.4, HU-3.3, HU-4.7 y HU-4.10 con la Definición de Listo (HU-3.3 sube de Media a Alta por ser parte del MVP); se agregó HU-4.11 (1 pt) a partir de la verificación de cierre.
- **Sprint 4:** se retiró HU-4.2, porque Nikto pasó a ser una actividad fija del cierre de cada sprint, y se agregó HU-4.10.

Este archivo se actualiza al cerrar cada sprint, junto con el documento del equipo.
