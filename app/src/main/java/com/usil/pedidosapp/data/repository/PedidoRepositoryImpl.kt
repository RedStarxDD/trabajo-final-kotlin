package com.usil.pedidosapp.data.repository

import com.usil.pedidosapp.data.api.PedidoApi
import com.usil.pedidosapp.data.model.Pedido
import com.usil.pedidosapp.data.model.PedidoUpsert
import com.usil.pedidosapp.domain.repository.PedidoRepository
import com.usil.pedidosapp.utils.ResultUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PedidoRepositoryImpl(private val apiService:PedidoApi): PedidoRepository {
    override suspend fun getPedidos(): ResultUtil<List<Pedido>> = withContext((Dispatchers.IO)) {
        try {
            val response=apiService.getPedidos()
            ResultUtil.Success(response)
        }catch (e:Exception){
            ResultUtil.Error(message = "Error en el servidor", throwable = e)
        }
    }

    override suspend fun getPedidoById(id: Int): ResultUtil<Pedido> = withContext((Dispatchers.IO)) {
        try {
            val response=apiService.getPedidoById(id)

            ResultUtil.Success(response)
        }catch (e:Exception){
            ResultUtil.Error(message = "Error en el servidor", throwable = e)
        }
    }

    override suspend fun createPedido(pedido: PedidoUpsert): ResultUtil<Pedido> = withContext((Dispatchers.IO)) {
        try {
            val response=apiService.createPedido(pedido)

            ResultUtil.Success(response)
        }catch (e:Exception){
            ResultUtil.Error(message = "Error en el servidor", throwable = e)
        }
    }

    override suspend fun updatePedido(id:Int, pedido: PedidoUpsert): ResultUtil<Pedido> = withContext((Dispatchers.IO)) {
        try {
            val response=apiService.updatePedido(id, pedido)

            ResultUtil.Success(response)
        }catch (e:Exception){
            ResultUtil.Error(message = "Error en el servidor", throwable = e)
        }
    }
}