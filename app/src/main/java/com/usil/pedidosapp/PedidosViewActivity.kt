package com.usil.pedidosapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.usil.pedidosapp.ui.theme.PedidosAPPTheme
import com.usil.pedidosapp.viewModel.PedidoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.usil.pedidosapp.data.model.Pedido
import com.usil.pedidosapp.viewModel.ApiPedidoResult

class PedidosViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // vamos a recibir el valor de la pantalla anterior
        val isCocinero = intent.getBooleanExtra("IS_COCINERO", false)

        val app=application as PedidoApplication
        val pedidoViewModel=app.createPedidoViewModel()

        setContent {
            PedidosAPPTheme {
                PedidosAppView(isCocinero, viewModel = pedidoViewModel)
            }
        }
    }
}

@Composable
fun PedidosAppView(isCocinero: Boolean, viewModel: PedidoViewModel = viewModel()) {
    // Llamar a las variables del viewModel
    val pedidos by viewModel.pedidos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val upsertResult by viewModel.upsertResult.collectAsState()
    val selectedPedido by viewModel.selectedPedido.collectAsState()

    // Unit -> Para se ejecute solo la primera vez
    LaunchedEffect(Unit) {
        viewModel.getPedidos()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (pedidos.isNotEmpty()) {
                    viewModel.refreshPedidos()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(upsertResult) {
        upsertResult?.let { result ->
            when (result) {
                is ApiPedidoResult.Success -> {
                    var message = "Pedido creado de forma exitosa"

                    if (isCocinero) {
                        message = "Pedido completado de forma exitosa"
                    }

                    Toast.makeText(context, message, Toast.LENGTH_SHORT)
                        .show()
                    viewModel.refreshPedidos()
                }

                is ApiPedidoResult.Error -> {
                    val message = "Hubo un error ${result.message}"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    PedidosAppContent(
        pedidos = pedidos,
        isLoading = isLoading,
        error = error,
        context = context,
        onCreatePedido = { nombreMesero, numMesa, listaComidas, listaCantidad ->
            viewModel.createPedido(nombreMesero, numMesa, listaComidas, listaCantidad)
        },
        onUpdatePedido = {numOrden ->
            viewModel.updatePedido(numOrden)
        },
        isCocinero = isCocinero
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosAppContent(
    pedidos: List<Pedido>,
    isLoading: Boolean,
    error: String?,
    context: Context,
    onCreatePedido: (
        nombreMesero:String,
        numMesa:Int,
        listaComidas:List<String>,
        listaCantidad:List<Int>) -> Unit,
    onUpdatePedido:(
            numOrden:Int) -> Unit,
    isCocinero: Boolean
) {
    // Estados locales
    var nombreMesero by remember { mutableStateOf("") }
    var numMesa by remember { mutableIntStateOf(0) }
    var comidas by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    //showDialog=true

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Lista de pedidos (" + if(isCocinero)  "Cocinero" else "Mesero" + ")",
                        color = Color.White)
                        },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.DarkGray),
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        // pasar a un valor a otra pantalla
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isCocinero) {
                FloatingActionButton(onClick = {
                    showDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar pedido"
                    )
                }
            }
        }
    ) { paddingValues -> PedidosList(
        paddingValues, 
        pedidos, 
        isLoading, 
        error,
        isCocinero,
        onChangeCocinero = {
            numOrden -> onUpdatePedido(numOrden)
        })
    }

    if (showDialog) {
        FormUpsertView(
            nombreMesero,
            numMesa,
            comidas,
            cantidad,
            isError,
            onNombreMeseroChange = {nombreMesero=it},
            onNumMesaChange = {numMesa=it},
            onComidasChange = {comidas=it},
            onCantidadChange = {cantidad=it},
            onIsError = { isError = it },
            onSaveTask = {
                val listaComidas:List<String> = comidas
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                val listaCantidad:List<Int> = cantidad
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }

                onCreatePedido(nombreMesero, numMesa, listaComidas, listaCantidad)
                nombreMesero=""
                numMesa=0
                comidas=""
                cantidad=""
                showDialog=false
            },
            onShowDialogChange = {showDialog = it}
        )
    }
}

@Composable
fun FormUpsertView(
    nombreMesero:String,
    numMesa:Int,
    comidas:String,
    cantidad:String,
    isError: Boolean,
    onNombreMeseroChange: (String) -> Unit,
    onNumMesaChange: (Int) -> Unit,
    onComidasChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit,
    onIsError: (Boolean) -> Unit,
    onSaveTask: () -> Unit,
    onShowDialogChange: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onShowDialogChange(false) },
        title = { Text("Nuevo pedido") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombreMesero,
                    onValueChange = { newValue->
                        onNombreMeseroChange(newValue)
                        onIsError(newValue.isBlank())
                    },
                    label = { Text("Mesero") },
                    placeholder = {
                        Text("Ingrese el nombre del mesero")
                    },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "El nombre es requerido",
                                color = Color.Red
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = numMesa.toString(),
                    onValueChange = { newValue->
                        val numero = newValue.toIntOrNull()
                        if (numero != null) onNumMesaChange(numero)
                        onIsError(newValue.isBlank())
                    },
                    label = { Text("N° Mesa")
                    },
                    placeholder = {
                        Text("Ingrese el número de mesa")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "La mesa es requerida",
                                color = Color.Red
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = comidas,
                    onValueChange = { newValue ->
                        onComidasChange(newValue)
                        onIsError(newValue.isBlank())
                    },
                    label = { Text("Platos (sep. comas)") },
                    placeholder = {
                        Text("Ingrese las comidas separadas por comas (,)")
                    },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Las comidas son requeridas",
                                color = Color.Red
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { newValue ->
                        onCantidadChange(newValue)
                        onIsError(newValue.isBlank())
                    },
                    label = { Text("Cantidades (sep. comas)") },
                    placeholder = {
                        Text("Ingrese las cantidades separadas por comas (,)")
                    },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Las cantidades son requeridas",
                                color = Color.Red
                            )
                        }
                    }

                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSaveTask()
            },
                colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.confirm),
                ),
                enabled = nombreMesero.isNotBlank() && numMesa>0 && comidas.isNotBlank() && cantidad.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onShowDialogChange(false)
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.cancel),
                ),
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PedidosAppPreview() {
    val samplePedidos = listOf(
        Pedido("Juan", 1, listOf("Ceviche", "Arroz con pollo"), listOf(1, 1), false, 1),
        Pedido("Maria", 1, listOf("Lomo saltado", "Vaso de maracuyá"), listOf(1, 2), true, 2)
    )

    PedidosAppContent(
        pedidos = samplePedidos,
        isLoading = false,
        error = null,
        context = LocalContext.current,
        onCreatePedido = { _, _, _, _ -> },
        onUpdatePedido = {_ -> },
        false
    )
}

@Composable
fun PedidosList(
    paddingValues: PaddingValues = PaddingValues(12.dp),
    pedidos: List<Pedido>,
    isLoading: Boolean,
    error: String?,
    isCocinero: Boolean,
    onChangeCocinero: (Int) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = 10.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> {
                    item {
                        Column {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando pedidos...")
                        }
                    }
                }

                error != null -> {
                    item {
                        Column {
                            Text(
                                text = "Error al obtener los pedidos",
                                color = Color.Red,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> {
                    items(pedidos.filter { !it.listoParaEntregar }) { pedido ->
                        PedidoCard(pedido, 
                            isCocinero, 
                            onChangeCocinero = {numOrden ->
                                onChangeCocinero(numOrden)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: Pedido,
    isCocinero:Boolean,
    onChangeCocinero: (Int) -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Mesero: ${pedido.nombreMesero}", color = Color.White)
            Text("Mesa: ${pedido.numMesa}", color = Color.White)
            Text("Orden #${pedido.numOrden}", color = Color.White)

            Spacer(Modifier.height(8.dp))

            Text("Platos:", color = Color.White, style = MaterialTheme.typography.labelMedium)

            //El zip combina dos listas en una sola lista de pares, más seguro que el for
            pedido.comidas.zip(pedido.cantidad).forEach { (comida, cantidad) ->
                Text(
                    text = "• $comida x$cantidad",
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
            if(isCocinero){
                Button(
                    onClick = {
                        onChangeCocinero(pedido.numOrden)
                    },
                    modifier =Modifier.fillMaxWidth().padding(top = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.confirm)
                    ),
                ) {
                    Text("Marcar como completado")
                }
            }
        }
    }
}