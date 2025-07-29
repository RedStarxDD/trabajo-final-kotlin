package com.usil.pedidosapp.data.api

import com.usil.pedidosapp.data.model.Pedido
import com.usil.pedidosapp.data.model.PedidoUpsert
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PedidoApi {
    @GET("pedidos")
    suspend fun getPedidos():List<Pedido>

    @GET("pedidos/{id}")
    suspend fun getPedidoById(@Path("id") id:Int):Pedido

    @POST("pedidos")
    suspend fun createPedido(@Body pedido: PedidoUpsert):Pedido

    @PUT("pedidos/{id}")
    suspend fun updatePedido(@Path("id") id:Int, @Body pedido: PedidoUpsert):Pedido
}