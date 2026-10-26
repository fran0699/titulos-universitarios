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

- **Pts** son los puntos completados en el sprint (la velocidad).
- **Nikto:** desde el Sprint 3 se usa siempre la versión 2.6.1, para que los hallazgos se puedan comparar. Las cifras del Sprint 1 y del Sprint 2 sirven solo como referencia.
- En la Sprint Review del Sprint 2 se reestructuró el backlog en épicas, historias y tareas. Los commits más antiguos usan los identificadores anteriores (PB-01 a PB-11).

**Avance del Product Backlog:** 30 de 97 pts al cierre del Sprint 3; 44 de 97 en el Sprint 4; 57 de 98 en el Sprint 5 (58 %). El total subió a 98 con HU-4.11.

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

- Subir temprano, en un PR propio, la tarea de mayor riesgo. La historia del PDF llegó completa y tarde, y acumuló conflictos con tres fusiones.
- Resolver los conflictos solo con `git merge origin/main`. Copiar archivos de main a mano dejó una versión vacía de la auditoría.
- Pedir como «necesario» todo lo que la tarea prometía. Una observación de HU-4.7 quedó como recomendación y hubo que completarla en otro PR.
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

Se reajusta en cada planificación. Quedan 41 pts.

| Sprint | Propuesta | Notas |
|---|---|---|
| 6 · **MVP** | HU-2.5 (QR y hash, 5), HU-3.1 (verificación pública, 5), HU-3.4 (consulta de auditoría, 3) y HU-4.11 (1) | 14 pts frente a una velocidad de 13; la planificación decide. Completa la cadena del MVP |
| 7 y 8 | Lo postergado: HU-1.11, HU-2.6, HU-1.5, HU-4.6, HU-1.8, HU-2.7, HU-3.2, HU-4.5, HU-4.8 y HU-4.9 | HU-4.6 (modo demostración) depende de HU-1.5 y debe estar antes de la entrega |
| 9 y 10 | Margen, ajustes pedidos en las revisiones y preparación de la entrega | Entrega del producto: primera semana de diciembre de 2026 |
