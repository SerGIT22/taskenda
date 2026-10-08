
Cargando mensajes anteriores…

17105.mp4
Archivo
Te voy a pedir después que me pases todo esto, recuerdalo que lo retomamos en breve, antes quería pasarte este video para que me expliques porque no entiendo, tanto Hoy como Tareas muestran exactamente lo mismo aunque haya fecha de mañana como es el caso de la última tarea, no está bien, no?

Ha pensado durante 1m 33s




Sí. Tienes razón: tal y como está ahora, no está bien. He revisado el vídeo y se ve claramente el problema.

En la captura se ve que la última tarea:

🖥️ Solución color pantalla Expediciones PC006
📅 9 oct

aparece dentro de Hoy, cuando en el vídeo estás en 8 de octubre.

El comportamiento correcto debería ser
Hoy

Tareas con fecha de hoy.
Tareas atrasadas que no se hayan completado.
Tareas sin fecha, dependiendo del criterio que queramos adoptar; yo las dejaría fuera de Hoy para que realmente sea una vista temporal.
❌ Una tarea del 9 de octubre no debería aparecer.
Tareas

Todas las tareas pendientes, independientemente de su fecha.
Aquí sí debe aparecer la del 9 de octubre.
También las de hoy, atrasadas y sin fecha.
Por tanto, ahora mismo parece que Hoy y Tareas están utilizando prácticamente el mismo filtro de "pendientes", en lugar de que Hoy aplique un filtro por fecha.

Y además hay una cuestión que conviene hacer bien ahora, antes de seguir con las subtareas:

Yo dejaría las cuatro vistas así
Vista	Qué muestra
Hoy	Atrasadas + las que vencen hoy
Tareas	Todas las pendientes
Importantes	Todas las pendientes marcadas como importantes
Completadas	Todas las completadas
Así tiene sentido que puedas entrar en Tareas y ver que mañana tienes algo pendiente, pero entrar en Hoy y centrarte exclusivamente en lo que requiere atención hoy.

Y esto es mejor corregirlo antes de continuar con las subtareas, porque forma parte de la lógica base de las tareas.

También tengo presente lo que me dijiste antes: después quieres que te vuelva a pasar todos los archivos de la Fase 2.2 con la ruta exacta de cada uno. No hace falta que me lo vuelvas a explicar cuando retomemos.

Y no tocaría el diseño que ya tienes conseguido; solo corregiría la lógica de filtrado de "Hoy". 

Vale, a su vez se podría implementar no se si en esta corrección o en otras fases una papelera con las tareas eliminadas? Por si se me ha ido la mano borrando algo, pero no se si cuadra aquí o ves mejor implementarlo en otra fase
Sí, encaja muchísimo con Taskenda, y de hecho haría una cosa concreta: prepararía la base de datos para la papelera ahora, pero dejaría la interfaz de la papelera para una fase posterior.

La razón es que ahora mismo estamos precisamente tocando Task, Room, TaskDao, TaskRepository y TaskViewModel por las subtareas. Es el momento ideal para introducir el concepto de borrado lógico.

Cómo lo haría
En lugar de que al pulsar eliminar hagamos:

❌ borrar físicamente la tarea de Room

haríamos:

🗑️ deleted = true

Así la tarea desaparece de Hoy, Tareas, Importantes y Completadas, pero sigue guardada.

Después crearíamos una vista:

🗑️ Papelera
Con:

Lista de tareas eliminadas.
Fecha de eliminación.
Restaurar → vuelve exactamente a donde estaba.
Eliminar definitivamente.
Vaciar papelera.
Confirmación antes del borrado definitivo.
Las subtareas se conservan junto con la tarea.
Restaurar una tarea restaura también sus subtareas.
Una tarea en la papelera no aparece en ninguna otra vista.
Y yo añadiría además una protección bastante útil:

Las tareas no se borrarían definitivamente automáticamente.

Es decir, nada de "se elimina después de 30 días" salvo que tú quieras hacerlo. Para una aplicación personal, prefiero que permanezcan hasta que tú decidas vaciar la papelera.

¿Dónde lo metería en las fases?
Yo reorganizaría ligeramente lo que tenemos:

Ahora mismo: corrección de Hoy

Corregir el filtro de Hoy.
Tareas → todas las pendientes.
Hoy → atrasadas + hoy.
Importantes → importantes pendientes.
Completadas → completadas.
Fase 2.2

Subtareas.
Fase 2.3

Prioridades.
Fase 2.4

Fechas, atrasadas y comportamiento temporal.
Fase 2.5

Búsqueda, filtros y ordenación.
Fase 2.6

Gestos y pulido.
Fase 2.7 — Papelera

Interfaz de papelera.
Restauración.
Eliminación definitiva.
Vaciar papelera.
Pero la estructura deleted sí la introduciría junto con la corrección/subtareas, porque así no tendremos que volver a hacer otra migración de Room inmediatamente después.

Y hay otra ventaja: esto nos permite hacer que el botón de eliminar que ya tienes sea mucho más seguro. No cambiaremos su aspecto, simplemente dejará de destruir los datos directamente.

Así que mi recomendación es: primero corregimos Hoy + dejamos preparada la eliminación reversible + después seguimos con subtareas. Es una modificación pequeña ahora que nos ahorra problemas más adelante.

Vale, corrige esto primero de Hoy, etc y luego continuamos

Ha pensado durante 27s
Hecho. He corregido solo el comportamiento de las categorías, manteniendo el resto del archivo tal cual estaba.

Qué cambia
Hoy

Muestra tareas pendientes con fecha de hoy.
También muestra tareas pendientes atrasadas.
Una tarea con fecha de mañana, como la del vídeo del 9 de octubre, ya no aparecerá en Hoy.
Tareas

Muestra todas las tareas pendientes, tengan fecha de hoy, mañana, dentro de una semana, atrasadas o sin fecha.
Importantes

Todas las pendientes marcadas como importantes.
Completadas

Todas las completadas.
También he ajustado el contador del encabezado, para que en Hoy cuente únicamente las tareas que realmente aparecen ahí.

Archivo completo
Sustituye:

app/src/main/java/com/sergio/taskenda/MainActivity.kt
por este:

No cambies ningún otro archivo por esta corrección.

Y una decisión importante: una tarea sin fecha no aparece en Hoy. Me parece lo más lógico para que "Hoy" sea realmente una bandeja temporal y no termine siendo otra copia de "Tareas".

Cuando confirmes que esto funciona, seguimos con la Fase 2.2 de subtareas y te vuelvo a pasar los archivos completos con sus rutas exactas, como me pediste.


MainActivity_Taskenda_Hoy_CORREGIDO.kt
Código
2026-10-08T19:06:26.3566593Z ##[group]Run gradle assembleDebug --stacktrace
gradle assembleDebug --stacktrace
shell: /usr/bin/bash -e {0}
env:
  JAVA_HOME: /opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/17.0.20-1/x64
  JAVA_HOME_17_X64: /opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/17.0.20-1/x64
  GRADLE_ACTION_ID: gradle/actions/setup-gradle
  GRADLE_USER_HOME: /home/runner/.gradle
  GRADLE_BUILD_ACTION_SETUP_COMPLETED: true
  GRADLE_BUILD_ACTION_CACHE_RESTORED: true
  DEVELOCITY_INJECTION_INIT_SCRIPT_NAME: gradle-actions.inject-develocity.init.gradle
  DEVELOCITY_INJECTION_CUSTOM_VALUE: gradle-actions
  GITHUB_DEPENDENCY_GRAPH_ENABLED: false
  ANDROID_HOME: /usr/local/lib/android/sdk
  ANDROID_SDK_ROOT: /usr/local/lib/android/sdk
  TASKENDA_KEYSTORE_PATH: /home/runner/work/***/***/app/***-debug.keystore
  TASKENDA_KEYSTORE_PASSWORD: ***
  TASKENDA_KEY_ALIAS: ***
  TASKENDA_KEY_PASSWORD: ***

Welcome to Gradle 9.6.0!

Here are the highlights of this release:
 - Improved Configuration Cache hit rates
 - Additional CLI rendering options
 - Important project hierarchy lookup deprecations

For more details see https://docs.gradle.org/9.6.0/release-notes.html

Starting a Gradle Daemon (subsequent builds will be faster)
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:generateDebugBuildConfig
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:processDebugNavigationResources
> Task :app:parseDebugLocalResources
> Task :app:javaPreCompileDebug
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:generateDebugRFile
> Task :app:mergeDebugAssets
> Task :app:compressDebugAssets
> Task :app:generateDebugGlobalSynthetics
> Task :app:checkDebugDuplicateClasses
> Task :app:desugarDebugFileDependencies
> Task :app:kspDebugKotlin
> Task :app:mergeExtDexDebug
> Task :app:mergeLibDexDebug
> Task :app:checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :app:compileDebugNavigationResources
> Task :app:mergeDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :app:processDebugMainManifest
> Task :app:processDebugManifest
> Task :app:processDebugManifestForPackage
> Task :app:processDebugResources
> Task :app:mergeDebugJniLibFolders
> Task :app:mergeDebugNativeLibs

> Task :app:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so. Run with --info option to learn more.

> Task :app:validateSigningDebug
> Task :app:writeDebugAppMetadata
> Task :app:writeDebugSigningConfigVersions
> Task :app:compileDebugKotlin
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:97:33 Unresolved reference 'Subtask'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:182:30 Unresolved reference 'MIGRATION_2_3'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:223:18 Property delegate must have a 'getValue(Nothing?, KProperty0<??? (Unknown type for type parameter T)>)' method. None of the following functions is applicable:
fun <T> State<T>.getValue(thisObj: Any?, property: KProperty<*>): T
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:223:21 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:223:24 Unresolved reference 'subtasks' on receiver of type 'TaskViewModel'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:223:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:225:26 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:226:28 Unresolved reference 'it'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:433:48 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:433:48 Argument type mismatch: actual type is 'String', but 'List<??? (Unresolved qualified name: Subtask)>' was expected.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:459:24 Argument type mismatch: actual type is 'Any', but 'List<??? (Unresolved qualified name: Subtask)>' was expected.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:459:43 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:459:80 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:469:20 Unresolved reference 'save' on receiver of type 'TaskViewModel'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:699:20 Unresolved reference 'Subtask'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:778:30 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:778:30 Candidate 'fun <T> Collection<T>.isNotEmpty(): Boolean' is inapplicable because of a receiver type mismatch.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:779:59 Unresolved reference 'it'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:949:20 Unresolved reference 'Subtask'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:953:25 Unresolved reference 'Subtask'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:980:26 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:981:9 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:981:9 Inapplicable candidate(s): fun <T> mutableStateOf(value: T, policy: SnapshotMutationPolicy<T> = ...): MutableState<T>
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:981:33 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:981:33 Candidate 'fun <T> Collection<T>.toMutableList(): MutableList<T>' is inapplicable because of a receiver type mismatch.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1128:69 Unresolved reference 'it'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1128:101 Function invocation 'size(...)' expected.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1141:52 Cannot infer type for value parameter 'index'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1141:59 Cannot infer type for value parameter 'subtask'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1142:25 @Composable invocations can only happen from the context of a @Composable function
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1144:47 'operator' modifier is required on 'fun <T> Comparable<T>.compareTo(other: T): Int'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1145:49 'operator' modifier is required on 'fun String.compareTo(other: String, ignoreCase: Boolean = ...): Int'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1148:79 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1148:79 Candidate 'fun <T> T.also(block: (T) -> Unit): T' is inapplicable because of a receiver type mismatch.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1148:86 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1149:51 Argument type mismatch: actual type is 'Map.Entry<uninferred K (of fun <K, V> Map.Entry<K, V>.copy), uninferred V (of fun <K, V> Map.Entry<K, V>.copy)>', but 'Char' was expected.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1149:59 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1149:59 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1149:59 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1149:64 No parameter with name 'completed' found.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1149:85 Unresolved reference 'completed'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1154:43 'operator' modifier is required on 'fun <T> Comparable<T>.compareTo(other: T): Int'.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1155:83 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1155:83 Candidate 'fun <T> T.also(block: (T) -> Unit): T' is inapplicable because of a receiver type mismatch.
e: file:///home/runner/work/***/***/app/src/main/java/com/sergio/***/MainActivity.kt:1155:90 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/***/***/a
…

Ha pensado durante 1m 18s
Sí, ya está localizado el problema y no tiene nada que ver con tu proyecto ni con Gradle.

El error fue mío: te pasé como corrección de Hoy un MainActivity.kt que todavía contenía parte de la Fase 2.2 de subtareas. Por eso GitHub intenta encontrar:

Subtask
MIGRATION_2_3
vm.subtasks
vm.save(...)
y tu proyecto actual todavía no tiene nada de eso.

Todos los errores posteriores son prácticamente errores en cascada provocados por ese primero.

Qué tienes que hacer AHORA
Solo sustituye un archivo:

app/src/main/java/com/sergio/taskenda/MainActivity.kt
por este:

Este archivo está basado en la versión estable que ya tenías con listas, no en la Fase 2.2.

Ahora queda así
Hoy

Atrasadas.
Las que vencen hoy.
❌ No las de mañana.
Tareas

Todas las pendientes.
Hoy.
Mañana.
Futuras.
Atrasadas.
Sin fecha.
Importantes

Todas las pendientes importantes.
Completadas

Todas las completadas.
Además, el contador de cada sección se adapta al contenido correspondiente y respeta la lista seleccionada.

MUY IMPORTANTE
No pongas todavía estos archivos de la Fase 2.2:

Subtask.kt
TaskDao.kt de subtareas
TaskRepository.kt de subtareas
TaskDatabase.kt con MIGRATION_2_3
TaskViewModel.kt de subtareas
TaskViewModelFactory.kt de subtareas
Los dejamos aparcados hasta que terminemos esta corrección.

Y el mensaje:

Unable to strip ... libandroidx.graphics.path.so
no es el problema. El build llega perfectamente hasta compileDebugKotlin.

Sube únicamente este MainActivity.kt, ejecuta Actions otra vez y pásame el resultado. Esta vez estamos trabajando sobre la versión correcta, sin meter ninguna parte de la Fase 2.2.


MainActivity_Hoy_Tareas_CORREGIDO_FINAL.kt
Código
PARECE QUE FUNCIONA, CONTINUEMOS

Ha pensado durante 1m 32s
Perfecto. Entonces seguimos con Fase 2.2 — Subtareas, pero esta vez de forma coherente con la versión que acabas de comprobar que funciona.

He preparado los archivos tomando como base el MainActivity.kt estable de Hoy/Tareas que acabamos de validar y añadiendo encima las subtareas.

Qué incorpora esta fase
✅ Crear subtareas dentro de una tarea.
✅ Marcar/desmarcar subtareas.
✅ Editar una tarea y modificar sus subtareas.
✅ Eliminar subtareas.
✅ Cambiar el orden de las subtareas.
✅ Contador X/Y subtareas en la tarea.
✅ Contador X/Y completadas dentro del editor.
✅ Las subtareas se guardan junto con la tarea.
✅ Al editar una tarea se conservan.
✅ Al eliminar una tarea, Room elimina sus subtareas automáticamente.
✅ Migración Room 2 → 3 para no perder tus tareas actuales.
✅ Operaciones de base de datos en Dispatchers.IO.
✅ Mantiene el diseño que ya tienes.
✅ El editor ahora puede desplazarse para que muchas subtareas no corten el botón de guardar.
1. MainActivity.kt — SUSTITUIR
Ruta:

app/src/main/java/com/sergio/taskenda/MainActivity.kt
Este es el archivo principal de esta fase.

2. Subtask.kt — NUEVO
Ruta:

app/src/main/java/com/sergio/taskenda/data/Subtask.kt
Este archivo no existía. Créalo dentro de data.

3. TaskDao.kt — SUSTITUIR
Ruta:

app/src/main/java/com/sergio/taskenda/data/TaskDao.kt
Incluye las operaciones de tareas y subtareas, además de la transacción para guardar una tarea junto con todas sus subtareas.

4. TaskRepository.kt — SUSTITUIR
Ruta:

app/src/main/java/com/sergio/taskenda/data/TaskRepository.kt
5. TaskDatabase.kt — SUSTITUIR
Ruta:

app/src/main/java/com/sergio/taskenda/data/TaskDatabase.kt
Aquí está la migración:

Room 2 → Room 3
y se crea la tabla:

subtasks
⚠️ MUY IMPORTANTE
No desinstales Taskenda.

La aplicación deberá actualizarse encima de la que ya tienes para que Room haga:

versión 2
    ↓
MIGRATION_2_3
    ↓
versión 3
Tus tareas actuales deberían mantenerse.

6. TaskViewModel.kt — SUSTITUIR
Ruta:

app/src/main/java/com/sergio/taskenda/ui/TaskViewModel.kt
Este mantiene la gestión de listas que ya utiliza tu aplicación:

lists
currentList
selectList()
createList()
renameList()
deleteList()
y añade:

subtasks
save()
toggleSubtask()
deleteSubtask()
addSubtask()
7. TaskViewModelFactory.kt — SUSTITUIR
Ruta:

app/src/main/java/com/sergio/taskenda/ui/TaskViewModelFactory.kt
📁 El árbol final
Después de sustituirlos, deberías tener:

app/
└── src/
    └── main/
        └── java/
            └── com/
                └── sergio/
                    └── taskenda/
                        │
                        ├── MainActivity.kt
                        │
                        ├── data/
                        │   ├── Task.kt
                        │   ├── Subtask.kt              ← NUEVO
                        │   ├── TaskDao.kt
                        │   ├── TaskDatabase.kt
                        │   ├── TaskRepository.kt
                        │   └── TaskListStore.kt
                        │
                        └── ui/
                            ├── TaskViewModel.kt
                            └── TaskViewModelFactory.kt
❌ No modificar
De momento deja exactamente como están:

Task.kt
TaskListStore.kt
app/build.gradle.kts
AndroidManifest.xml
.github/workflows/...
No necesitamos ninguna dependencia nueva para las subtareas.

Y una cosa importante
Esta vez no te voy a decir que está compilado, porque no he ejecutado tu proyecto Gradle real. He preparado los archivos para que sean una implementación coherente entre sí y, sobre todo, basada en la versión de MainActivity que acabamos de validar.

Sube los 7 archivos, ejecuta GitHub Actions y no desinstales la aplicación.

Si el build da algún error, pásame el log como has hecho antes y corregimos sobre esta base.


MainActivity_Taskenda_Fase2_2.kt
Código

Subtask.kt
Código

TaskDao_Fase2_2.kt
Código

TaskRepository_Fase2_2.kt
Código

TaskDatabase_Fase2_2.kt
Código

TaskViewModel_Fase2_2.kt
Código

TaskViewModelFactory_Fase2_2.kt
Código
Mostrar menos



package com.sergio.taskenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sergio.taskenda.data.TaskListStore
import com.sergio.taskenda.data.TaskRepository

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val listStore: TaskListStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository, listStore) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

