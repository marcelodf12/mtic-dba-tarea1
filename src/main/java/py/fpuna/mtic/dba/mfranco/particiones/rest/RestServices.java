package py.fpuna.mtic.dba.mfranco.particiones.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import py.fpuna.mtic.dba.mfranco.particiones.repository.Repositorio;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "**")
@RequestMapping("api/")
public class RestServices {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    Repositorio repositorio;


    @PostMapping
    @RequestMapping("createTable/{tableName}")
    public ResponseEntity<String> createTable(@PathVariable("tableName") String tableName){
        String r = this.repositorio.createTable(tableName);
        return ResponseEntity.ok().body(r);
    }

    @PostMapping
    @RequestMapping("createPartitions/{tableName}/{numPartition}")
    public ResponseEntity<String> createPartitions(
            @PathVariable("tableName") String tableName,
            @PathVariable("numPartition") Integer numPartition
    ){
        String r = this.repositorio.createPartitions(tableName, numPartition);
        return ResponseEntity.ok().body(r);
    }

    @PostMapping
    @RequestMapping("deletePartitions/{tableName}")
    public ResponseEntity<String> deletePartitions(
            @PathVariable("tableName") String tableName
    ){
        String r = this.repositorio.deletePartitions(tableName);
        return ResponseEntity.ok().body(r);
    }

    @PostMapping
    @RequestMapping("insertRecords/{tableName}/{fileName}")
    public ResponseEntity<String> insertRecords(
            @PathVariable("tableName") String tableName,
            @PathVariable("fileName") String fileName
    ){
        String r = this.repositorio.insertRecords(tableName, fileName);
        return ResponseEntity.ok().body(r);
    }

    @GetMapping
    @RequestMapping("getDocuments")
    public ResponseEntity<List<String>> getDocuments(){
        List<String> lista = this.repositorio.getDocumentos();
        return ResponseEntity.ok().body(lista);
    }

    @GetMapping
    @RequestMapping("getTables")
    public ResponseEntity<ArrayList<String>> getTables(){
        return ResponseEntity.ok().body(this.repositorio.getTables());
    }

    @GetMapping
    @RequestMapping("resumen/{tableName}")
    public ResponseEntity<String> resumen(
            @PathVariable("tableName") String tableName
    ){
        return ResponseEntity.ok().body(this.repositorio.resumen(tableName));
    }

    @PostMapping
    @RequestMapping("insertRecord/{tableName}")
    public ResponseEntity<String> insertRecord(
            @PathVariable("tableName") String tableName,
            @RequestParam(name = "userId") Integer userId,
            @RequestParam(name = "movieId") Integer movieId,
            @RequestParam(name = "rating") Number rating
    ){
        String r = this.repositorio.insertNewRecord(tableName, userId, movieId, rating);
        return ResponseEntity.ok().body(r);
    }

}
