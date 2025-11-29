package com.example.ongcare.data.repository

import com.example.ongcare.data.model.Incident
import com.example.ongcare.data.model.Resident
import com.example.ongcare.data.model.ResidentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Object em Kotlin cria um Singleton (uma única instância para todo o app)
object OngRepository {
    
    // Capacidade do abrigo (Regra de Negócio)
    private const val MAX_CAPACITY = 20

    // Listas em memória (StateFlow permite que a UI reaja a mudanças automaticamente)
    private val _residents = MutableStateFlow<List<Resident>>(emptyList())
    val residents: StateFlow<List<Resident>> = _residents.asStateFlow()

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    // --- MÉTODOS DE MORADORES ---

    fun addResident(resident: Resident): Boolean {
        // Regra: Só adiciona se tiver vaga (para ativos)
        val activeCount = _residents.value.count { it.status == ResidentStatus.ATIVO }
        if (resident.status == ResidentStatus.ATIVO && activeCount >= MAX_CAPACITY) {
            return false // Sem vaga
        }
        
        _residents.update { currentList ->
            currentList + resident
        }
        return true
    }

    fun updateResident(updatedResident: Resident) {
        _residents.update { list ->
            list.map { if (it.id == updatedResident.id) updatedResident else it }
        }
    }

    fun getResidentById(id: String): Resident? {
        return _residents.value.find { it.id == id }
    }
    
    // Regra: Check-in (Entrada)
    fun registerEntry(residentId: String): Boolean {
        val activeCount = _residents.value.count { it.status == ResidentStatus.ATIVO }
        if (activeCount >= MAX_CAPACITY) return false
        
        val resident = getResidentById(residentId) ?: return false
        updateResident(resident.copy(status = ResidentStatus.ATIVO))
        return true
    }

    // Regra: Check-out (Saída)
    fun registerExit(residentId: String, isDefinitive: Boolean) {
        val resident = getResidentById(residentId) ?: return
        val newStatus = if (isDefinitive) ResidentStatus.EGRESSO else ResidentStatus.EXTERNO
        updateResident(resident.copy(status = newStatus))
    }

    // --- MÉTODOS DE INCIDENTES ---

    fun addIncident(incident: Incident) {
        _incidents.update { it + incident }
    }
    
    fun getIncidentsByResident(residentId: String): List<Incident> {
        return _incidents.value.filter { it.residentId == residentId }
    }

    // --- DASHBOARD ---
    
    fun getOccupancy(): Pair<Int, Int> {
        val occupied = _residents.value.count { it.status == ResidentStatus.ATIVO }
        return Pair(occupied, MAX_CAPACITY - occupied)
    }
}