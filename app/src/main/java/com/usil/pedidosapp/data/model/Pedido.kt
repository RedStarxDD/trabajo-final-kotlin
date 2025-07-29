package com.usil.pedidosapp.data.model

data class Pedido(
    val nombreMesero: String,
    val numMesa: Int,
    val comidas: List<String>,
    val cantidad: List<Int>,
    val listoParaEntregar: Boolean,
    val numOrden: Int
)

data class PedidoUpsert(
    val nombreMesero: String,
    val numMesa: Int,
    val comidas: List<String>,
    val cantidad: List<Int>,
    val listoParaEntregar: Boolean
)
