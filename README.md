**TecnoMovil Data**  
**Procesamiento funcional de datos para un sistema de gestión de transporte urbano**  
Solución académica que simula y analiza registros de entrada y salida de pasajeros en un sistema de transporte público utilizando **Java 17+** de forma nativa (sin frameworks ni dependencias externas).  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnEAAAACCAYAAAA3pIp+AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAANklEQVR4nO3OUQmAABBAsSeYxZyXSzCJASxgACv4J8KWYMvMbNURAAB/ca7VXe1fTwAAeO16AKe+BdmJqrPdAAAAAElFTkSuQmCC)  
**🚀 Inicio Rápido**  
**Requisitos**  
- JDK 17 o superior.  
**Compilación y Ejecución (Consola)**  
**Linux / macOS:**  
mkdir -p out  
 javac -d out $(find src/main/java -name "*.java")  
 java -cp out edu.iudigital.tecnomovil.Main [cantidad_usuarios]  
   
**Windows (PowerShell):**  
New-Item -ItemType Directory -Force out  
 $archivos = Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName }  
 javac -d out $archivos  
 java -cp out edu.iudigital.tecnomovil.Main [cantidad_usuarios]  
   
*Por defecto, si no se especifica el parámetro de usuarios, la simulación procesa * ***100.000 usuarios*** * (200.000 registros).*  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnEAAAACCAYAAAA3pIp+AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAANUlEQVR4nO3OsQ1AABRAwSdRqNXWsN+v7WkPK+hEcjfBLTNzVFcAAPzFvVZbdX49AQDgtf0BVFgDYBMgFRYAAAAASUVORK5CYII=)  
**📊 Funcionalidades Generadas**  
A partir de los registros de viaje (Entrada/Salida), el sistema calcula:  
1. **Afluencia por estación:** Conteo total de ingresos por punto.  
2. **Horas pico:** Franjas con mayor volumen de validaciones.  
3. **Rutas más utilizadas:** Ranking de rutas según demanda.  
4. **Patrones de viaje:** Secuencia cronológica de estaciones recorridas por usuario.  
5. **Tiempos promedio:** Duración media entre tramos de entrada y salida.  
6. **Rutas críticas:** Identificación de rutas que superan el umbral de ocupación simultánea configurado.  
7. **Benchmark de rendimiento:** Comparativa de tiempo entre procesamiento secuencial vs. paralelo (parallelStream).  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnEAAAACCAYAAAA3pIp+AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAANklEQVR4nO3OQQmAABRAsSfYxZo/kSGMYQLPJrCCNxG2BFtmZquOAAD4i3Ot7mr/egIAwGvXA4qrBdGuSdJuAAAAAElFTkSuQmCC)  
**🛠️ Estructura del Proyecto**  
src/main/java/edu/iudigital/tecnomovil/  
 ├── Main.java                 # Punto de entrada de la aplicación  
 ├── modelo/                   # Records e inmutabilidad de datos (Registro, Tramo, etc.)  
 ├── datos/                    # Catálogo de la red y generador de registros  
 ├── analisis/                 # Lógica principal, Streams y comparativa de rendimiento  
 └── reporte/                  # Formateo e impresión de resultados en consola  
   
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnEAAAACCAYAAAA3pIp+AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAANUlEQVR4nO3OQQmAABRAsSfYxKK/kJXEkyE8WcGbCFuCLTOzVXsAAPzFsVZ3dX4cAQDgvesB/vEF9H9odtUAAAAASUVORK5CYII=)  
**💡 Aplicación de Programación Funcional**  
- **Inmutabilidad:** Uso de record para el modelo y colecciones no modificables (List.of(), Stream.toList(), Map.copyOf()).  
- **Funciones Puras:** Métodos en AnalisisTransporte sin efectos secundarios.  
- **Streams & Lambdas:** Procesamiento declarativo (filter, map, sorted, collect) de grandes volúmenes de datos.  
- **Funciones de Orden Superior:** Operaciones parametrizadas mediante Function, Predicate y Supplier en Operadores y Medicion.  
- **Composición:** Combinación de predicados (ej. esEntrada().and(deRuta()).and(enFranja())).  
- **Paralelismo:** Implementación de parallelStream() para comparar tiempos de cómputo frente al modelo secuencial.  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnEAAAACCAYAAAA3pIp+AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAANElEQVR4nO3OUQmAABBAsSeILQSjXgcrmkOs4J8IW4ItM7NXZwAA/MW1Vlt1fBwBAOC9+wEukwQ+V/SggAAAAABJRU5ErkJggg==)  
**📝 Notas de Simulación**  
- Los datos se generan con una semilla fija (12345L) para garantizar la repetibilidad de las pruebas.  
- La estimación de ocupación es una simulación de volumen por eventos en el tiempo y no representa la capacidad física de vehículos reales.  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnEAAAACCAYAAAA3pIp+AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAANUlEQVR4nO3OQQmAABRAsSd4NIGRTPXNaQBrWMGbCFuCLTOzV2cAAPzFvVZbdXw9AQDgtesBhZQEOYZGgUEAAAAASUVORK5CYII=)  
**👥 Autoría**  
- **Proyecto:** TecnoMovil Data - Aplicación de Programación Funcional en Java  
- **Año:** 2026  
