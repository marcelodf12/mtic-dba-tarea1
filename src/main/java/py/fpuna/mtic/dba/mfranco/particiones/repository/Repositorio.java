package py.fpuna.mtic.dba.mfranco.particiones.repository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import py.fpuna.mtic.dba.mfranco.particiones.ParticionesApplication;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Repositorio {

    protected final Log logger = LogFactory.getLog(this.getClass());

    String[] HEADERS = { "userId","movieId","rating","timestamp"};

    @PersistenceContext
    EntityManager em;

    String sqlCreateMeta =  "CREATE TABLE IF NOT EXISTS PARTITION_INFO (" +
            "    TABLE_NAME varchar(32) primary key,\n" +
            "    NUM_PART   int\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
    String deleteMeta = "DELETE FROM PARTITION_INFO WHERE TABLE_NAME='##tableName##';";
    String insertMeta = "INSERT INTO PARTITION_INFO(TABLE_NAME, NUM_PART) VALUE ('##tableName##', ##num_part##)";
    String updateMeta = "UPDATE PARTITION_INFO SET NUM_PART=##num_part## WHERE TABLE_NAME = '##tableName##'";
    String sqlGetNumPart = "SELECT NUM_PART FROM PARTITION_INFO WHERE TABLE_NAME = '##tableName##';";

    String sqlCreate =  "CREATE OR REPLACE TABLE ##tableName## (" +
                        "    ID        int auto_increment primary key,\n" +
                        "    USER_ID   int,\n" +
                        "    MOVIE_ID  int,\n" +
                        "    RATING    decimal(4, 2),\n" +
                        "    TIMESTAMP varchar(16)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";

    String sqlDrop =  "DROP TABLE  ##tableName##;";

    @Transactional
    public String createTable(String tableName){
        logger.info("Creando Tabla " + tableName);
        String sql = this.sqlCreate;
        em.createNativeQuery(sqlCreateMeta).executeUpdate();
        em.createNativeQuery(sql.replaceAll("##tableName##", tableName)).executeUpdate();
        em.createNativeQuery(deleteMeta.replaceAll("##tableName##", tableName)).executeUpdate();
        em.createNativeQuery(insertMeta.replaceAll("##tableName##", tableName).replaceAll("##num_part##", "null")).executeUpdate();
        return "Tabla " +tableName+" creada correctamente";
    };

    @Transactional
    public ArrayList<String> getTables(){
        logger.info("Listar tablas");
        String sql = "SELECT CONCAT(TABLE_NAME, ' (' , IFNULL(NUM_PART,0) , ' particiones)') FROM PARTITION_INFO;";
        try{
            ArrayList<String> r = (ArrayList<String>) em.createNativeQuery(sql).getResultList();
            if(r==null) return new ArrayList<>();
            return r;
        }catch (Exception e){
            return null;
        }
    };

    public List<String>  getDocumentos(){
        ApplicationHome home = new ApplicationHome(ParticionesApplication.class);
        String path = home.getDir().getAbsolutePath()+File.separator;
        File carpeta = new File(path);
        logger.info("Los archivos csv deben estar en la ruta: " + path);
        return Arrays.stream(carpeta.list()).filter(s -> s.endsWith(".csv")).collect(Collectors.toList());
    }

    @Transactional
    public String resumen(String tableName){
        logger.info("Contar tablas");
        String sql = "Select count(*) FROM ##tableName##;";

        Integer numPart = null;
        try {
            numPart = (Integer) em.createNativeQuery(sqlGetNumPart.replaceAll("##tableName##", tableName)).getSingleResult();
        }catch (Exception e){
            return "La tabla " + tableName + " no existe";
        }

        try{
            ArrayList<String> r = new ArrayList<>();
            BigInteger count = (BigInteger) em.createNativeQuery(sql.replaceAll("##tableName##", tableName)).getSingleResult();
            r.add("-----------------------------------");
            r.add("Total de registros: " + count);

            if( numPart != null ) {
                r.add("-----------------------------------");
                for(Integer i = 0 ; i < numPart; i++){
                    String tableNamePart = tableName+"_part"+ i;
                    BigInteger countPart = (BigInteger) em.createNativeQuery(sql.replaceAll("##tableName##", tableNamePart)).getSingleResult();
                    r.add("Registros en particion " + i + ": " + countPart);
                }
            }
            r.add("-----------------------------------");

            return r.stream()
                    .map(String::toString)
                    .collect(Collectors.joining("\n" , "" , ""));

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    };

    @Transactional
    public String deletePartitions(String tableName){
        logger.info("Eliminando Particiones de Tabla " + tableName);

        Integer numPart = null;
        try {
            numPart = (Integer) em.createNativeQuery(sqlGetNumPart.replaceAll("##tableName##", tableName)).getSingleResult();
        }catch (Exception e){
            return "La tabla " + tableName + " no existe";
        }
        if( numPart == null ) {
            return "La tabla " +tableName+" no est?? particionada";
        }else{
            em.createNativeQuery(updateMeta.replaceAll("##tableName##", tableName).replaceAll("##num_part##", "null")).executeUpdate();
            for(Integer i = 0 ; i < numPart; i++){
                String tableNamePart = tableName+"_part"+ i;
                em.createNativeQuery(sqlDrop.replaceAll("##tableName##", tableNamePart)).executeUpdate();
            }
            return "Se eliminaron correctamente las particiones";
        }
    };

    @Transactional
    public String createPartitions(String tableName, Integer numPartition) {
        logger.info("Particionando Tabla " + tableName + " con " + numPartition + " particiones");

        String sql = "SELECT NUM_PART FROM PARTITION_INFO WHERE TABLE_NAME = '##tableName##';";
        Integer numPart = null;
        try {
            numPart = (Integer) em.createNativeQuery(sql.replaceAll("##tableName##", tableName)).getSingleResult();
        }catch (Exception e){
            return "La tabla " + tableName + " no existe";
        }
        if( numPart == null ) {
            em.createNativeQuery(updateMeta.replaceAll("##tableName##", tableName).replaceAll("##num_part##", numPartition.toString())).executeUpdate();
            for(Integer i = 0 ; i < numPartition; i++){
                String tableNamePart = tableName+"_part"+ i;
                String sqlCreatePart = this.sqlCreate;
                em.createNativeQuery(sqlCreatePart.replaceAll("##tableName##", tableNamePart)).executeUpdate();
                String sqlInsertPart = "INSERT INTO ##tableNamePart## SELECT * FROM ##tableName## WHERE MOD(ID, ##num_part##) = ##i##;";
                sqlInsertPart = sqlInsertPart.replaceAll("##tableNamePart##",tableNamePart);
                sqlInsertPart = sqlInsertPart.replaceAll("##tableName##",tableName);
                sqlInsertPart = sqlInsertPart.replaceAll("##num_part##",numPartition.toString());
                sqlInsertPart = sqlInsertPart.replaceAll("##i##",i.toString());
                em.createNativeQuery(sqlInsertPart).executeUpdate();
            }
        } else {
            return "La tabla " +tableName+" ya est?? particionada";
        }
        return "Se particiono la " +tableName+ " correctamente";
    }

    @Transactional
    public String insertRecords(String tableName, String filename) {
        Reader in = null;
        Integer numPart;
        Map<String, Integer> results = new HashMap<>();
        ApplicationHome home = new ApplicationHome(ParticionesApplication.class);
        String fullFilename = home.getDir().getAbsolutePath() + File.separator + filename;
        try {
            in = new FileReader(fullFilename);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return "El archivo " + fullFilename + " no existe";
        }

        try {
            numPart = (Integer) em.createNativeQuery(sqlGetNumPart.replaceAll("##tableName##", tableName)).getSingleResult();
        }catch (Exception e){
            return "La tabla " + tableName + " no existe";
        }

        try{
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader(HEADERS)
                    .withFirstRecordAsHeader()
                    .parse(in);
            for (CSVRecord record : records) {
                Integer userId = Integer.parseInt(record.get("userId"));
                Integer movieId = Integer.parseInt(record.get("movieId"));
                Number rating = Double.parseDouble(record.get("rating"));
                String timestamp = record.get("timestamp");

                String r = this.insertRecord(tableName, numPart, userId, movieId, rating, timestamp);
                Integer count = results.get(r);
                if(count == null) results.put(r, 1);
                else results.put(r, count+1);
                logger.info(r);
            }
        }catch (IOException e) {
            return "Ocurrio un error al leer el archivo o el archivo no cumple con el formato userId,movieId,rating,timestamp";
        }
        return results.entrySet().stream()
                .map(entry -> entry.getKey() + entry.getValue())
                .collect(Collectors.joining("\n" , "" , ""));
    }

    @Transactional
    public String insertNewRecord(String tableName, Integer userId, Integer movieId, Number rating) {
        String currentTimestamp = Long.toString(Instant.now().toEpochMilli());
        currentTimestamp = currentTimestamp.substring(0, currentTimestamp.length() - 3);
        Integer numPart;
        String r;
        try {
            numPart = (Integer) em.createNativeQuery(sqlGetNumPart.replaceAll("##tableName##", tableName)).getSingleResult();
        }catch (Exception e){
            return "La tabla " + tableName + " no existe";
        }
        try{
            r = this.insertRecord(tableName, numPart, userId, movieId, rating, currentTimestamp) + "\n- userId=" +userId+ "\n- movieId=" +movieId+ "\n- rating=" +rating+ "\n- timestamp="+currentTimestamp;
        }catch (Exception e) {
            return "Ocurrio un error al grabar el registro nuevo";
        }
        return r;
    }

    public String insertRecord(String tableName, Integer numPart, Integer userId, Integer movieId, Number rating, String timestamp){
        String sqlBaseInsert =  "INSERT INTO ##tableName##(ID, USER_ID, MOVIE_ID, RATING, TIMESTAMP)" +
                "VALUES (##id##, ##userId##, ##movieId##, ##rating##, '##timestamp##');";

        //Tabla principal
        String sql = sqlBaseInsert;
        sql = sql.replaceAll("##tableName##",tableName);
        sql = sql.replaceAll("##userId##",userId.toString());
        sql = sql.replaceAll("##movieId##",movieId.toString());
        sql = sql.replaceAll("##rating##",rating.toString());
        sql = sql.replaceAll("##timestamp##",timestamp);
        sql = sql.replaceAll("##id##","null");
        try {
            em.createNativeQuery(sql).executeUpdate();
            BigInteger id = (BigInteger) em.createNativeQuery("SELECT LAST_INSERT_ID();").getSingleResult();

            if(numPart != null ){
                // Particion
                BigInteger part = id.mod(new BigInteger(String.valueOf(numPart)));
                String tableNamePart = tableName + "_part" + part;
                sql = sqlBaseInsert;
                sql = sql.replaceAll("##tableName##", tableNamePart);
                sql = sql.replaceAll("##userId##", userId.toString());
                sql = sql.replaceAll("##movieId##", movieId.toString());
                sql = sql.replaceAll("##rating##", rating.toString());
                sql = sql.replaceAll("##timestamp##", timestamp);
                sql = sql.replaceAll("##id##", id.toString());
                try {
                    em.createNativeQuery(sql).executeUpdate();
                }catch (Exception e){
                    return "Registros insertado en la tabla " + tableName + " pero no se encontr?? la tabla de partici??n ";
                }
                return "Registros insertados en la particion " + tableNamePart + ": ";
            }else{
                return "Registros insertado en la tabla " + tableName + ": ";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "Registros duplicados: ";
        }
    };

}