package com.usil.pedidosapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usil.pedidosapp.data.model.Pedido
import com.usil.pedidosapp.data.model.PedidoUpsert
import com.usil.pedidosapp.domain.repository.PedidoRepository
import com.usil.pedidosapp.utils.ResultUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PedidoViewModel (private val pedidoRepository: PedidoRepository): ViewModel() {
    // Estados privado (Estas solo se puede acceder desde la misma clase)
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    // upsert = create || update
    private val _upsertResult = MutableStateFlow<ApiPedidoResult?>(null)
    private val _selectedPedido = MutableStateFlow<Pedido?>(null)

    // Estados publicos
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val upsertResult: StateFlow<ApiPedidoResult?> = _upsertResult.asStateFlow()
    val selectedPedido: StateFlow<Pedido?> = _selectedPedido.asStateFlow()

    fun getPedidos() {
        // bloque para ejecutar tareas async
        viewModelScope.launch {
            _isLoading.value=true
            _error.value=null
            when(val result = pedidoRepository.getPedidos()){
                is ResultUtil.Success->{
                    println(result.data)
                    _pedidos.value=result.data
                }
                is ResultUtil.Error->{
                    _error.value=result.message
                }
                is ResultUtil.Loading->{

                }
            }
            _isLoading.value=false
        }
    }

    fun createPedido(
        nombreMesero: String,
        numMesa: Int,
        comidas: List<String>,
        cantidad: List<Int>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val pedidoRequest = PedidoUpsert(
                nombreMesero = nombreMesero,
                numMesa = numMesa,
                comidas = comidas,
                cantidad = cantidad,
                listoParaEntregar = false
            )
            when (val result = pedidoRepository.createPedido(pedidoRequest)) {
                is ResultUtil.Success -> {
                    val currentPedido = _pedidos.value.toMutableList()
                    currentPedido.add(result.data)
                    _pedidos.value = currentPedido

                    _upsertResult.value = ApiPedidoResult.Success(result.data)
                }

                is ResultUtil.Error -> {
                    _error.value = result.message
                    _upsertResult.value = ApiPedidoResult.Error(result.message)
                }

                is ResultUtil.Loading -> {

                }
            }
            _isLoading.value = false
        }
    }

    fun updatePedido(pedidoId:Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _upsertResult.value=null

            val pedido = fetchPedidoById(pedidoId)

            if (pedido == null) {
                _error.value = "Pedido no encontrado"
                _isLoading.value = false
                return@launch
            }

            val pedidoRequest = PedidoUpsert(
                numMesa = pedido.numOrden,
                nombreMesero = pedido.nombreMesero,
                comidas = pedido.comidas,
                cantidad = pedido.cantidad,
                listoParaEntregar = true
            )

            when (val result = pedidoRepository.updatePedido(pedidoId, pedidoRequest)) {
                is ResultUtil.Success -> {
                    val currentTodos = _pedidos.value.toMutableList()
                    currentTodos.add(result.data)
                    _pedidos.value = currentTodos

                    _upsertResult.value = ApiPedidoResult.Success(result.data)
                }

                is ResultUtil.Error -> {
                    _error.value = result.message
                    _upsertResult.value = ApiPedidoResult.Error(result.message)
                }

                is ResultUtil.Loading -> {
                    // TODO: Replace loading logic
                }
            }
            _isLoading.value = false
        }
    }

    private suspend fun fetchPedidoById(id: Int): Pedido? {
        return when (val result = pedidoRepository.getPedidoById(id)) {
            is ResultUtil.Success -> result.data
            is ResultUtil.Error -> {
                _error.value = result.message
                null
            }
            else -> null
        }
    }

    fun getPedidoById(id: Int) {
        viewModelScope.launch {
            _isLoading.value=true
            _error.value=null

            when(val result=pedidoRepository.getPedidoById(id)){
                is ResultUtil.Success->{
                    _selectedPedido.value=result.data
                }
                is ResultUtil.Error->{
                    _error.value=result.message
                }
                is ResultUtil.Loading->{

                }
            }
            _isLoading.value=false
        }
    }

    fun refreshPedidos() {
        getPedidos()
    }
}

sealed class ApiPedidoResult {
    data class Success(val pedido: Pedido) : ApiPedidoResult()
    data class Error(val message: String) : ApiPedidoResult()
}