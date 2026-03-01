import os

# ================= CONFIGURACIÓN =================
# Lista de extensiones a buscar
# '.md'
EXTENSIONES = ( '.java', '.proto', '.xml', '.yml', '.properties') 

# Carpetas que queremos ignorar por completo (evita entrar en ellas recursivamente)
CARPETAS_IGNORAR = ['target', '.git', '.idea', '.agent', '.kilocode', '.vscode'] 

# Nombre del archivo de salida
ARCHIVO_SALIDA = 'salidas/codigo_completo.txt'
# =================================================

def procesar_archivos():
    root_dir = os.getcwd() # Directorio actual
    ruta_salida_abs = os.path.abspath(ARCHIVO_SALIDA)

    # Abrimos el archivo de salida
    with open(ARCHIVO_SALIDA, 'w', encoding='utf-8') as outfile:
        
        # os.walk recorre recursivamente (root, dirs, files)
        for root, dirs, files in os.walk(root_dir):
            
            # --- FILTRADO DE CARPETAS ---
            # Modificamos la lista 'dirs' en el mismo lugar (in-place).
            # Al quitar 'target' de esta lista aquí, os.walk NO entrará en ella.
            # Hacemos una copia de la lista para poder remover elementos mientras iteramos seguro.
            for carpeta in list(dirs):
                if carpeta in CARPETAS_IGNORAR:
                    dirs.remove(carpeta)
            # ----------------------------

            for file in files:
                # Verificamos la extensión
                if file.lower().endswith(EXTENSIONES):
                    full_path = os.path.join(root, file)
                    
                    # Evitar leer el propio archivo de salida
                    if os.path.abspath(full_path) == ruta_salida_abs:
                        continue
                    
                    # Calcular ruta relativa
                    ruta_relativa = os.path.relpath(full_path, root_dir)
                    
                    # Escribir cabecera
                    outfile.write(f"\n====== {ruta_relativa} ======\n")
                    
                    try:
                        with open(full_path, 'r', encoding='utf-8', errors='replace') as infile:
                            lineas = infile.readlines()
                            
                            for i, linea in enumerate(lineas, 1):
                                contenido = linea.rstrip('\n') 
                                outfile.write(f"{i}\t{contenido}\n")
                                
                    except Exception as e:
                        print(f"No se pudo leer el archivo {ruta_relativa}: {e}")

if __name__ == "__main__":
    print(f"Buscando archivos con extensiones: {EXTENSIONES}")
    print(f"Ignorando carpetas: {CARPETAS_IGNORAR}")
    procesar_archivos()
    print(f"Proceso finalizado. Revisa '{ARCHIVO_SALIDA}'")