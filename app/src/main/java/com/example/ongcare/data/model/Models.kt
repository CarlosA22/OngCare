package com.example.ongcare.data.model

import java.util.UUID

// Enum para situação do morador
enum class ResidentStatus {
    ATIVO, // Está no abrigo
    EGRESSO, // Saiu definitivamente
    EXTERNO // Em acompanhamento mas não dorme no abrigo
}

// Modelo do Morador
data class Resident(
    val id: String = UUID.randomUUID().toString(), // Gera ID único automático
    val name: String,
    val nickname: String,
    val birthDate: String, // Simplificado como String para facilitar MVP
    val document: String?, // Nullable, pois pode não ter
    val contactName: String,
    val contactPhone: String,
    val healthConditions: String,
    val medications: String,
    val status: ResidentStatus = ResidentStatus.ATIVO,
    val photoUrl: String? = null
)

// Modelo do Incidente
data class Incident(
    val id: String = UUID.randomUUID().toString(),
    val residentId: String,
    val residentName: String, // Guardar o nome facilita exibição sem joins complexos
    val type: String, // Ex: "Conflito", "Saúde"
    val description: String,
    val actionTaken: String,
    val timestamp: Long = System.currentTimeMillis()
)