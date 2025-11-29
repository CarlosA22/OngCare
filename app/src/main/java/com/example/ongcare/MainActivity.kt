package com.example.ongcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ongcare.ui.navigation.Screen
import com.example.ongcare.ui.screen.*
import com.example.ongcare.ui.viewmodel.MainViewModel
import com.example.ongcare.ui.viewmodel.ResidentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                OngApp()
            }
        }
    }
}

@Composable
fun OngApp() {
    val navController = rememberNavController()
    
    // Instanciando ViewModels (O viewModel() gerencia o ciclo de vida automaticamente)
    val mainViewModel: MainViewModel = viewModel()
    val residentViewModel: ResidentViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        
        // 1. Dashboard
        composable(Screen.Dashboard.route) {
            val dashboardState by mainViewModel.dashboardStats.collectAsState()
            val capacity by mainViewModel.capacity.collectAsState()
            DashboardScreen(
                state = dashboardState,
                onNavigateToResidents = { navController.navigate(Screen.ResidentList.route) },
                onNavigateToIncidents = { /* Implementar tela de lista de incidentes se quiser */ },
                onNavigateToNewResident = { 
                    residentViewModel.clearSelectedResident()
                    navController.navigate(Screen.ResidentForm.createRoute())
                },
                currentCapacity = capacity,
                onUpdateCapacity = { newCap -> mainViewModel.updateCapacity(newCap) }
            )
        }

        // 2. Lista de Moradores
        composable(Screen.ResidentList.route) {
            val residents by mainViewModel.residents.collectAsState()
            ResidentListScreen(
                residents = residents,
                onResidentClick = { id -> navController.navigate(Screen.ResidentDetail.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        // 3. Formulário de Morador (Criação ou Edição)
        composable(
            route = Screen.ResidentForm.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val residentId = backStackEntry.arguments?.getString("id")
            
            LaunchedEffect(residentId) {
                if (residentId != null) {
                    residentViewModel.loadResident(residentId)
                } else {
                    residentViewModel.clearSelectedResident()
                }
            }
            val selectedResident by residentViewModel.selectedResident.collectAsState()

            ResidentFormScreen(
                residentToEdit = selectedResident,
                onSave = { resident -> 
                    residentViewModel.saveResident(resident, isEdit = residentId != null)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Rota explícita sem argumentos para criação de novo morador
        composable(route = "resident_form") {
            LaunchedEffect(Unit) { residentViewModel.clearSelectedResident() }
            val selectedResident by residentViewModel.selectedResident.collectAsState()

            ResidentFormScreen(
                residentToEdit = selectedResident,
                onSave = { resident ->
                    // Novo cadastro: sempre create
                    residentViewModel.saveResident(resident, isEdit = false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 4. Detalhes do Morador
        composable(
            route = Screen.ResidentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val residentId = backStackEntry.arguments?.getString("id") ?: return@composable
            
            LaunchedEffect(residentId) { residentViewModel.loadResident(residentId) }
            val resident by residentViewModel.selectedResident.collectAsState()

            if (resident != null) {
                ResidentDetailScreen(
                    resident = resident!!,
                    onRegisterEntry = { residentViewModel.registerEntry(residentId) },
                    onRegisterExit = { def -> residentViewModel.registerExit(residentId, def) },
                    onReportIncident = { navController.navigate(Screen.IncidentForm.createRoute(residentId)) },
                    onEdit = { navController.navigate(Screen.ResidentForm.createRoute(residentId)) },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("Carregando...")
            }
        }

        // 5. Formulário de Incidente
        composable(
            route = Screen.IncidentForm.route,
            arguments = listOf(navArgument("residentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val residentId = backStackEntry.arguments?.getString("residentId") ?: ""
            
            IncidentFormScreen(
                residentId = residentId,
                residentName = "Morador", // Simplificação didática
                onSave = { incident -> residentViewModel.saveIncident(incident) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}