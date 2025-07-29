package com.usil.pedidosapp.domain.repository

import com.usil.pedidosapp.data.model.Pedido
import com.usil.pedidosapp.data.model.PedidoUpsert
import com.usil.pedidosapp.utils.ResultUtil

interface PedidoRepository {
    suspend fun getPedidos(): ResultUtil<List<Pedido>>
    suspend fun getPedidoById(id:Int):ResultUtil<Pedido>
    suspend fun createPedido(pedido: PedidoUpsert):ResultUtil<Pedido>
    suspend fun updatePedido(id:Int, pedido: PedidoUpsert):ResultUtil<Pedido>
}