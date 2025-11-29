package com.example.ongcare.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ongcare.data.model.Incident
import com.example.ongcare.data.model.Resident
import com.example.ongcare.data.model.ResidentStatus
import com.example.ongcare.ui.viewmodel.DashboardUiState

// --- COMPONENTES REUTILIZÁVEIS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(title: String, canNavigateBack: Boolean, onBack: () -> Unit = {}) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}

// --- TELA 1: DASHBOARD ---

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigateToResidents: () -> Unit,
    onNavigateToIncidents: () -> Unit,
    onNavigateToNewResident: () -> Unit,
    currentCapacity: Int,
    onUpdateCapacity: (Int) -> Unit
) {
    Scaffold(topBar = { SimpleTopBar("ONG Care - Painel", false) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cards de Estatísticas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsCard("Acolhidos", state.activeResidents.toString(), Modifier.weight(1f), Color(0xFFE3F2FD))
                StatsCard("Vagas Livres", state.availableSpots.toString(), Modifier.weight(1f), Color(0xFFE8F5E9))
            }

            // Ajuste de Capacidade
            CapacityEditor(
                capacity = currentCapacity,
                onSave = onUpdateCapacity
            )

            Text("Acesso Rápido", style = MaterialTheme.typography.titleMedium)

            Button(onClick = onNavigateToNewResident, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cadastrar Novo Morador")
            }

            OutlinedButton(onClick = onNavigateToResidents, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver Lista de Moradores")
            }

            OutlinedButton(onClick = onNavigateToIncidents, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver Incidentes Recentes")
            }
        }
    }
}

@Composable
fun StatsCard(title: String, value: String, modifier: Modifier, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.displayMedium)
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CapacityEditor(capacity: Int, onSave: (Int) -> Unit) {
    val ctx = LocalContext.current
    var text by remember(capacity) { mutableStateOf(capacity.toString()) }
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Capacidade Máxima", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    // permite apenas dígitos
                    text = input.filter { it.isDigit() }
                },
                singleLine = true,
                label = { Text("Quantidade máxima de acolhidos") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val value = text.toIntOrNull()
                    if (value == null) {
                        Toast.makeText(ctx, "Informe um número válido", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(value)
                        Toast.makeText(ctx, "Capacidade atualizada para $value", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Salvar Capacidade")
                }
            }
        }
    }
}

// --- TELA 2: LISTA DE MORADORES ---

@Composable
fun ResidentListScreen(
    residents: List<Resident>,
    onResidentClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = { SimpleTopBar("Moradores", true, onBack) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(residents) { resident ->
                ListItem(
                    modifier = Modifier.clickable { onResidentClick(resident.id) },
                    headlineContent = { Text(resident.name) },
                    supportingContent = { Text("Status: ${resident.status}") },
                    leadingContent = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                )
                Divider()
            }
        }
    }
}

// --- TELA 3: CADASTRO/EDIÇÃO DE MORADOR ---

@Composable
fun ResidentFormScreen(
    residentToEdit: Resident?, // Se null, é cadastro novo
    onSave: (Resident) -> Unit,
    onBack: () -> Unit
) {
    // Estados do formulário
    var name by remember { mutableStateOf(residentToEdit?.name ?: "") }
    var nickname by remember { mutableStateOf(residentToEdit?.nickname ?: "") }
    var health by remember { mutableStateOf(residentToEdit?.healthConditions ?: "") }
    var meds by remember { mutableStateOf(residentToEdit?.medications ?: "") }
    var contactName by remember { mutableStateOf(residentToEdit?.contactName ?: "") }
    var contactPhone by remember { mutableStateOf(residentToEdit?.contactPhone ?: "") }

    val context = LocalContext.current

    Scaffold(topBar = { SimpleTopBar(if (residentToEdit == null) "Novo Morador" else "Editar Morador", true, onBack) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Apelido") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = health, onValueChange = { health = it }, label = { Text("Condições de Saúde") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = meds, onValueChange = { meds = it }, label = { Text("Medicações") }, modifier = Modifier.fillMaxWidth())
            
            Text("Contato de Emergência", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top=8.dp))
            OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Nome do Contato") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val newResident = residentToEdit?.copy(
                            name = name, nickname = nickname, healthConditions = health,
                            medications = meds, contactName = contactName, contactPhone = contactPhone
                        ) ?: Resident(
                            name = name, nickname = nickname, birthDate = "01/01/1980", document = null,
                            healthConditions = health, medications = meds, contactName = contactName, contactPhone = contactPhone
                        )
                        onSave(newResident)
                        onBack()
                    } else {
                        Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }
}

// --- TELA 4: DETALHES DO MORADOR ---

@Composable
fun ResidentDetailScreen(
    resident: Resident,
    onRegisterEntry: () -> Unit,
    onRegisterExit: (Boolean) -> Unit,
    onReportIncident: () -> Unit,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Registrar Saída") },
            text = { Text("Esta saída é temporária ou definitiva (egresso)?") },
            confirmButton = {
                TextButton(onClick = { onRegisterExit(true); showExitDialog = false }) { Text("Definitiva") }
            },
            dismissButton = {
                TextButton(onClick = { onRegisterExit(false); showExitDialog = false }) { Text("Temporária") }
            }
        )
    }

    Scaffold(
        topBar = { SimpleTopBar("Detalhes", true, onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            
            // Cabeçalho
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(resident.name, style = MaterialTheme.typography.headlineSmall)
                    Text("Vulgo: ${resident.nickname}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    
                    val statusColor = when(resident.status) {
                        ResidentStatus.ATIVO -> Color.Green
                        ResidentStatus.EGRESSO -> Color.Red
                        ResidentStatus.EXTERNO -> Color.Blue
                    }
                    Text("Situação: ${resident.status}", color = statusColor, style = MaterialTheme.typography.labelLarge)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Ações Rápidas
            Text("Ações", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRegisterEntry, enabled = resident.status != ResidentStatus.ATIVO, modifier = Modifier.weight(1f)) {
                    Text("Entrada")
                }
                Button(onClick = { showExitDialog = true }, enabled = resident.status == ResidentStatus.ATIVO, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))) {
                    Text("Saída")
                }
            }
            Button(onClick = onReportIncident, modifier = Modifier.fillMaxWidth().padding(top=8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                Icon(Icons.Default.Warning, null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar Incidente")
            }

            Spacer(Modifier.height(16.dp))
            Text("Saúde e Cuidados", style = MaterialTheme.typography.titleMedium)
            Text("Condições: ${resident.healthConditions}")
            Text("Remédios: ${resident.medications}")
        }
    }
}

// --- TELA 5: FORMULÁRIO DE INCIDENTE ---

@Composable
fun IncidentFormScreen(
    residentId: String,
    residentName: String, // Opcional, passado para mostrar na tela
    onSave: (Incident) -> Unit,
    onBack: () -> Unit
) {
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }

    Scaffold(topBar = { SimpleTopBar("Novo Incidente", true, onBack) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Morador: $residentName", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (Ex: Briga, Saúde)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("O que aconteceu?") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = action, onValueChange = { action = it }, label = { Text("Ação tomada") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = {
                onSave(Incident(residentId = residentId, residentName = residentName, type = type, description = description, actionTaken = action))
                onBack()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar Incidente")
            }
        }
    }
}