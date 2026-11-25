# Historial de sprints

Un resumen por sprint: qué se completó, cómo se midió y qué se aprendió. Se agrega una fila y una sección al cerrar cada sprint.

## Resumen

| Sprint | Cierre | Historias completadas | Pts | Pruebas | Hallazgos Nikto | PR fusionados |
|---|---|---|---|---|---|---|
| 1 | — | HU-4.1 y HU-4.2 | 5 | 4 | 15 | — |
| 2 | — | HU-1.1, HU-1.2 y HU-1.3 | 13 | 6 | 5 | — |
| 3 | — | HU-2.1, HU-2.2, HU-2.3, HU-4.3, HU-1.4 y HU-4.2 (agregada) | 16 | 15 | 2 | — |
| 4 | 18/10/2026 | HU-1.9, HU-1.7, HU-1.6, HU-1.10 y HU-4.4 | 14 | 50 | 2 | 8 (#15 a #22) |
| 5 | 25/10/2026 | HU-4.10, HU-4.7, HU-3.3 y HU-2.4 | 13 | 95 | 2 | 6 (#23 a #28) |
| 6 | 29/10/2026 | HU-2.5, HU-3.1, HU-3.4 y HU-4.11 | 12 | 155 | 2 | 5 (#31 a #35) |
| 7 | 16/11/2026 | — (transferencia al equipo nuevo) | 0 | 156 | — | 3 (#39 a #41) |
| 8 | 24/11/2026 | HU-2.7, HU-1.8, HU-1.5 y HU-4.5 | 9 | 205 | 2 | 8 (#42 a #49) |

- **Pts** son los puntos completados en el sprint (la velocidad). Suman 82, cuatro más que los 78 completados del [Product Backlog](../producto/product-backlog.md): HU-4.2 (Nikto, 2 pts) se contó en el Sprint 1 y en el Sprint 3, y en el Sprint 4 se retiró del backlog al pasar a ser una actividad fija del cierre.
- **Nikto:** desde el Sprint 3 se usa siempre la versión 2.6.1, para que los hallazgos se puedan comparar. Las cifras del Sprint 1 y del Sprint 2 sirven solo como referencia. En el Sprint 7 no se escaneó: el único cambio de código fue el del PR #40. En el Sprint 8 se escaneó de nuevo, con los mismos 2 hallazgos del Sprint 6.
- En la Sprint Review del Sprint 2 se reestructuró el backlog en épicas, historias y tareas. Los commits más antiguos usan los identificadores anteriores (PB-01 a PB-11).

**Avance del Product Backlog:** 30 de 97 pts al cierre del Sprint 3; 44 de 97 en el Sprint 4; 57 de 98 en el Sprint 5 (58 %); 69 de 98 en el Sprint 6 (70 %); 69 de 102 en el Sprint 7 (68 %); 78 de 102 en el Sprint 8 (76 %). El total subió a 98 con HU-4.11 y a 102 con la reestimación del refinamiento del Sprint 7.

## Sprint 8 · 17 al 24 de noviembre de 2026

**Objetivo:** primera iteración completa del equipo nuevo: que el aprobador confirme antes de rechazar un título, que la lista de títulos se pueda buscar y paginar, que el inicio de sesión tenga página propia y que los errores no muestren detalles técnicos.

**Resultado:** objetivo cumplido, con 9 de 9 pts. Las cuatro historias del plan entraron a main, cada una con sus pruebas y su evidencia.

| Responsable | Historia | PR |
|---|---|---|
| Marleni Barria | HU-2.7 · Página de confirmación antes de rechazar (1) | #42 |
| Aldo Bovell | HU-1.8 · Búsqueda y paginación de títulos (3) | #47 |
| Maria Gonzalez | HU-1.5 · Página de inicio de sesión propia (3) | #45 y #48 |
| Carolina Rodriguez | HU-4.5 · Páginas de error propias (2) | #44 |

Además entraron los PR de documentación #43 (documentación del plan) y #46 (cierre del Sprint 7), y la corrección #49.

**Mediciones:**

- 205 pruebas en verde (156 heredadas y 49 nuevas: 7 de HU-4.5, 8 de HU-2.7, 21 de HU-1.8 y 13 de HU-1.5) y 27 de 27 criterios de aceptación.
- Verificación de principio a fin sobre `2e48c04` con una base de datos nueva y la consola del navegador sin errores.
- Nikto 2.6.1: 2 hallazgos, los mismos del Sprint 6 (falta HSTS, que solo se envía por HTTPS, y una IP en la cabecera `Location` que repite la que pone el propio Nikto). El login propio, las páginas de error y la búsqueda no agregaron hallazgos ([informe](../evidencias/sprint-8-nikto.txt)).
- Tiempos con Prometheus ([detalle](../evidencias/sprint-8-rendimiento.txt)): la lista de títulos con búsqueda y filtro, 19,8 ms de promedio y 134,3 ms de máximo con 32 títulos (meta: menos de 1 s).
- Código: 8 443 líneas (6 557 de código efectivo) entre producción y pruebas, 968 más de producción y 993 más de pruebas que al cierre del Sprint 7. Las pruebas pasaron de 0,72 a 0,78 líneas por cada línea de producción.
- Defectos: 1 encontrado en la verificación de cierre y corregido en main (PR #49): en `titulos.css` quedó sin cerrar un bloque `@media` al traer main a la rama de HU-1.5, y los estilos de HU-2.7 y de las páginas de error no se aplicaban. Edad: el mismo día; abiertos: 0.

**Pendientes técnicos que dejó el sprint:**

- La página de error de un aprobador usa el encabezado común, que consulta la base de datos para el contador de pendientes. Si un error 500 viniera de una caída de la base, la página de error también podría fallar.

**Lecciones**

- Las pruebas con MockMvc no revisan el CSS: un bloque sin cerrar dejó sin estilos dos historias mientras todas las pruebas seguían en verde. Lo detectó la verificación de cierre en el navegador.

## Sprint 7 · 30 de octubre al 16 de noviembre de 2026

**Objetivo:** transferir el proyecto al equipo nuevo y dejar planificado el Sprint 8 sin perder calidad, trazabilidad ni visión de negocio. Sprint de transición: sin historias nuevas.

**Resultado:** objetivo cumplido. El equipo nuevo recibió el proyecto con las 11 historias pendientes refinadas (33 pts), aceptó el plan del Sprint 8 y cada integrante eligió su historia ([plan tentativo](#próximos-sprints-plan-tentativo)).

| PR | Qué entró |
|---|---|
| #39 | Documentación corregida antes de la llegada del equipo nuevo (16 archivos). |
| #40 | T-3.1.4: la verificación pública muestra solo los últimos 2 caracteres del documento ([HU-3.1](../producto/historias/HU-3.1.md)). |
| #41 | Las 11 historias pendientes refinadas, el Product Backlog en 102 pts, la guía de pantallas, el equipo nuevo, las decisiones de alcance y el plan del Sprint 8. |

**Mediciones:**

- 156 pruebas en verde (155 heredadas y 1 nueva, del PR #40).
- 11 de 11 historias pendientes refinadas con la Definición de Listo: 75 criterios de aceptación, 40 métricas QA y 35 riesgos con su mitigación. Cambió la redacción de 7 y se reestimaron 3: el total pasa de 98 a 102 pts.
- 5 inconsistencias encontradas al refinar y corregidas en las historias antes de llegar al código ([decisiones de alcance](../producto/vision-y-mvp.md#decisiones-de-alcance)).
- Plan del Sprint 8: 4 historias independientes (9 pts), una por integrante.

**Lecciones**

- Refinar todas las historias pendientes antes del traspaso: al revisarlas contra el código aparecieron 5 inconsistencias que habrían salido a mitad de un sprint.
- Para quien llega, lo primero es distinguir lo terminado de lo pendiente: está en la tabla «Estado» del README y en el [Product Backlog](../producto/product-backlog.md), y conviene señalarlo desde el primer día.
- La transferencia necesita un tiempo acorde con la complejidad del proyecto y con las horas disponibles del equipo, y sesiones de conocimiento entre equipos, no solo entre Product Owners.
- Presentar el plan como propuesta y documentarlo en GitHub con el acuerdo del equipo: así cada integrante eligió su historia.
- Una huella impresa en el mismo PDF no prueba que el PDF no fue editado, porque se puede copiar o cambiar junto con el documento: [HU-2.8](../producto/historias/HU-2.8.md) guarda la huella al aprobar y la verificación avisa si los datos cambiaron.

## Sprint 6 · 26 al 29 de octubre de 2026

**Objetivo:** completar el MVP. Que la credencial tenga un QR que abra su verificación pública; que cualquiera compruebe un título sin iniciar sesión, en español e inglés; que el aprobador consulte la auditoría con filtros; y que iniciar sesión con la consola del navegador abierta no muestre un 404. Primer sprint con la regla de independencia: cada responsable es dueño de historias completas y nadie espera a otro.

**Resultado:** objetivo cumplido, con 12 de 12 pts. El MVP funciona de punta a punta: registrar, aprobar, emitir la credencial con QR, verificarla desde un teléfono y consultar quién hizo qué.

| Responsable | Historia | PR |
|---|---|---|
| Edinson Ramirez | HU-2.5 · QR en la credencial (3) | #33 |
| Henry Birminghan | HU-3.1 · Verificación pública (5) | #32 |
| Anel Gonzalez | HU-3.4 · Consulta de la auditoría (3) | #35 (completado por Francisco Lata) |
| Francisco Lata | HU-4.11 · Inicio de sesión sin error 404 (1) | #34 |

La planificación entró en el PR de documentación #31.

**Mediciones:**

- 155 pruebas en verde (95 heredadas y 60 nuevas) y 28 de 28 criterios de aceptación.
- Verificación de principio a fin sobre `7b213ac` con una base de datos nueva, sin fallos y con la consola del navegador sin errores. El QR se leyó con un teléfono conectado a la misma red.
- Nikto 2.6.1 sobre `7b213ac`: 8 350 peticiones, 0 errores y 2 hallazgos, los mismos del Sprint 5. Las páginas públicas nuevas no agregaron hallazgos ([informe](../evidencias/sprint-6-nikto.txt)).
- Matriz de acceso: 12 de 12.
- Tiempos con Prometheus ([detalle](../evidencias/sprint-6-rendimiento.txt)): verificación 9,8 ms de promedio y auditoría 23,1 ms (meta: menos de 1 s); credencial 70,5 ms de promedio y 313,7 ms la primera después de arrancar (meta: menos de 2 s).
- Defectos: 3 corregidos en main (el nombre de tres líneas en la credencial y los dos 404 al iniciar sesión; edad promedio de 2,7 días) y 0 abiertos.

**Lecciones**

- La regla de independencia funcionó: las cuatro historias avanzaron en paralelo y se fusionaron en distinto orden, trayendo main con `git merge origin/main`, sin conflictos de código.
- Cada PR de historia cambia solo su fila del Product Backlog; los totales se actualizan al cierre ([por qué](../proceso/como-trabajamos.md#qué-documentación-actualiza-cada-pull-request)).
- Revisar qué métodos trae una interfaz heredada: `JpaSpecificationExecutor` (Spring Data JPA 4.0) incluye `delete` y `update`, lo que rompía la auditoría de solo lectura. Se reemplazó por una consulta con `@Query`, y una prueba comprueba que el repositorio no tenga métodos para borrar ni editar.
- Escribir las pruebas antes de corregir: en HU-4.11 primero fallaron 5 pruebas, lo que confirmó la causa del 404.
- Las pruebas van en el mismo PR desde su primera versión: un PR sin pruebas necesita más rondas de revisión y pone en riesgo el cierre del sprint.
- Seguir la convención `T-X.Y.Z: …` también en los commits de corrección: 4 de 21 no la siguieron.
- Prometheus necesita Docker Desktop abierto: comprobar el target en UP antes de medir, y medir antes de reiniciar la aplicación.

**Quien se incorpora al proyecto** empieza por [Si te incorporas al proyecto](../../README.md#si-te-incorporas-al-proyecto).

## Sprint 5 · 20 al 25 de octubre de 2026

**Objetivo:** proteger y completar la cadena del MVP. Que las métricas solo las lea un usuario técnico de Prometheus; que el servidor valide cada campo del registro, con restricciones en la base de datos; que cada registro, aprobación y rechazo quede en una auditoría que no se puede modificar; y que el personal descargue en PDF la credencial de un título aprobado.

**Resultado:** objetivo cumplido, con 13 de 13 pts. La cadena del MVP funciona desde el registro hasta la credencial, y cada acción queda registrada con quién la hizo y cuándo.

| Responsable | Historia | PR |
|---|---|---|
| Anel Gonzalez | HU-4.7 · Validación en el servidor (3) | #23 · complemento de Henry en el #28 |
| Henry Birminghan | HU-3.3 · Registro de auditoría (3) | #25 y #27 |
| Francisco Lata | HU-4.10 · Usuario técnico de Prometheus (2) | #24 |
| Edinson Ramirez | HU-2.4 · Credencial PDF (5) | #26 |

**Mediciones:** 95 pruebas en verde; Nikto 2.6.1 sobre `2330af1` con 2 hallazgos, los mismos del Sprint 4; matriz de acceso 12 de 12; 4 de 4 acciones con su evento de auditoría; credencial PDF en 20 ms de promedio (meta: menos de 2 s).

**Lecciones**

- Subir temprano, en un PR propio, la tarea de mayor riesgo técnico. Si llega completa al final del sprint, acumula conflictos con las demás fusiones.
- Resolver los conflictos solo con `git merge origin/main`. Copiar archivos de main a mano puede borrar cambios que ya estaban fusionados.
- Pedir como «necesario» todo lo que la tarea prometía. Una observación que formaba parte de la tarea quedó como recomendación y hubo que completarla en otro PR.
- Si un revisor no responde, avisar en el grupo y pasar la revisión al Product Owner.
- Corregir las historias antes de planificar. Los criterios de aceptación con sí o no sirvieron para revisar cada PR.

**Nueva historia:** HU-4.11 (1 pt), por el 404 al iniciar sesión con F12 abierto. Ver [HU-4.11](../producto/historias/HU-4.11.md).

## Sprint 4 · cerrado el 18 de octubre de 2026

**Objetivo:** separar a quien registra de quien aprueba, con un encabezado común y un menú por rol, y un botón para cerrar sesión. Identificar a cada graduado por su tipo y número de documento. Completar la integración obligatoria con Prometheus.

**Resultado:** objetivo cumplido, con 14 de 14 pts y 12 de 12 tareas. Nikto 2.6.1 sobre `f7ae84c`, con 2 hallazgos, los mismos del Sprint 3.

**Lecciones**

- Traer main a la rama (`git fetch` y `git merge origin/main`) y correr las pruebas antes de pedir revisión. Una rama trabajada sobre un main antiguo quitó cambios ya fusionados.
- Al resolver un conflicto, comparar el resultado con main antes de subirlo.
- Ejecutar Nikto siempre con la versión 2.6.1 y anotar el commit.
- Usar `HU-`/`T-` en commits y evidencias.

## Próximos sprints (plan tentativo)

Se reajusta en cada planificación. Quedan 29 pts (el total es 107: 102 tras la reestimación del refinamiento del Sprint 7 más los 5 pts de las 2 innovaciones). Todas las historias pendientes están refinadas en [`historias/`](../producto/historias/README.md). El reparto sigue la regla de independencia ([cómo trabajamos](../proceso/como-trabajamos.md)): nadie espera a que otro termine.

| Sprint | Propuesta | Notas |
|---|---|---|
| 9 y 10 | Las otras 7 historias (24 pts): HU-2.8, HU-2.6, HU-3.2, HU-1.11, HU-4.6, HU-4.9 y HU-4.8, y las 2 innovaciones (5 pts): HU-3.5 y HU-4.12; ajustes pedidos en las revisiones y preparación de la entrega | Los planifica el Product Owner con el equipo. Dependencias: HU-3.2 necesita HU-2.6 (HU-2.7 ya está completa); HU-2.8 y HU-3.2 cambian los mismos estados de la verificación. HU-4.6 (modo demostración) debe estar antes de la entrega del producto (primera semana de diciembre de 2026); la parte visual ya está en el inicio de sesión (HU-1.5) |
