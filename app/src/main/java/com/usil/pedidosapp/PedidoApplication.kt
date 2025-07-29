package com.usil.pedidosapp

import android.app.Application
import com.usil.pedidosapp.data.api.PedidoApi
import com.usil.pedidosapp.data.repository.PedidoRepositoryImpl
import com.usil.pedidosapp.domain.repository.PedidoRepository
import com.usil.pedidosapp.viewModel.PedidoViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PedidoApplication: Application() {
    val pedidoRepository:PedidoRepository by lazy {
        val api: PedidoApi = Retrofit.Builder()
            .baseUrl("https://688478c9745306380a38590c.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PedidoApi::class.java)

        PedidoRepositoryImpl(api)
    }

    fun createPedidoViewModel(): PedidoViewModel{
        return PedidoViewModel(pedidoRepository)
    }
}