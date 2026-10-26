# Cómo trabajamos

Los acuerdos del equipo para planificar, desarrollar, revisar y cerrar cada sprint. Si un acuerdo cambia, se actualiza aquí en un Pull Request de documentación y se avisa en el grupo, para que todos trabajen con la misma versión.

## Equipo

| Integrante | GitHub | Rol |
|---|---|---|
| Francisco Lata | `fran0699` | Product Owner y desarrollador |
| Henry Birminghan | `birminghanhenry-arch` | Desarrollador |
| Anel Gonzalez | `aggon14` | Desarrolladora |
| Edinson Ramirez | `Edinson31` | Desarrollador |

Los roles de Scrum son formales: en la práctica, los cuatro deciden y desarrollan por igual. Las áreas de trabajo rotan entre sprints.

## El sprint en una semana

```
Planificación ──► Desarrollo y revisión ──► Cierre ──────────► Retrospectiva
(historias        (una rama por tarea,       (verificación      (qué funcionó,
 corregidas,       PR revisado por el         sobre main,        qué no y qué
 cuadro)           siguiente del relevo)      Nikto, métricas)   se ajusta)
```

### 1. Planificación (orden fijo)

Primero se corrigen las historias y después se decide el alcance. Así el equipo recibe el cuadro con historias que ya no van a cambiar.

1. **Reunir insumos:** el resultado y las lecciones del sprint anterior ([historial](../sprints/historial.md)) y las prioridades del Product Owner.
2. **Refinar:** corregir las historias candidatas hasta que cumplan la Definición de Listo. Cada historia corregida se escribe en [`docs/producto/historias/`](../producto/historias/README.md); el Product Owner la aprueba y se sube en un [Pull Request de documentación](#pull-requests-de-documentación). El equipo la recibe en el paso 5.
3. **Planificar:** decidir el alcance según la velocidad reciente ([historial](../sprints/historial.md)) y armar el cuadro del Sprint Backlog.
4. Si al dividir en tareas cambia una historia, se corrige la historia antes de enviar el cuadro.
5. **Enviar al equipo** las historias corregidas y el cuadro. Cada integrante elige su número de responsable.

**Comprometer o no comprometer:** no hay historias «opcionales». Lo que entra al sprint se compromete; lo demás queda en el backlog. Una historia agregada a mitad del sprint necesita el acuerdo del equipo y se registra como «agregada».

### Definición de Listo

Una historia entra a la planificación solo si cumple todo:

- [ ] Redacción verificable: actor real, qué y para qué.
- [ ] Criterios de aceptación que se comprueban con sí o no.
- [ ] Valores definidos: los que la guía de pantallas no fija se acuerdan antes (por ejemplo, «PDF en menos de 2 s»).
- [ ] Dependencias conocidas: otras historias y pantallas.
- [ ] Estimación revisada con los criterios a la vista.
- [ ] Riesgos y métrica QA asociada.

### 2. Sprint Backlog: formato y orden de relevo

Un solo cuadro, con una fila por tarea:

| Responsable | Historia (pts) | Tarea | Qué se hace | Depende de | Revisa el PR |
|---|---|---|---|---|---|
| 1 | HU-4.7 (3) | T-4.7.1 (base) | … | — | 2 |
| 2 | HU-3.3 (3) | T-3.3.2 | … | T-3.3.1 y T-4.7.1 (Resp. 1) ya fusionada | 3 |

**Regla de numeración (relevo).** Con solo ver su número, cada quien sabe de quién depende:

- El **1** tiene la tarea base, que se fusiona primero. El **4** es quien más depende de los demás y fusiona al final.
- Nadie depende de un número mayor que el suyo.
- El relevo ordena las **fusiones**, no el inicio: los cuatro empiezan el primer día, cada uno con una tarea sin dependencias.
- Una historia independiente puede ocupar un número intermedio y fusionar cuando termine.
- Cuenta como dependencia tanto la funcional como la de un archivo compartido (mismo método o mismo controlador).

**Revisión:** cada PR lo revisa el siguiente del relevo: el 2 revisa al 1, el 3 al 2, el 4 al 3 y el 1 al 4. El Product Owner puede aprobar cualquier PR. Si un revisor no responde, se avisa en el grupo y la revisión pasa al Product Owner.

**Notas de fusión:** la tarea de mayor riesgo técnico se sube temprano, en un PR propio. Quien fusiona después de otro que cambió un archivo compartido (por ejemplo, `pom.xml`) trae main a su rama antes.

**Cierre:** debajo del cuadro se indica quién ejecuta Nikto y quién hace la verificación de principio a fin sobre main.

### 3. Desarrollo: flujo de Git

| Qué | Convención | Ejemplo |
|---|---|---|
| Rama | `feature/HU-X.Y-descripcion` (una por tarea o historia) | `feature/HU-4.10-usuario-tecnico-prometheus` |
| Commit | `T-X.Y.Z: descripción` | `T-4.10.2: denegar por defecto en las pantallas` |
| Título del PR | `HU-X.Y / T-X.Y.Z: descripción` | `HU-2.4 / T-2.4.1–T-2.4.3: credencial PDF y descarga para el personal` |
| Evidencia | `docs/evidencias/HU-X.Y-descripcion.ext` | `docs/evidencias/HU-4.10-matriz-acceso.txt` |
| Rama (solo documentación) | `feature/docs-descripcion` | `feature/docs-documentacion-del-proyecto` |
| Commit (solo documentación) | `docs: descripción` | `docs: documentacion del proyecto en el repositorio` |
| Título del PR (solo documentación) | `Docs: descripción` | `Docs: documentación del proyecto en el repositorio` |

**Antes de cada commit**

```powershell
.\mvnw.cmd clean test      # todo en verde
git status                 # solo lo que esperas
git add ruta\del\archivo   # por nombre, no "git add ."
```

Si el commit solo cambia documentación (`README.md` o `docs/`), no hace falta `clean test`: CircleCI corre las pruebas igual.

**Antes de pedir revisión:** traer main y volver a probar.

```powershell
git fetch origin
git merge origin/main      # si hay conflictos, en los archivos de otra historia se conserva la versión de main
.\mvnw.cmd clean test
git push
```

Los conflictos se resuelven solo con `git merge origin/main`. **No se copian archivos de main a mano:** se pueden borrar cambios que ya estaban fusionados.

**Descripción del Pull Request**

```markdown
## Qué cambia
Qué tareas cubre y qué criterios de aceptación cumple (CA1, CA2…).

## Cómo se verificó
Número de pruebas (por ejemplo, 95 en verde) y la evidencia en docs/evidencias/.

## Acción requerida para el equipo
(Solo si aplica.) Variables de entorno nuevas, cambios en la base de datos local, algo que instalar.
```

**Fusionar** solo con CircleCI en verde y la aprobación («Approve») de un compañero, usando «Create a merge commit». Las correcciones van como commits nuevos en el mismo PR. Después de fusionar:

```powershell
git checkout main
git pull
.\mvnw.cmd clean test
git branch -d feature/HU-X.Y-descripcion
```

#### Pull Requests de documentación

Un PR que solo cambia documentación (`README.md` o `docs/`) **no necesita aprobación**: lo fusiona quien lo abre, con CircleCI en verde. Si cambia un acuerdo del equipo, se avisa en el grupo.

La aprobación de un compañero es para los PR de las historias y tareas del sprint. Si una historia cambia documentación (por ejemplo, el stack o la guía de pantallas), ese cambio va en el PR de la historia y se revisa con ella.

### 4. Revisión de un Pull Request

- [ ] El PR dice qué historia y qué criterios de aceptación cubre, y cada criterio se cumple (sí o no).
- [ ] Hay pruebas nuevas y CircleCI está en verde.
- [ ] La evidencia está en `docs/evidencias/`.
- [ ] **Rutas nuevas:** las públicas usan `permitAll()` y las de un solo rol tienen su regla antes de `anyRequest()` en `SecurityConfig` (ver [arquitectura y seguridad](../tecnico/arquitectura-y-seguridad.md)).
- [ ] **Formulario:** se enlaza a una clase con solo los campos permitidos (sin asignación masiva). El usuario sale de la sesión.
- [ ] Las acciones que cambian datos van por POST con CSRF; sin estilos ni scripts en línea.
- [ ] **Configuración fuera del código:** una variable de entorno nueva está en la tabla del README. Un cambio en una tabla existente, o un valor nuevo en un enum guardado como texto, tiene su entrada en «Cambios manuales en la base de datos local».
- [ ] Si cambia el stack, la configuración o una historia, el mismo PR actualiza `docs/`.
- [ ] **Pantallas:** si el PR cambia el diseño de una pantalla respecto de su imagen en `docs/producto/pantallas/` (campos, columnas, botones, textos o flujo), lo dice en «Acción requerida para el equipo» para que el Product Owner actualice la imagen.

**Cómo clasificar lo que se encuentra:**

- **Necesario:** todo lo que forma parte del «Qué se hace» de la tarea o de sus criterios de aceptación, además de cualquier fallo de seguridad. Se pide con «Request changes». Si se deja como recomendación, la historia se fusiona incompleta.
- **Recomendado:** mejoras que no forman parte de la tarea. Se pueden dejar para otra historia.

### Definición de Terminado

Una historia está terminada cuando:

- [ ] Las pruebas están en verde.
- [ ] El PR está revisado y fusionado en main.
- [ ] La evidencia está en `docs/evidencias/`.
- [ ] Si tiene pantalla, cumple su lista «Debe tener», las reglas comunes y las reglas de validación de la [guía de pantallas](../producto/guia-de-pantallas.md).
- [ ] **(Propuesta del Sprint 5; se confirma en la planificación del Sprint 6.)** Su archivo en `docs/producto/historias/` y el [Product Backlog](../producto/product-backlog.md) están actualizados con el estado y las evidencias.

### 5. Cierre del sprint

Sobre la versión final de main, después de la última fusión:

1. **Verificación de principio a fin**, con una base de datos creada desde cero, comprobando cada criterio de aceptación del sprint.
2. **Consola del navegador** (F12) sin errores de CSP.
3. **Nikto 2.6.1**, comparado con el sprint anterior. Lo ejecuta quien se asignó en la planificación y se anota el commit escaneado. Instalación y comando: [stack y versiones](../tecnico/stack-y-versiones.md).
4. **Métricas:** puntos comprometidos y completados, velocidad, pruebas (heredadas y nuevas), PR fusionados, hallazgos de Nikto, avance del backlog por épica y las métricas QA de cada historia.
5. Actualizar el [historial de sprints](../sprints/historial.md), el [Product Backlog](../producto/product-backlog.md), el estado de cada historia y el estado de implementación de la [guía de pantallas](../producto/guia-de-pantallas.md).

**Evidencias:** se generan en el momento de la prueba, antes de fusionar o borrar la rama, y se guardan en `docs/evidencias/` con el nombre `HU-X.Y-descripcion.ext`.

### 6. Retrospectiva

Al final de cada sprint, el equipo responde tres preguntas sobre el proceso, no sobre las personas:

- **Qué funcionó bien** y conviene mantener.
- **Qué no funcionó** y qué causó.
- **Qué se ajusta** en el próximo sprint: acciones concretas, que se aplican desde la planificación siguiente.

El resultado se registra en el [historial de sprints](../sprints/historial.md) como «Lecciones». Si una lección cambia un acuerdo de este documento, se actualiza aquí en el mismo Pull Request.

## Dónde está cada cosa

| Busco… | Está en |
|---|---|
| Qué es el producto y qué es el MVP | [`docs/producto/vision-y-mvp.md`](../producto/vision-y-mvp.md) |
| Todas las historias, con puntos y estado | [`docs/producto/product-backlog.md`](../producto/product-backlog.md) |
| Criterios de aceptación de una historia | [`docs/producto/historias/`](../producto/historias/README.md) |
| Cómo debe verse cada pantalla y cómo se valida cada campo | [`docs/producto/guia-de-pantallas.md`](../producto/guia-de-pantallas.md) |
| Qué instalar y en qué versión | [`docs/tecnico/stack-y-versiones.md`](../tecnico/stack-y-versiones.md) |
| Rutas, roles, tablas y controles de seguridad | [`docs/tecnico/arquitectura-y-seguridad.md`](../tecnico/arquitectura-y-seguridad.md) |
| Configurar la base de datos y las variables | [`README.md`](../../README.md) |
| Levantar Prometheus | [`docs/prometheus.md`](../prometheus.md) |
| Qué se hizo en cada sprint | [`docs/sprints/historial.md`](../sprints/historial.md) |
| Evidencias (capturas, informes, pruebas) | [`docs/evidencias/`](../evidencias/) |
