package com.example.cletaeatsapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var pedidos = mutableStateOf<List<Pedido>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    init {
        loadPedidos()
    }

    private fun loadPedidos() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                pedidos.value = repository.getPedidos()
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar pedidos: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
