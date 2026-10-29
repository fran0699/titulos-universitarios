# HU-2.5 · QR en la credencial

**Responsable 1: Edinson Ramirez · Funcional · Verificabilidad internacional · Prioridad Alta · 3 pts (antes 5) · Sprint 6**

## Redacción

> Como verificador (un empleador u otra institución), quiero que la credencial tenga un código QR que abra su verificación pública, para comprobar desde el celular que el título es auténtico sin pedirle nada a la universidad.

**Redacción anterior:** Como verificador, quiero que la credencial incluya un código QR y un hash de integridad para detectar alteraciones.

**Por qué cambió:** El hash solo sirve si se puede comparar en la página de verificación (P8), que llega en el mismo sprint con HU-3.1. Para que ninguna historia espere a otra, el hash pasa a [HU-2.8](../product-backlog.md) (Sprint 7, 2 pts), que lo hará completo cuando P8 ya esté en main. Esta historia se queda con el QR y con la corrección del nombre que ocupa tres líneas.

**Aporte al MVP:** Completa el eslabón «emitir la credencial»: desde la credencial impresa se llega a la verificación pública con el celular.

## Criterios de aceptación

1. **CA1.** Al leer el QR se obtiene exactamente `{titulos.verificacion.url}/{identificador}` del título, sin doble barra.
2. **CA2.** El QR no contiene datos personales: solo esa dirección.
3. **CA3.** El QR mide al menos 2,5 cm de lado, conserva su margen blanco y lleva debajo «Escanee para verificar». La línea del pie dice «Verifique este título en {url} o escaneando el código QR».
4. **CA4.** Con un nombre de 120 caracteres y un título de 150 (los máximos de [HU-4.7](HU-4.7.md)), la credencial sigue en una página y ningún texto se monta sobre la línea separadora, el documento, el QR ni otro texto.
5. **CA5.** La dirección se cambia sin tocar el código, con la variable de entorno `TITULOS_VERIFICACION_URL`, que queda en la tabla de variables del README.
6. **CA6.** La cámara de un teléfono lee el QR y muestra la dirección esperada.
7. **CA7.** Los 8 elementos del diseño D1 que entregó [HU-2.4](HU-2.4.md) se mantienen y sus pruebas siguen en verde.

## Reglas de negocio

- Solo un título APROBADO tiene credencial (y, por tanto, QR).
- El QR solo lleva la dirección de verificación con el identificador; nunca el nombre ni el documento.
- La dirección base es la misma de la línea «Verifique este título en …»: la propiedad `titulos.verificacion.url`.
- El hash de integridad llega con HU-2.8 (Sprint 7).

## Validaciones

| Dato | Regla |
|---|---|
| Identificador | Presente (UUID de 36 caracteres); ya lo exige HU-2.4. |
| Dirección base | Si termina en `/`, no se duplica la barra al agregar el identificador. |
| Tamaño del QR | Al menos 2,5 cm de lado, con margen blanco (acordado el 26/10/2026). |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Adecuación funcional | Elementos de D1 presentes ÷ requeridos: los 8 de HU-2.4, el QR y su leyenda (sin el hash, que es de HU-2.8). | JUnit (lee el PDF) | 100 % |
| Exactitud del QR | QR que se leen con la dirección esperada ÷ QR generados, incluidos nombres y títulos con el máximo de caracteres. | JUnit con ZXing | 100 % |
| Eficiencia de rendimiento | Tiempo de generación del PDF con QR. | Prometheus (`http_server_requests`) | Menos de 2 s (Sprint 5: 20 ms de promedio) |
| Defectos | Defecto del nombre de tres líneas, abierto el 25/10/2026. | Revisión del PR y evidencia | Cerrado (se registra su edad) |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| En la demostración, el teléfono no puede abrir `localhost`. | Conectar la computadora al hotspot del teléfono, poner en `TITULOS_VERIFICACION_URL` la IP de la computadora en esa red, permitir Java (o el puerto 8080) en el firewall de Windows, reiniciar la app y descargar la credencial después. Ensayarlo antes de la demostración del MVP. |
| ZXing es la parte de mayor riesgo técnico. | Subir T-2.5.1 temprano, en un PR propio. Usar ZXing 3.5.4, la versión de la [prueba de concepto](../../investigacion-pdf-qr.md) (HU-2.3). |
| Reacomodar el diseño rompe algo que ya funcionaba. | CA7: las pruebas de HU-2.4 siguen en verde. |
| Un QR impreso muy pequeño o sin margen no se lee. | CA3 (tamaño y margen) y CA6 (lectura con un teléfono). |

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-2.5.1 | ZXing en el `pom.xml` y el QR en la credencial con su dirección, tamaño, margen y leyenda; línea del pie nueva. | Edinson Ramirez | #33 |
| T-2.5.2 | El diseño se acomoda al largo del nombre y del título (corrige el defecto de tres líneas). | Edinson Ramirez | #33 |
| T-2.5.3 | Pruebas JUnit (decodificar el QR; nombre y título máximos en una página), variable `TITULOS_VERIFICACION_URL` en el README y evidencia (PDF y captura del teléfono). | Edinson Ramirez | #33 |

## Estado

Completo (Sprint 6, PR #33). Evidencias en `docs/evidencias/`: `HU-2.5-pruebas.txt` (los siete criterios), `HU-2.5-qr-prueba-telefono.pdf` (credencial de prueba), `HU-2.5-credencial-qr-vista-previa.png` (vista de la credencial con el QR) y `HU-2.5-telefono-redireccion-localhost.png` (el teléfono lee el QR y abre la dirección esperada, `…/verificar/{identificador}`; la conexión falla porque `localhost` apunta al propio teléfono). El recorrido completo desde un teléfono, con `TITULOS_VERIFICACION_URL` apuntando a la IP de la computadora, está en `HU-2.5-telefono-recorrido-completo-hotspot.png` y se repitió en la verificación de cierre del sprint.

_Criterios aprobados por el Product Owner en la planificación del Sprint 6 (26/10/2026)._
