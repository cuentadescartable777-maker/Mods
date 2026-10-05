# Argentina Flags (NeoForge 1.21.1)

Banderas argentinas decorativas y crafteables (mod id: `argentinaflags`).

## Como compilar
1. Instala un JDK 21.
2. Falta el wrapper de Gradle (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`): son archivos
   binarios/boilerplate. Opcion A: con Gradle 8.10+ instalado, ejecuta una vez `gradle wrapper --gradle-version 8.10.2`.
   Opcion B: copialos del MDK oficial https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle
3. Ejecuta `./gradlew build` (Windows: `gradlew.bat build`).
4. El .jar queda en `build/libs/argentinaflags-1.0.0.jar`.

Para probarlo en desarrollo: `./gradlew runClient`. Tambien se puede abrir la carpeta con IntelliJ IDEA (Open > build.gradle).

## Como instalar
Copia el .jar en la carpeta `mods` de tu instancia de Minecraft 1.21.1 con NeoForge 21.1.x (cualquier 21.1.x reciente).

## Como agregar una bandera
1. `tools/AssetGenerator.java`: agregar una entrada en `flags()` y un metodo pintor.
2. `ModBlocks.FLAG_IDS`: agregar el mismo id.

Texturas, modelos, blockstates, recetas, loot tables y lang (en_us / es_ar) se generan solos en cada build.

## Solucion de errores
- "Could not find net.neoforged:neoforge:21.1.172" -> cambia `neo_version` en gradle.properties por otra 21.1.x existente.
- "Plugin net.neoforged.moddev not found/incompatible" -> cambia la version del plugin en build.gradle por una 2.0.x vigente.
- "No matching toolchains found" -> instala JDK 21 (o deja activo el plugin foojay de settings.gradle con internet).
- "Unsupported class file major version" -> Gradle esta corriendo con un JDK mas viejo que 17/21; configura JAVA_HOME a JDK 21.
- Texturas en violeta/negro -> corre `./gradlew clean build` para regenerar `build/generated/flag-assets`.
- La tarea generateAssets falla -> revisa que Gradle corra con un JDK completo (no JRE).

## Compilar en GitHub (sin instalar nada)
1. Crea un repositorio en GitHub y sube todo el contenido de esta carpeta (incluida `.github`).
2. Entra en la pestana Actions, abre la ejecucion "Build mod" y espera a que termine (unos minutos).
3. Al final de la pagina, en Artifacts, descarga `argentinaflags-jar` (es un zip con el .jar adentro).

## Mastil extensible, Rosas y discos (v1.1)
- Mastil: item "Flag Pole" (3 palos en columna = 3 mastiles). Clic derecho sobre una bandera de suelo con un mastil la sube un bloque.
- Bandera de la Confederacion (epoca de Rosas): sol rojo y gorros frigios en las esquinas.
- Discos: ver sounds/music_disc/LEEME.txt (hay que agregar los .ogg y ajustar la duracion).

## v1.2: 31 banderas x2 tamanos, ondeo y 13 discos
- 62 bloques de bandera: `flag_x` (normal) y `flag_x_grande` (4x el area). La grande se fabrica con 4 banderas normales iguales en un cuadrado 2x2.
- Las banderas de suelo/mastil ondean (efecto del renderer, sin entidades); las de pared son estaticas.
- Discos: copiar los .ogg (MONO) en `src/main/resources/assets/argentinaflags/sounds/music_disc/` con el nombre `<id>.ogg`
  (himno_nacional, marcha_san_lorenzo, marcha_malvinas, avenida_camelias, aurora, pucara_malvinas, sobreviviendo,
  el_grandote, diablo_humahuaca, campanas_noche, cara_tramposo, estrella_federal, reina_madre).
- Duraciones: `tools/AssetGenerator.java`, tabla `discs` (cuarto valor de cada fila, en segundos).
- Alcance del sonido: `ATTENUATION_DISTANCE` en el mismo archivo (alcance en bloques = 4 x valor).
