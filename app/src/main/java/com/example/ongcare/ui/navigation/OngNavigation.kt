package com.example.ongcare.ui.navigation

// Define as rotas do aplicativo como objetos para evitar erros de digitação (type-safety básico)
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ResidentList : Screen("resident_list")
    object ResidentForm : Screen("resident_form?id={id}") {
        // Função auxiliar para criar a rota com argumento
        fun createRoute(id: String? = null) = if (id != null) "resident_form?id=$id" else "resident_form"
    }
    object ResidentDetail : Screen("resident_detail/{id}") {
        fun createRoute(id: String) = "resident_detail/$id"
    }
    object IncidentList : Screen("incident_list")
    object IncidentForm : Screen("incident_form?residentId={residentId}") {
        fun createRoute(residentId: String) = "incident_form?residentId=$residentId"
    }
}