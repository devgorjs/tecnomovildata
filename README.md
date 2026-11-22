# TecnoMovil Data - EA3

**Interfaz gráfica Swing, conexión JDBC y almacenamiento de datos sintéticos en MySQL**

Esta versión incorpora una interfaz gráfica que permite probar la conexión, generar simulaciones, guardar sus registros en MySQL y consultar los datos almacenados.

## Requisitos

- JDK 21 (el proyecto está configurado para compilar con Java 21).
- MySQL Server 8.0 o superior.
- MySQL Workbench (recomendado para ejecutar el script SQL).
- IntelliJ IDEA con soporte Maven.
- Conexión a Internet la primera vez que Maven descargue MySQL Connector/J.

MySQL Connector/J es el controlador JDBC oficial necesario para que Java se comunique con MySQL. La versión configurada es 9.7.0, publicada por MySQL como versión GA en abril de 2026 y compatible con MySQL Server 8.0 o superior. Consulte la documentación oficial: https://dev.mysql.com/doc/connector-j/en/ y https://dev.mysql.com/downloads/connector/j/.

## Preparar la base de datos

1. Inicie MySQL Server.
2. Abra MySQL Workbench y conéctese al servidor.
3. Abra el archivo `sql/crear_base_datos.sql`.
4. Ejecute el script completo.
5. Compruebe que aparezcan las tablas `simulaciones` y `registros` dentro de `tecnomovil_data`.

El script crea la base de datos y sus tablas. No crea usuarios ni contraseñas; se utilizan las credenciales que ya tenga configuradas en MySQL.

## Abrir el proyecto en IntelliJ IDEA

1. Descomprima el proyecto.
2. En IntelliJ seleccione **File > Open** y elija la carpeta que contiene `pom.xml`.
3. Si IntelliJ pregunta, abra el proyecto como proyecto Maven.
4. Seleccione JDK 21 en **File > Project Structure > Project SDK**.
5. En la ventana Maven pulse **Reload All Maven Projects** para descargar MySQL Connector/J.
6. Ejecute `src/main/java/edu/iudigital/tecnomovil/Main.java`.

La clase principal completa es `edu.iudigital.tecnomovil.Main`.

## Configuración de conexión en la aplicación

La ventana inicia con estos valores de ejemplo:

| Campo | Valor |
|---|---|
| URL | `jdbc:mysql://localhost:3306/tecnomovil_data` |
| Usuario | `root` |
| Contraseña | La contraseña definida en MySQL |

Escriba las credenciales que correspondan a su instalación y pulse **Probar conexión**. El programa informa el resultado y cierra la conexión de prueba automáticamente. Si el servidor utiliza otro puerto, cambie el valor `3306` por el puerto configurado.

## Funcionalidades

### 1. Generar y guardar

- Valor inicial: 5.000 usuarios.
- Cada usuario produce una entrada y una salida.
- Total inicial esperado: 10.000 registros.
- La semilla inicial es `12345` para que la simulación pueda repetirse.
- Se puede modificar la cantidad de usuarios en la ventana (entre 1 y 100.000).
- La aplicación guarda la simulación y sus registros en una transacción. Si ocurre un error durante la inserción, se revierte la operación.
- Las inserciones se ejecutan en lotes de 500 registros.

### 2. Consultar registros

Se pueden filtrar los registros por identificador de simulación, identificador de usuario, ruta y tipo de acción. La tabla muestra hasta 500 resultados por consulta para mantener la interfaz ágil.

### 3. Estadísticas

La aplicación recupera todos los registros de la simulación seleccionada desde MySQL y reutiliza los métodos de `AnalisisTransporte` para calcular:

- Afluencia por estación.
- Horas con mayor flujo de validaciones.
- Rutas más utilizadas.
- Patrones de viaje por usuario.
- Tiempo promedio por tramo.
- Rutas que superan el umbral de ocupación simultánea.

El umbral de ocupación usado por esta pantalla es 80 y está definido en `PanelEstadisticas`. Es un valor de simulación, no la capacidad oficial de una ruta.

## Organización del código

```text
src/main/java/edu/iudigital/tecnomovil/
├── Main.java
├── MainConsola.java
├── modelo/
│   ├── Accion.java
│   ├── Registro.java
│   ├── FranjaHoraria.java
│   ├── UsoRuta.java
│   ├── Tramo.java
│   ├── RutaCritica.java
│   ├── ResultadoMedicion.java
│   ├── Simulacion.java
│   ├── RegistroPersistido.java
│   └── ResumenEstadistico.java
├── datos/
│   ├── CatalogoRed.java
│   └── GeneradorRegistros.java
├── analisis/
│   ├── Operadores.java
│   ├── AnalisisTransporte.java
│   └── Medicion.java
├── conexion/
│   ├── ConfiguracionConexion.java
│   └── ConexionMySQL.java
├── persistencia/
│   └── RegistroDAO.java
├── servicio/
│   ├── SimulacionService.java
│   ├── ConsultaService.java
│   └── EstadisticasService.java
├── interfaz/
│   ├── ConexionTester.java
│   ├── PanelGeneracion.java
│   ├── PanelConsultas.java
│   └── PanelEstadisticas.java
└── reporte/
    └── Reporte.java
```

- `modelo`: representa los datos mediante records y enum.
- `datos`: define el catálogo y genera los registros sintéticos.
- `analisis`: contiene las operaciones funcionales que calculan los indicadores.
- `conexion`: prepara y prueba la conexión JDBC con MySQL.
- `persistencia`: ejecuta las operaciones SQL para guardar y consultar datos.
- `servicio`: coordina la generación, persistencia y análisis de información.
- `interfaz`: contiene la ventana Swing y sus paneles.
- `reporte`: conserva la salida de resultados por consola de la actividad anterior.

## Decisiones importantes de diseño

- La interfaz Swing no contiene sentencias SQL. Delega las operaciones a los servicios.
- `RegistroDAO` concentra las consultas e inserciones y utiliza `PreparedStatement`.
- Las conexiones, sentencias y resultados JDBC se cierran mediante `try-with-resources`.
- La inserción de una simulación y sus registros se realiza en una transacción con `commit` y `rollback`.
- Las tareas de conexión, generación, consulta y cálculo se ejecutan con `SwingWorker` para evitar que la ventana se bloquee.
- Se mantienen los records y las colecciones no modificables del modelo y del análisis funcional.
- La contraseña se toma del `JPasswordField` y no se escribe en el código fuente ni en el repositorio.

## Volumen de datos

El valor inicial es 5.000 usuarios, equivalentes a 10.000 registros. Para una actividad académica, este volumen permite observar la generación, el guardado por lotes, las consultas y los resultados de los análisis sin exigir grandes recursos al equipo.

## Problemas frecuentes

**No se puede conectar:** compruebe que MySQL Server esté iniciado, que la base `tecnomovil_data` exista, que el puerto sea correcto y que el usuario y la contraseña sean válidos.

**No se encuentra el driver:** en IntelliJ recargue Maven (**Reload All Maven Projects**) y verifique que la dependencia `mysql-connector-j` aparezca en External Libraries.

**No existen tablas:** ejecute `sql/crear_base_datos.sql` desde MySQL Workbench.

**La consulta no muestra registros:** primero genere y guarde una simulación. Después consulte usando el ID que aparece en el mensaje de confirmación.

## Autoría

- **Proyecto:** TecnoMovil Data - EA3. Incorporando UI y BD
- **Año:** 2026  

