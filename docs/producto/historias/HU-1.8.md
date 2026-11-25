# HU-1.8 · Búsqueda y paginación de títulos

**Dueño: Aldo Bovell · Funcional · Precisión · Prioridad Media · 3 pts · Sprint 8**

## Redacción

> Como personal autorizado (registrador o aprobador), quiero buscar los títulos por nombre o documento del graduado, o por el nombre del título, filtrarlos por estado y verlos por páginas, para encontrar un registro entre miles sin recorrer la lista completa.

**Redacción anterior:** Como personal autorizado, quiero buscar los títulos por nombre o documento del graduado y filtrarlos por estado, con paginación, para encontrar un registro entre miles.

**Por qué cambió:** Se agrega la búsqueda por el nombre del título, que la guía de pantallas (P2) ya incluía, y se aclara quién es el personal autorizado.

**Aporte al producto:** Hoy la lista de títulos carga todos los registros de una vez. Con el volumen esperado (30 000 graduados) eso sería lento y nadie encontraría un registro a mano.

## Criterios de aceptación

1. **CA1.** La lista de títulos (P2) tiene un campo de búsqueda que encuentra los títulos cuyo nombre del graduado, número de documento o nombre del título contiene el texto escrito, sin distinguir mayúsculas.
2. **CA2.** Tiene un filtro por estado con «Todos» y cada estado posible; se puede combinar con la búsqueda.
3. **CA3.** Muestra 20 títulos por página, del registro más reciente al más antiguo, con «Anterior», «Siguiente» y «Mostrando X–Y de N títulos»; al cambiar de página se conservan la búsqueda y el filtro.
4. **CA4.** Una búsqueda de 1 carácter o de más de 100, un estado que no está en la lista o una página menor que 1 muestran el error junto al campo, sin consultar la base de datos.
5. **CA5.** Si no hay resultados, la lista muestra «No hay títulos con esa búsqueda» (o «Todavía no hay títulos registrados» si no se buscó nada).
6. **CA6.** La lista ya no carga todos los títulos: la consulta a la base de datos trae solo la página pedida.

## Reglas de negocio

- La búsqueda no distingue mayúsculas, pero sí tildes: «martinez» encuentra «MARTINEZ» y «Martinez», pero no «Martínez». Para un resultado exacto se busca por documento. Ignorar tildes queda como mejora posible (ver Riesgos).
- El documento se busca tal como se guarda: con guiones y en mayúsculas para la cédula (`8-940-1000`), sin espacios ni guiones para el pasaporte.
- El texto se busca contenido en cualquier parte del dato («940» encuentra `8-940-1000`).
- Los estados del filtro salen de la lista de estados de la aplicación, así que los que agregue [HU-3.2](HU-3.2.md) (revocado y sustituido) aparecen solos.
- Los dos roles ven todos los títulos; la búsqueda no cambia quién ve qué.

## Validaciones

| Dato | Regla |
|---|---|
| Búsqueda | Sin espacios al inicio o al final; vacía o de 2 a 100 caracteres ([guía de pantallas](../guia-de-pantallas.md#validación-de-datos-hu-47)). |
| Estado | Vacío («Todos») o un valor de la lista de estados. |
| Página | Número entero desde 1; tamaño fijo de 20, igual que la auditoría (P6). |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Exactitud de la búsqueda | Búsquedas de prueba que devuelven exactamente los títulos esperados (por nombre, documento, título, estado y combinadas) ÷ búsquedas de prueba. | JUnit con H2 | 100 % |
| Validación de entradas | Entradas inválidas (CA4) que muestran el error sin consultar la base de datos ÷ entradas inválidas probadas. | JUnit (MockMvc) | 100 % |
| Eficiencia de rendimiento | Tiempo de `/titulos` con búsqueda y filtro. | Prometheus (`http_server_requests`) | Menos de 1 s |
| Carga de la consulta | Filas que trae la consulta de la lista. | JUnit (página de resultados) | 20 como máximo |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Quien busque un apellido sin tilde no encuentra el que la tiene. | Se acepta por ahora: la búsqueda exacta es por documento. Ignorar tildes exige guardar una copia del nombre sin tildes o una extensión de PostgreSQL que la base de pruebas (H2) no tiene; queda como mejora posible en el Product Backlog. |
| Una búsqueda por texto contenido no aprovecha índices y se vuelve lenta con muchos registros. | Medir con Prometheus (meta de menos de 1 s); con 30 000 registros es manejable. Si no se cumple, se registra como deuda técnica. |
| Repetir la lógica de filtros y páginas de la auditoría con otro estilo. | Seguir el mismo diseño de HU-3.4 (servicio que valida antes de consultar, consulta paginada y la misma barra de paginación). |
| La lista de títulos (P2) también la cambia [HU-2.6](HU-2.6.md) (columna «Ver»). | Archivo compartido, no dependencia: esta historia cambia la búsqueda, la consulta y la paginación; quien fusione después trae main. |

## Dependencias

- Ninguna historia pendiente: usa la lista de títulos que ya existe.
- Comparte la plantilla de la lista con HU-2.6 (ver Riesgos).

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-1.8.1 | Consulta paginada con búsqueda y filtro por estado, y su validación (CA1, CA2, CA4 y CA6). Pruebas con H2. | Aldo Bovell | #47 |
| T-1.8.2 | Campo de búsqueda, filtro, paginación y estados vacíos en la lista (CA3 y CA5). Pruebas con MockMvc, evidencia (capturas de una búsqueda, un filtro y la segunda página) y la fila de P2 en la tabla de estado de la guía. | Aldo Bovell | #47 |

## Estado

Completo (Sprint 8). PR #47. Evidencias en `docs/evidencias/`:

- `HU-1.8-busqueda.jpg`: CA1, una búsqueda.
- `HU-1.8-filtro-pendiente.jpg`, `HU-1.8-filtro-aprobado.jpg` y `HU-1.8-filtro-rechazado.jpg`: CA2, el filtro por cada estado.
- `HU-1.8-pagina-2.jpg`: CA3, la segunda página.
- 21 pruebas nuevas. Al cierre, CircleCI corre 205 pruebas, 0 fallos.
- Verificación de cierre sobre main con 32 títulos: la lista tarda 19,8 ms de promedio y 134,3 ms de máximo (meta: menos de 1 s). Detalle en [`sprint-8-rendimiento.txt`](../../evidencias/sprint-8-rendimiento.txt).
