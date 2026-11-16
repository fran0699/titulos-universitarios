# HU-2.8 · Huella de integridad

**Dueño: por asignar · Funcional · Precisión / Verificabilidad internacional · Prioridad Alta · 3 pts (antes 2) · Sprint por planificar**

## Redacción

> Como verificador (un empleador u otra institución), quiero que la credencial y la verificación pública muestren la misma huella de integridad, y que la verificación me avise si los datos del título cambiaron después de emitir la credencial, para confiar en que lo que tengo en la mano corresponde al registro oficial.

**Redacción anterior:** Como verificador, quiero comparar el hash de integridad impreso en la credencial con el que muestra la verificación pública, para detectar si el documento fue alterado.

**Por qué cambió:** Un hash impreso no detecta que alguien editó el PDF: basta con dejar copiado el hash original. Un PDF editado se detecta comparando sus datos con la verificación pública, y eso ya lo hace [HU-3.1](HU-3.1.md). Lo que la huella sí detecta es que el registro oficial cambió después de la emisión, incluso si el cambio se hizo por fuera de la aplicación (por ejemplo, directamente en la base de datos), donde el registro de auditoría no llega.

**Aporte al producto:** Completa la verificabilidad de la credencial, pendiente desde la división de [HU-2.5](HU-2.5.md) en el Sprint 6.

## Qué es la huella

Una huella SHA-256 es un código de 64 caracteres que se calcula a partir de un texto. El mismo texto da siempre la misma huella; si cambia una sola letra, la huella cambia por completo, y no se puede volver de la huella al texto. Ejemplo: `Ana Pérez` → `859e0b14…`; `Ana Perez` → `a4050642…`.

## Criterios de aceptación

1. **CA1.** Al aprobar un título se calcula y se guarda su huella a partir de sus cinco datos de emisión: identificador, tipo y número de documento, nombre del graduado, título y fecha de emisión.
2. **CA2.** La credencial imprime la huella en letra pequeña, con la etiqueta «Huella de integridad (SHA-256)» y en 8 grupos de 8 caracteres separados por un espacio.
3. **CA3.** La verificación pública de un título sin cambios muestra la misma huella, con el mismo formato, y la etiqueta en español e inglés («Huella de integridad · Integrity fingerprint»).
4. **CA4.** Si la huella recalculada con los datos actuales no coincide con la guardada, la verificación no muestra «Válido»: muestra el estado «No coincide con la emisión · Does not match the issued record», con icono, el código y la fecha de la consulta, sin los datos del título, y pide comunicarse con la institución.
5. **CA5.** Calcular dos veces la huella del mismo título da el mismo resultado, y cambiar una sola letra en cualquiera de los cinco datos la cambia.
6. **CA6.** Los títulos aprobados antes de esta historia reciben su huella al iniciar la aplicación, sin pasos manuales en la base de datos.
7. **CA7.** Los elementos de la credencial (D1) y de la verificación (P8) que ya existían se mantienen y sus pruebas siguen en verde.

## Reglas de negocio

- Solo un título APROBADO tiene huella.
- La huella se fija al aprobar y no se recalcula para guardarla de nuevo: si el registro cambia después, la comparación de CA4 lo delata.
- Texto del que se calcula, un dato por línea, en este orden: `identificador=…`, `tipoDocumento=CEDULA` o `PASAPORTE`, `numeroDocumento=…`, `graduado=…`, `titulo=…` y `fechaEmision=AAAA-MM-DD`. Se codifica en UTF-8 y en forma Unicode NFC, para que una letra con tilde escrita de dos maneras dé la misma huella.
- El nombre y el título son los mismos que imprime la credencial (`nombreGraduado` y `nombreTitulo` del título); la fecha de emisión es la de aprobación.
- La huella se muestra en minúsculas y en 8 grupos de 8 caracteres, igual en la credencial y en la verificación.
- La huella no reemplaza la comparación de datos de HU-3.1: el verificador sigue comparando nombre, título y fecha con la credencial.

## Validaciones

| Dato | Regla |
|---|---|
| Huella | 64 caracteres hexadecimales en minúsculas. La pone el servidor, nunca un formulario. |
| Datos de emisión | Los cinco presentes; si falta alguno, el título no recibe huella y la verificación responde como hoy con un título incompleto («No encontrado»). |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Exactitud de la huella | Títulos sin cambios cuya huella recalculada coincide con la guardada ÷ títulos verificados. | JUnit | 100 % |
| Detección de cambios | Cambios simulados en cada uno de los cinco datos que la verificación detecta ÷ cambios simulados. | JUnit | 100 % (5 de 5) |
| Adecuación funcional | Elementos de D1 presentes ÷ requeridos: los de HU-2.5 más la huella. | JUnit (lee el PDF) | 100 % |
| Eficiencia de rendimiento | Tiempo de `/verificar/{código}` con el recálculo de la huella. | Prometheus (`http_server_requests`) | Menos de 1 s (Sprint 6: 9,8 ms de promedio) |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Alguien con acceso a la base de datos cambia un dato y también recalcula y guarda la huella: la verificación no lo detectaría. | Fuera del alcance de esta historia. Se evitaría calculando la huella con una clave secreta (HMAC-SHA256) en una variable de entorno. Queda registrado en [arquitectura y seguridad](../../tecnico/arquitectura-y-seguridad.md) como mejora pendiente. |
| La misma letra codificada de dos formas da huellas distintas y un título sin cambios aparece como «No coincide». | Normalización NFC antes de calcular (regla de negocio) y prueba con nombres con tildes (CA5). |
| Comparar 64 caracteres a ojo es difícil. | Grupos de 8 caracteres y el aviso automático de CA4, que no depende del ojo del verificador. |
| Reacomodar la credencial o la página de verificación rompe algo que ya funcionaba. | CA7: las pruebas de HU-2.5 y HU-3.1 siguen en verde. |

## Dependencias

- Necesita la credencial con QR ([HU-2.5](HU-2.5.md)) y la verificación pública ([HU-3.1](HU-3.1.md)), ya completas.
- Comparte la página de resultado de la verificación con [HU-3.2](HU-3.2.md), que agrega los estados revocado y sustituido. Para la planificación: las dos al mismo responsable o en sprints distintos, porque cambian los mismos estados.
- No necesita cambios manuales en la base de datos: `ddl-auto=update` agrega la columna nueva.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-2.8.1 | Cálculo de la huella con el texto de las reglas de negocio; se guarda al aprobar (columna `huella_integridad`); los aprobados sin huella la reciben al iniciar la aplicación. Pruebas de CA1, CA5 y CA6. | — | — |
| T-2.8.2 | La credencial imprime la huella. Prueba que lee el PDF (CA2 y CA7). | — | — |
| T-2.8.3 | La verificación muestra la huella y el estado «No coincide con la emisión». Pruebas de integración (CA3, CA4 y CA7); evidencia: la credencial y la verificación en los dos estados (para el segundo, cambiar el nombre del graduado en pgAdmin). | — | — |

## Estado

Pendiente.
