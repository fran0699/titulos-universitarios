# HU-1.11 · Catálogo oficial de programas

**Dueño: por asignar · Funcional · Precisión · Prioridad Alta · 5 pts · Sprint por planificar**

## Redacción

> Como registrador, quiero elegir el programa de un catálogo oficial, con sugerencias mientras escribo, para que un mismo título no quede escrito de formas distintas.

**Redacción anterior:** Como registrador, quiero elegir el programa de un catálogo oficial con búsqueda para que un mismo título no quede escrito de formas distintas.

**Por qué cambió:** El catálogo trae todos los programas de la universidad (unos 160), así que hace falta buscar; la búsqueda la hace el propio navegador con sugerencias (lista `datalist` de HTML), sin JavaScript.

**Aporte al producto:** Hoy el nombre del título es texto libre: «Lic. en Ing. de Software» y «Licenciatura en Ingeniería de Software» quedan como títulos distintos, la regla de duplicados no los detecta y la credencial puede salir con un nombre que no es el oficial.

## Criterios de aceptación

1. **CA1.** El catálogo trae todos los programas del documento oficial «Oferta académica por sede, según facultad y carrera/programa: Año 2025» de la Universidad Tecnológica de Panamá ([PDF](https://utp.ac.pa/sites/default/files/documentos/2025/pdf/utp-oferta-academica-2025-final.pdf)), con su nombre y su facultad.
2. **CA2.** El catálogo vive en un archivo de datos del repositorio (por ejemplo, `src/main/resources/catalogo/programas.csv`, con las columnas nombre, facultad y activo, y el documento de origen en un comentario al inicio). Al iniciar, la aplicación carga los programas que falten en la tabla `programas`, sin duplicarlos.
3. **CA3.** En «Registrar título» (P3), el campo «Programa» sugiere los programas activos del catálogo mientras se escribe.
4. **CA4.** El servidor solo acepta el nombre exacto de un programa activo (sin distinguir mayúsculas y sin espacios al inicio o al final). Si no, muestra junto al campo «Elija un programa del catálogo». El título guarda el nombre tal como está en el catálogo.
5. **CA5.** Un programa inactivo no se sugiere y se rechaza. Los títulos ya registrados conservan su nombre, aunque su programa se desactive o no esté en el catálogo.
6. **CA6.** El título sigue guardando el nombre del programa como texto, así que la credencial, la verificación, la huella ([HU-2.8](HU-2.8.md)) y la regla de duplicados no cambian.

## Reglas para armar el archivo

- Los nombres se copian del documento oficial, sin las marcas de nota que trae (por ejemplo, el «(2)» de «Licenciatura en Desarrollo y Gestión de Software (2)»).
- «Maestría y Postgrado en X» son dos programas: «Maestría en X» y «Postgrado en X», porque cada uno otorga su propio título.
- Un programa que ofrecen varias facultades se escribe una sola vez, con la facultad «Varias facultades».
- Los títulos intermedios de técnico de las licenciaturas no aparecen en el documento: quedan fuera hasta que la universidad los confirme.
- Un programa que deja de ofrecerse se marca inactivo; nunca se borra.
- Para actualizar el catálogo con la oferta de otro año se cambia el archivo en un Pull Request.

## Validaciones

| Dato | Regla |
|---|---|
| Programa | Nombre de un programa activo del catálogo; de 5 a 150 caracteres. Las tildes cuentan, igual que en la búsqueda de [HU-1.8](HU-1.8.md). |
| Nombre en el catálogo | Único; de 5 a 150 caracteres. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Carga del catálogo | Programas en la tabla ÷ filas del archivo. | JUnit | 100 % |
| Exactitud de los datos | Nombres del archivo que coinciden con el documento oficial, revisados por otro integrante (todos, o una muestra de 30 si no da el tiempo). | Revisión del PR | 100 % de la muestra |
| Consistencia | Títulos nuevos con un nombre que no está en el catálogo. | JUnit (MockMvc) | 0 |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Errores al copiar unos 160 nombres. | El revisor del PR compara el archivo con el documento oficial (métrica «Exactitud de los datos»). |
| El navegador no ignora tildes en las sugerencias y cada navegador sugiere un poco distinto. | Lo que garantiza la precisión es la validación del servidor (CA4), no las sugerencias. |
| La oferta cambia cada año. | Regla de actualización por Pull Request, con el documento de origen en el archivo. |

## Dependencias

- Ninguna historia pendiente.
- Comparte el registro de títulos (P3) con [HU-2.6](HU-2.6.md), que agrega el enlace al título duplicado. Para la planificación: al mismo responsable o en sprints distintos.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-1.11.1 | Archivo del catálogo con todos los programas, según las reglas de arriba. Subirlo temprano en un PR propio, para que el revisor tenga tiempo de compararlo. | — | — |
| T-1.11.2 | Tabla `programas`, carga al iniciar y validación del programa al registrar (CA2, CA4 a CA6). Pruebas. | — | — |
| T-1.11.3 | Campo «Programa» con sugerencias en P3 (CA3). Pruebas con MockMvc, evidencia (captura de las sugerencias y del error) y la fila de P3 en la tabla de estado de la guía. | — | — |

## Estado

Pendiente.
