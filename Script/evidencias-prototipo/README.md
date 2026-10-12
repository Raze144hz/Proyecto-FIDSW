# Recorrido automático de la interfaz del prototipo

`Evidencias.java` abre el prototipo, recorre los casos de prueba en la interfaz real y guarda una captura de cada paso. Así se generaron las capturas de `Graficas/entrega-2/` y el registro `Docs/Calidad/evidencias/registro-ejecucion-interfaz.txt`, que se usan en el [acta de pruebas del Sprint 2](../../Docs/Calidad/actas/acta-pruebas-sprint-2.md).

## Qué recorre

1. Inicio de sesión, con contraseña incorrecta y correcta.
2. Creación de planes, incluida la validación del objetivo vacío.
3. Envío a revisión.
4. Casos CP-HU017-01 a CP-HU017-04 con los roles de coordinador y director.
5. Notificaciones, historial del plan, bloqueo de edición de un plan aprobado y rol RESPONSABLE en modo consulta.

## Requisitos

- Linux con `Xvfb` (pantalla virtual) e ImageMagick (comando `import`).
- Las clases del prototipo ya compiladas y las librerías de JavaFX, H2 y jBCrypt.

## Cómo se ejecuta

Desde una carpeta vacía, para que la base H2 empiece sin datos:

```bash
CP="<jars de JavaFX, H2 y jBCrypt>:<clases del prototipo>"
javac --release 17 -encoding UTF-8 -cp "$CP" -d salida-script Evidencias.java
xvfb-run -a -s "-screen 0 1600x1000x24" \
  java -Dprism.order=sw -Dfile.encoding=UTF-8 -cp "$CP:salida-script" \
  com.sgpi.Evidencias carpeta-de-capturas
```

Al terminar, `carpeta-de-capturas` tiene las capturas en PNG y `registro-ejecucion.txt` con lo que mostró la aplicación en cada paso.

No hace falta para usar el prototipo ni para `mvn test`: solo sirve para repetir las evidencias.
