package com.usil.pedidosapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.usil.pedidosapp.ui.theme.PedidosAPPTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PedidosAPPTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun HomeScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Restaurante Kotlin", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.DarkGray)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, PedidosViewActivity::class.java)
                    intent.putExtra("IS_COCINERO", false)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(8.dp)
            ) { Text("Mesero") }

            Button(
                onClick = {
                    val intent = Intent(context, PedidosViewActivity::class.java)
                    intent.putExtra("IS_COCINERO", true)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(8.dp)
            ) { Text("Cocinero") }
        }
    }
}


