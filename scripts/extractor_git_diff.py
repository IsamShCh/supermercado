import subprocess
import os
import sys

# ================= CONFIGURACIÓN =================
ARCHIVO_SALIDA = 'salidas/cambios_git_diff.txt'
# =================================================

def obtener_cambios_git():
    print("🔍 Analizando repositorio Git...")

    # 1. Verificamos si estamos en un repo de git
    # Ejecutamos 'git rev-parse --is-inside-work-tree' para ver si es un repo válido
    try:
        subprocess.check_call(
            ["git", "rev-parse", "--is-inside-work-tree"], 
            stdout=subprocess.DEVNULL, 
            stderr=subprocess.DEVNULL
        )
    except subprocess.CalledProcessError:
        print("❌ Error: El directorio actual no es un repositorio de Git.")
        return

    # 2. Obtenemos los cambios (DIFF)
    # 'git diff HEAD' muestra las diferencias entre el directorio de trabajo y el último commit.
    # Esto incluye archivos en 'staged' (verdes) y 'unstaged' (rojos).
    # --unified=3 da 3 líneas de contexto alrededor del cambio (estándar).
    try:
        resultado = subprocess.run(
            ["git", "diff", "HEAD", "--unified=3"],
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='replace' # Evita errores si hay binarios o caracteres raros
        )
        
        diff_content = resultado.stdout

        if not diff_content:
            print("✅ No se detectaron cambios respecto al último commit.")
            return

    except Exception as e:
        print(f"❌ Error ejecutando git: {e}")
        return

    # 3. Obtenemos la lista de archivos nuevos (Untracked)
    # 'git diff HEAD' a veces ignora archivos nuevos que nunca han sido añadidos.
    # Usamos 'git ls-files --others --exclude-standard' para ver archivos nuevos.
    try:
        archivos_nuevos = subprocess.run(
            ["git", "ls-files", "--others", "--exclude-standard"],
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='replace'
        ).stdout

        contenido_extra = ""
        if archivos_nuevos:
            contenido_extra = "\n\n====== ARCHIVOS NUEVOS (UNTRACKED) ======\n"
            contenido_extra += "(Estos archivos son nuevos y no existían en el commit anterior)\n"
            # Para los nuevos, leemos su contenido entero porque no hay 'diff' previo
            for archivo in archivos_nuevos.splitlines():
                contenido_extra += f"\n--- Nuevo Archivo: {archivo} ---\n"
                try:
                    with open(archivo, 'r', encoding='utf-8', errors='replace') as f:
                        # Numeramos las líneas también para mantener consistencia
                        lineas = f.readlines()
                        for i, linea in enumerate(lineas, 1):
                            contenido_extra += f"{i}\t{linea}"
                except Exception as e:
                    contenido_extra += f"[No se pudo leer el contenido: {e}]\n"

    except Exception:
        contenido_extra = ""

    # 4. Escribir todo al archivo de salida
    try:
        with open(ARCHIVO_SALIDA, 'w', encoding='utf-8') as f:
            f.write("====== REPORTE DE CAMBIOS GIT (vs HEAD) ======\n")
            f.write("Generado automáticamente.\n\n")
            
            if diff_content:
                f.write("====== MODIFICACIONES (DIFF) ======\n")
                f.write(diff_content)
            
            if contenido_extra:
                f.write(contenido_extra)
        
        ruta_completa = os.path.abspath(ARCHIVO_SALIDA)
        print(f"✅ Contexto extraído correctamente en:\n   -> {ruta_completa}")

    except Exception as e:
        print(f"❌ Error escribiendo el archivo de salida: {e}")

if __name__ == "__main__":
    obtener_cambios_git()