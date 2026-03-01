#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Cliente genérico para hacer peticiones gRPC usando grpcurl.
Permite ejecutar llamadas a servicios gRPC con datos JSON y autenticación JWT.

Uso:
    python grpcurl_client.py --json datos.json --token token.txt --host localhost:9090
    --proto-path ./api/src/main/proto --proto catalogo.proto --service isam.catalogo.CatalogoService
    --method CrearProducto
"""

import subprocess
import json
import sys
import argparse
import os
import time
from typing import Dict, Optional, Any

# Configurar salida UTF-8 para Windows
if sys.platform == "win32":
    import codecs
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer)
    sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer)


class GrpcUrlClient:
    """Cliente genérico para ejecutar comandos grpcurl."""
    
    def __init__(self, grpc_host: str, proto_path: str, proto_file: str, service: str, 
                 token_file: Optional[str] = None):
        """
        Inicializa el cliente gRPC.
        
        Args:
            grpc_host: Host y puerto del servicio gRPC (ej: localhost:9090)
            proto_path: Ruta al directorio con archivos .proto
            proto_file: Nombre del archivo .proto a usar
            service: Nombre completo del servicio gRPC
            token_file: Ruta al archivo con token JWT (opcional)
        """
        self.grpc_host = grpc_host
        self.proto_path = proto_path
        self.proto_file = proto_file
        self.service = service
        self.token = self._load_token(token_file) if token_file else None
    
    def _load_token(self, token_file: str) -> Optional[str]:
        """
        Carga el token JWT desde un archivo.
        
        Args:
            token_file: Ruta al archivo con el token
            
        Returns:
            Token JWT o None si no se puede leer
        """
        try:
            if os.path.exists(token_file):
                with open(token_file, 'r', encoding='utf-8') as f:
                    token = f.read().strip()
                    if token:
                        print(f"[OK] Token JWT cargado desde {token_file}")
                        return token
                    else:
                        print(f"[WARN] El archivo {token_file} está vacío")
                        return None
            else:
                print(f"[ERROR] El archivo {token_file} no existe")
                return None
        except Exception as e:
            print(f"[ERROR] Error leyendo el archivo de token: {e}")
            return None
    
    def _build_command(self, method: str, data: Dict[str, Any]) -> list:
        """
        Construye el comando grpcurl con los parámetros configurados.
        
        Args:
            method: Método del servicio a llamar
            data: Datos JSON a enviar
            
        Returns:
            Lista con el comando completo
        """
        cmd = [
            "grpcurl",
            "-plaintext",
            "-import-path", self.proto_path,
            "-proto", self.proto_file
        ]
        
        # Añadir token si está disponible (DEBE ir ANTES de -d)
        if self.token:
            cmd.extend(["-rpc-header", f"authorization: Bearer {self.token}"])
        
        # Añadir datos y destino
        cmd.extend([
            "-d", json.dumps(data, ensure_ascii=False),
            self.grpc_host,
            f"{self.service}/{method}"
        ])
        
        return cmd
    
    def execute_request(self, method: str, data: Dict[str, Any], verbose: bool = False,
                       save_token: bool = False, token_output_file: str = "token.txt") -> Optional[Dict[str, Any]]:
        """
        Ejecuta una petición gRPC y retorna la respuesta.
        
        Args:
            method: Método del servicio a llamar
            data: Datos JSON a enviar
            verbose: Mostrar información detallada de depuración
            save_token: Si es True, extrae y guarda el token JWT de la respuesta
            token_output_file: Archivo donde guardar el token extraído
            
        Returns:
            Respuesta JSON del servidor o None si hay error
        """
        cmd = self._build_command(method, data)
        
        # Mostrar comando para depuración si verbose está activo
        if verbose:
            print(f"[DEBUG] Ejecutando: {' '.join(cmd)}")
        
        start_time = time.perf_counter()
        try:
            resultado = subprocess.run(cmd, capture_output=True, text=True, check=True, encoding='utf-8')
            duration_ms = (time.perf_counter() - start_time) * 1000
            print(f"[OK] Petición {method} exitosa (Tiempo: {duration_ms:.2f} ms)")
            
            # Intentar parsear la respuesta JSON
            if resultado.stdout.strip():
                try:
                    respuesta = json.loads(resultado.stdout)
                    
                    # Si se solicita guardar el token, intentar extraerlo
                    if save_token:
                        self._extract_and_save_token(respuesta, token_output_file)
                    
                    return respuesta
                except json.JSONDecodeError:
                    # Si no es JSON, retornar el texto crudo
                    return {"response": resultado.stdout.strip()}
            else:
                return {"response": "Respuesta vacía"}
                
        except subprocess.CalledProcessError as e:
            duration_ms = (time.perf_counter() - start_time) * 1000
            print(f"[ERROR] Error en {method} (Tiempo: {duration_ms:.2f} ms):")
            print(f"  Código de salida: {e.returncode}")
            if e.stderr:
                print(f"  Error: {e.stderr}")
            if e.stdout:
                print(f"  Salida: {e.stdout}")
            return None
        except Exception as e:
            duration_ms = (time.perf_counter() - start_time) * 1000
            print(f"[ERROR] Error inesperado (Tiempo: {duration_ms:.2f} ms): {e}")
            return None
    
    def _extract_and_save_token(self, response: Dict[str, Any], token_file: str) -> None:
        """
        Extrae el token JWT de la respuesta y lo guarda en un archivo.
        
        Args:
            response: Respuesta JSON del servidor
            token_file: Archivo donde guardar el token
        """
        # Intentar extraer el token de diferentes campos comunes
        token = None
        
        # Buscar en campos comunes de respuesta de login
        if "tokenJwt" in response:
            token = response["tokenJwt"]
        elif "token_jwt" in response:
            token = response["token_jwt"]
        elif "token" in response:
            token = response["token"]
        elif "accessToken" in response:
            token = response["accessToken"]
        elif "access_token" in response:
            token = response["access_token"]
        
        if token and isinstance(token, str) and len(token) > 0:
            try:
                with open(token_file, 'w', encoding='utf-8') as f:
                    f.write(token)
                print(f"[OK] Token JWT guardado en {token_file}")
            except Exception as e:
                print(f"[ERROR] No se pudo guardar el token: {e}")
        else:
            print("[WARN] No se encontró token JWT en la respuesta")
    
    def execute_from_json_file(self, json_file: str, method: str, verbose: bool = False,
                               save_token: bool = False, token_output_file: str = "token.txt") -> Optional[Dict[str, Any]]:
        """
        Ejecuta una petición gRPC usando datos desde un archivo JSON.
        
        Args:
            json_file: Ruta al archivo JSON con los datos
            method: Método del servicio a llamar
            verbose: Mostrar información detallada de depuración
            
        Returns:
            Respuesta JSON del servidor o None si hay error
        """
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            print(f"[OK] Datos cargados desde {json_file}")
            return self.execute_request(method, data, verbose, save_token, token_output_file)
        except FileNotFoundError:
            print(f"[ERROR] El archivo {json_file} no existe")
            return None
        except json.JSONDecodeError as e:
            print(f"[ERROR] Error parseando JSON: {e}")
            return None
        except Exception as e:
            print(f"[ERROR] Error leyendo archivo JSON: {e}")
            return None


def main():
    """Función principal con interfaz de línea de comandos."""
    parser = argparse.ArgumentParser(
        description="Cliente genérico para peticiones gRPC usando grpcurl",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Ejemplos de uso:
  # Petición simple sin autenticación
  python grpcurl_client.py --json producto.json --host localhost:9090 
  --proto-path ./api/src/main/proto --proto catalogo.proto 
  --service isam.catalogo.CatalogoService --method CrearProducto

  # Petición con autenticación JWT
  python grpcurl_client.py --json datos.json --token token.txt --host localhost:9090 
  --proto-path ./api/src/main/proto --proto usuarios.proto 
  --service isam.usuarios.UsuariosService --method CrearUsuario
        """
    )
    
    parser.add_argument("--json", required=True, 
                       help="Archivo JSON con los datos de la petición")
    parser.add_argument("--token", 
                       help="Archivo de texto con el token JWT (opcional)")
    parser.add_argument("--host", required=True, 
                       help="Host y puerto del servicio gRPC (ej: localhost:9090)")
    parser.add_argument("--proto-path", required=True, 
                       help="Ruta al directorio con archivos .proto")
    parser.add_argument("--proto", required=True, 
                       help="Nombre del archivo .proto")
    parser.add_argument("--service", required=True, 
                       help="Nombre completo del servicio gRPC")
    parser.add_argument("--method", required=True, 
                       help="Método del servicio a invocar")
    parser.add_argument("--verbose", "-v", action="store_true",
                       help="Mostrar información detallada")
    parser.add_argument("--save-token", action="store_true",
                       help="Extraer y guardar el token JWT de la respuesta (útil para login)")
    parser.add_argument("--token-output", default="token.txt",
                       help="Archivo donde guardar el token extraído (default: token.txt)")
    
    args = parser.parse_args()
    
    # Crear cliente
    client = GrpcUrlClient(
        grpc_host=args.host,
        proto_path=args.proto_path,
        proto_file=args.proto,
        service=args.service,
        token_file=args.token
    )
    
    # Ejecutar petición
    print("=" * 60)
    print("CLIENTE gRPC - EJECUTANDO PETICIÓN")
    print("=" * 60)
    print(f"Servicio: {args.service}")
    print(f"Método: {args.method}")
    print(f"Host: {args.host}")
    print(f"Proto: {args.proto}")
    print(f"Datos: {args.json}")
    if args.token:
        print(f"Token: {args.token}")
    print("-" * 60)
    
    respuesta = client.execute_from_json_file(
        args.json,
        args.method,
        args.verbose,
        args.save_token,
        args.token_output
    )
    
    if respuesta:
        print("\n" + "=" * 60)
        print("RESPUESTA DEL SERVIDOR")
        print("=" * 60)
        print(json.dumps(respuesta, indent=2, ensure_ascii=False))
        print("\n[OK] Petición completada exitosamente")
        return 0
    else:
        print("\n[ERROR] La petición falló")
        return 1


if __name__ == "__main__":
    sys.exit(main())