package com.example.ongcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ongcare.data.model.Incident
import com.example.ongcare.data.model.Resident
import com.example.ongcare.data.model.ResidentStatus
import com.example.ongcare.data.repository.OngRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel para lista e dashboard
class MainViewModel : ViewModel() {
    // Repository acessado diretamente (Singleton)
    private val repository = OngRepository

    // Converte o fluxo do repositório para um estado que a UI observa
    val residents: StateFlow<List<Resident>> = repository.residents
    val incidents: StateFlow<List<Incident>> = repository.incidents
    val capacity: StateFlow<Int> = repository.capacity

    // Estado derivado para o Dashboard
    val dashboardStats = residents
        .combine(incidents) { resList, incList ->
            Pair(resList, incList)
        }
        .combine(capacity) { (resList, incList), cap ->
            val active = resList.count { it.status == ResidentStatus.ATIVO }
            DashboardUiState(
                activeResidents = active,
                availableSpots = (cap - active).coerceAtLeast(0),
                recentIncidents = incList.takeLast(3).size // Exemplo simples
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun updateCapacity(newCapacity: Int) {
        repository.setCapacity(newCapacity)
    }
}

data class DashboardUiState(
    val activeResidents: Int = 0,
    val availableSpots: Int = 0,
    val recentIncidents: Int = 0
)

// ViewModel para lidar com cadastro e detalhes de morador
class ResidentViewModel : ViewModel() {
    private val repository = OngRepository

    private val _selectedResident = MutableStateFlow<Resident?>(null)
    val selectedResident: StateFlow<Resident?> = _selectedResident

    fun loadResident(id: String) {
        _selectedResident.value = repository.getResidentById(id)
    }

    fun clearSelectedResident() {
        _selectedResident.value = null
    }

    fun saveResident(resident: Resident, isEdit: Boolean) {
        if (isEdit) {
            repository.updateResident(resident)
        } else {
            repository.addResident(resident)
        }
    }

    fun registerEntry(id: String) = repository.registerEntry(id)
    
    fun registerExit(id: String, definitive: Boolean) = repository.registerExit(id, definitive)
    
    fun saveIncident(incident: Incident) = repository.addIncident(incident)
}