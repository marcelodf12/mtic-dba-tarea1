# Base de datos avanzadas - MTIC
## Descripcion
La tarea requerida es simular particiones de datos en un sistema de gestión de bases de datos relacionales de código abierto (ej. MySQL, PostgreSQL etc.). Implementaremos esta tarea, de partición de datos, utilizando datos de calificaciones de películas recopilados del sitio web MovieLens. Los datos brutos están disponibles en la sección ‘recommended for education and development’ y puede accederlo a través del enlace MovieLens Latest Datasets. Este conjunto de datos posee registros de usuarios que han calificado un conjunto de películas. El usuario atribuye una calificación de cero a cinco estrellas a una película. Los datos de entrada para esta tarea serán  los localizados en el archivo ratings.dat
El archivo rating.dat contiene 10 millones de evaluaciones y 100.000 aplicaciones de etiquetas aplicadas a 10,000 películas por 72,000 usuarios. Cada línea de este archivo representa una clasificación de una película por un usuario y tiene el siguiente formato:
UserID :: MovieID :: Rating :: Timestamp
Las calificaciones se realizan en una escala de 5 estrellas, con incrementos de media estrella. Las marcas de tiempo representan los segundos desde la medianoche Hora universal coordinada (UTC) del 1 de enero de 1970.
Desarrollar el programa de simulación, en el lenguaje de programación de su preferencia, y generar el conjunto de funciones que carguen los datos de entrada en una tabla relacional, particionen la tabla utilizando algún enfoque de fragmentación horizontal e inserten nuevas tuplas en la partición correcta.

## Ejecutar en modo desarrollo
mvn spring-boot:run

## Empaquetar
mvn clean install
El comando genera una archivo jar en la carpeta target de nombre particiones-version.jar

## Ejecutar jar
java -jar particiones-0.0.1-SNAPSHOT.jar 