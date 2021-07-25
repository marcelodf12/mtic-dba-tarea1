package py.fpuna.mtic.dba.mfranco.particiones.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import py.fpuna.mtic.dba.mfranco.particiones.ParticionesApplication;
import py.fpuna.mtic.dba.mfranco.particiones.repository.Repositorio;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "**")
@RequestMapping("api/")
public class RestServices {

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
    @RequestMapping("pathInfo")
    public ResponseEntity<List<String>> pathInfo(){
        ApplicationHome home = new ApplicationHome(ParticionesApplication.class);
        String path = home.getSource().getAbsolutePath();
        File carpeta = new File(path);
        List<String> lista = Arrays.stream(carpeta.list()).filter(s -> s.endsWith(".csv")).collect(Collectors.toList());
        return ResponseEntity.ok().body(lista);
    }

    @GetMapping
    @RequestMapping("getTables")
    public ResponseEntity<ArrayList<String>> getTables(){
        return ResponseEntity.ok().body(this.repositorio.getTables());
    }

    @GetMapping
    @RequestMapping("countTable/{tableName}")
    public ResponseEntity<String> countTables(
            @PathVariable("tableName") String tableName
    ){
        return ResponseEntity.ok().body(this.repositorio.countTables(tableName));
    }

}
