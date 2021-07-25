var host = "http://localhost:8080/bda/api/";

var tablaSeleccionada = null;

var archivoSeleccionado = null;

var spinner = '<div class="loading"></div>';

var elementos = [
    "card-crear-tabla",
    "card-particionar-tabla",
    "card-info-tabla"
]

ocultarTodo = () => {
    elementos.forEach(e => {
        document.getElementById(e).style.display = 'none';
    });
    if(tablaSeleccionada == null ){
        document.getElementById("menu-acciones-tabla-items").style.display = 'none';
        document.getElementById("menu-acciones-tabla").innerHTML = 'Seleccione una tabla';
    }else{
        setTablaSeleccionada(tablaSeleccionada);
    }
};

crearTablaShow = () => {
    ocultarTodo();
    document.getElementById("card-crear-tabla").style.display = 'block';
};

particionarTablaShow = () => {
    ocultarTodo();
    document.getElementById("card-particionar-tabla").style.display = 'block';
    document.getElementById("lbl-cantidad-particiones").innerHTML = `Particionar tabla ${tablaSeleccionada}`;
};

infoTablaShow = () => {
    ocultarTodo();
    document.getElementById("card-info-tabla").style.display = 'block';
    document.getElementById("lbl-titulo-tabla").innerHTML = 'Tabla ' +  tablaSeleccionada;
    document.getElementById("lbl-info-tabla").innerHTML = spinner;
    fetch(host + `countTable/${tablaSeleccionada}`)
    .then(response => response.text())
    .then(data => {
        document.getElementById("lbl-info-tabla").innerHTML = data.split("\n").join("<br>");
    });
    //id="lbl-registros-tabla"
};

importarEnTabla = () => {
    ocultarTodo();
    document.getElementById("card-info-tabla").style.display = 'block';
    document.getElementById("lbl-titulo-tabla").innerHTML = 'Tabla ' +  tablaSeleccionada;
    document.getElementById("lbl-info-tabla").innerHTML = spinner;
    fetch(host + `insertRecords/${tablaSeleccionada}/${archivoSeleccionado}`, {method: 'POST'})
    .then(response => response.text())
    .then(data => {
        document.getElementById("lbl-info-tabla").innerHTML = data.split("\n").join("<br>");
    });
    //id="lbl-registros-tabla"
};

particionarTabla = () => {
    ocultarTodo();
    document.getElementById("card-info-tabla").style.display = 'block';
    particiones = document.getElementById("input-cantidad-particiones").value;
    document.getElementById("lbl-titulo-tabla").innerHTML = 'Tabla ' +  tablaSeleccionada;
    if(particiones.match(/^([0-9])*$/)){
        document.getElementById("lbl-info-tabla").innerHTML = spinner;
        fetch(host + `createPartitions/${tablaSeleccionada}/${particiones}`, {method: 'POST'})
        .then(response => response.text())
        .then(data => {
            document.getElementById("lbl-info-tabla").innerHTML = data.split("\n").join("<br>");
            consultarTablas(false);
        });
    }else{
        document.getElementById("lbl-info-tabla").innerHTML =
         `
            El numero de particiones debe ser un número
         `
    }
    //id="lbl-registros-tabla"
};

desParticionarTabla = () => {
    ocultarTodo();
    document.getElementById("card-info-tabla").style.display = 'block';
    document.getElementById("lbl-titulo-tabla").innerHTML = 'Tabla ' +  tablaSeleccionada;
    document.getElementById("lbl-info-tabla").innerHTML = spinner;
    fetch(host + `deletePartitions/${tablaSeleccionada}`, {method: 'POST'})
    .then(response => response.text())
    .then(data => {
        document.getElementById("lbl-info-tabla").innerHTML = data.split("\n").join("<br>");
        consultarTablas(false);
    });
};


crearTabla = () => {
    ocultarTodo();
    document.getElementById("card-info-tabla").style.display = 'block';
    nuevaTabla = document.getElementById("input-nombre-tabla").value
    document.getElementById("lbl-titulo-tabla").innerHTML = 'Tabla ' +  nuevaTabla;
    if(nuevaTabla.match(/^([A-Z]|[a-z])+([A-Z]|[a-z]|[0-9])*$/)){
        document.getElementById("lbl-info-tabla").innerHTML = spinner;
        fetch(host + `createTable/${nuevaTabla}`, {method: 'POST'})
        .then(response => response.text())
        .then(data => {
            document.getElementById("lbl-info-tabla").innerHTML = data.split("\n").join("<br>");
            consultarTablas(false);
            setTablaSeleccionada(nuevaTabla);
        });
    }else{
        document.getElementById("lbl-info-tabla").innerHTML =
         `
            El nombre para la nueva tabla no es válido<br>
            - Debe empezar con una letra<br>
            - Puede contener numeros o letras<br>
            - No puede contener caracteres especiales<br>
         
         `
    }
};

resetearTabla = () => {
    ocultarTodo();
    document.getElementById("card-info-tabla").style.display = 'block';
    document.getElementById("lbl-titulo-tabla").innerHTML = 'Tabla ' +  tablaSeleccionada;
    document.getElementById("lbl-info-tabla").innerHTML = spinner;
    fetch(host + `createTable/${tablaSeleccionada}`, {method: 'POST'})
    .then(response => response.text())
    .then(data => {
        document.getElementById("lbl-info-tabla").innerHTML = data.split("\n").join("<br>").replace("creada", "reseteada");
        consultarTablas(false);
    });
}


consultarTablas = (ocultar=true) => {
    if(ocultar) ocultarTodo();
    var menu = document.getElementById("menu-seleccionar-tablas");
    while (menu.firstChild) {
        menu.removeChild(menu.firstChild);
    }

    fetch(host + "getTables")
    .then(response => response.json())
    .then(data => {
        data.forEach(tabla => {
            console.log(`Recuperada Tabla: ${tabla}`);
            let liTabla = document.createElement("li");
            let aTabla = document.createElement("a");
            liTabla.appendChild(aTabla);
            aTabla.text = tabla;
            aTabla.id = `menu-item-tabla-${tabla.split(" ")[0]}`;
            aTabla.addEventListener("click", seleccionarTabla);
            menu.appendChild(liTabla);
        });
    });
}

consultarDocumentos = () => {
    ocultarTodo();
    var menu = document.getElementById("menu-archivos-disponibles");
    while (menu.firstChild) {
        menu.removeChild(menu.firstChild);
    }

    fetch(host + "pathInfo")
    .then(response => response.json())
    .then(data => {
        data.forEach(doc => {
            console.log(`Recuperado doc: ${doc}`);
            let liDoc = document.createElement("li");
            let aDoc = document.createElement("a");
            liDoc.appendChild(aDoc);
            aDoc.text = doc;
            aDoc.id = `menu-item-tabla-${doc.split(".")[0]}`;
            aDoc.addEventListener("click", importarDocument);
            menu.appendChild(liDoc);
        });
    });
}

seleccionarTabla = (event) => {
    setTablaSeleccionada(event.path[0].innerHTML.split(" ")[0]);
    infoTablaShow();
};

setTablaSeleccionada = (tabla) => {
    tablaSeleccionada = tabla;
    console.log(`Tabla ${tablaSeleccionada} seleccionada`);
    document.getElementById("menu-acciones-tabla").innerHTML = `Acciones sobre ${tabla}`;
    document.getElementById("menu-acciones-tabla-items").style.display = 'block';
}

importarDocument = (event) => {
    archivoSeleccionado = event.path[0].innerHTML;
    console.log(`Archivo ${archivoSeleccionado} seleccionado`);
    importarEnTabla();
}

ocultarTodo();
consultarTablas();
consultarDocumentos();