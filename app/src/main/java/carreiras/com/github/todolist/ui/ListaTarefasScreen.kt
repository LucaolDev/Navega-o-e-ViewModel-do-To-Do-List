package carreiras.com.github.todolist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import carreiras.com.github.todolist.data.Tarefa
import carreiras.com.github.todolist.util.formatarDataHora
import carreiras.com.github.todolist.util.statusPrazo
import carreiras.com.github.todolist.util.tarefaAtrasada
import carreiras.com.github.todolist.viewmodel.TarefaViewModel

private enum class FiltroListaTarefas {
    TODAS,
    PENDENTES,
    CONCLUIDAS
}

private enum class OrdemListaTarefas {
    PRAZO,
    RECENTES
}

@Composable
fun ListaTarefasScreen(
    viewModel: TarefaViewModel,
    onNovaTarefa: () -> Unit,
    onEditarTarefa: (Int) -> Unit
) {
    val tarefas by viewModel.tarefas.collectAsStateWithLifecycle()

    ListaTarefasContent(
        tarefas = tarefas,
        onNovaTarefa = onNovaTarefa,
        onEditarTarefa = onEditarTarefa,
        onCheckedChange = { tarefa, concluida ->
            viewModel.atualizar(tarefa.copy(concluida = concluida))
        },
        onDeletar = { tarefa -> viewModel.deletar(tarefa) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaTarefasContent(
    tarefas: List<Tarefa>,
    onNovaTarefa: () -> Unit,
    onEditarTarefa: (Int) -> Unit,
    onCheckedChange: (Tarefa, Boolean) -> Unit,
    onDeletar: (Tarefa) -> Unit
) {
    var filtro by remember { mutableStateOf(FiltroListaTarefas.TODAS) }
    var ordem by remember { mutableStateOf(OrdemListaTarefas.PRAZO) }
    var pesquisa by remember { mutableStateOf("") }
    var tarefaParaExcluir by remember { mutableStateOf<Tarefa?>(null) }
    val tarefasFiltradas = remember(tarefas, filtro, pesquisa) {
        val base = when (filtro) {
            FiltroListaTarefas.TODAS -> tarefas
            FiltroListaTarefas.PENDENTES -> tarefas.filter { !it.concluida }
            FiltroListaTarefas.CONCLUIDAS -> tarefas.filter { it.concluida }
        }

        if (pesquisa.isBlank()) {
            base
        } else {
            val termo = pesquisa.trim().lowercase()
            base.filter { tarefa ->
                tarefa.titulo.lowercase().contains(termo) ||
                    tarefa.descricao.lowercase().contains(termo)
            }
        }
    }
    val tarefasOrdenadas = remember(tarefasFiltradas, ordem) {
        when (ordem) {
            OrdemListaTarefas.PRAZO -> tarefasFiltradas.sortedWith(
                compareBy<Tarefa> { it.dataHora == null }
                    .thenBy { it.dataHora ?: Long.MAX_VALUE }
                    .thenByDescending { it.dataCriacao }
            )
            OrdemListaTarefas.RECENTES -> tarefasFiltradas.sortedByDescending { it.dataCriacao }
        }
    }
    val totalPendentes = tarefas.count { !it.concluida }
    val totalConcluidas = tarefas.count { it.concluida }
    val filtrosAtivos = pesquisa.isNotBlank() || filtro != FiltroListaTarefas.TODAS || ordem != OrdemListaTarefas.PRAZO

    if (tarefaParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { tarefaParaExcluir = null },
            title = { Text("Excluir tarefa") },
            text = { Text("Deseja realmente remover \"${tarefaParaExcluir!!.titulo}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeletar(tarefaParaExcluir!!)
                    tarefaParaExcluir = null
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { tarefaParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Minhas Tarefas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNovaTarefa) {
                Icon(Icons.Default.Add, contentDescription = "Nova tarefa")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "${tarefas.size} tarefas • ${totalPendentes} pendentes • ${totalConcluidas} concluídas",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                placeholder = { Text("Buscar tarefa") },
                trailingIcon = {
                    if (pesquisa.isNotBlank()) {
                        IconButton(onClick = { pesquisa = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                        }
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroListaTarefas.values().forEach { opcao ->
                    FilterChip(
                        selected = filtro == opcao,
                        onClick = { filtro = opcao },
                        label = {
                            Text(
                                text = when (opcao) {
                                    FiltroListaTarefas.TODAS -> "Todas"
                                    FiltroListaTarefas.PENDENTES -> "Pendentes"
                                    FiltroListaTarefas.CONCLUIDAS -> "Concluídas"
                                }
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrdemListaTarefas.values().forEach { opcao ->
                    FilterChip(
                        selected = ordem == opcao,
                        onClick = { ordem = opcao },
                        label = {
                            Text(
                                text = when (opcao) {
                                    OrdemListaTarefas.PRAZO -> "Prazo"
                                    OrdemListaTarefas.RECENTES -> "Recentes"
                                }
                            )
                        }
                    )
                }
            }

            if (filtrosAtivos) {
                TextButton(
                    onClick = {
                        pesquisa = ""
                        filtro = FiltroListaTarefas.TODAS
                        ordem = OrdemListaTarefas.PRAZO
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Limpar filtros")
                }
            }

            if (tarefasOrdenadas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("Nenhuma tarefa cadastrada neste filtro.")
                        Button(
                            onClick = onNovaTarefa,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Criar tarefa")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tarefasOrdenadas, key = { it.id }) { tarefa ->
                        TarefaItem(
                            tarefa = tarefa,
                            onCheckedChange = { concluida -> onCheckedChange(tarefa, concluida) },
                            onEditar = { onEditarTarefa(tarefa.id) },
                            onDeletar = { tarefaParaExcluir = tarefa }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarefaItem(
    tarefa: Tarefa,
    onCheckedChange: (Boolean) -> Unit,
    onEditar: () -> Unit,
    onDeletar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditar)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarefa.concluida,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarefa.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (tarefa.concluida) TextDecoration.LineThrough else TextDecoration.None
                )
                if (tarefa.descricao.isNotBlank()) {
                    Text(
                        text = tarefa.descricao,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (tarefa.dataHora == null) {
                    Text(
                        text = "Sem prazo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val atrasada = tarefaAtrasada(tarefa.dataHora, tarefa.concluida)
                    val status = statusPrazo(tarefa.dataHora, tarefa.concluida)

                    if (status != null) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (atrasada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = formatarDataHora(tarefa.dataHora),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (atrasada) MaterialTheme.colorScheme.error else Color.Unspecified,
                        fontWeight = if (atrasada) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            IconButton(onClick = onDeletar) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar tarefa")
            }
        }
    }
}

@Preview(showBackground = true, name = "Lista com tarefas")
@Composable
private fun ListaTarefasContentPreview() {
    ListaTarefasContent(
        tarefas = listOf(
            Tarefa(id = 1, titulo = "Estudar Room", descricao = "Revisar anotações e DAO", concluida = false),
            Tarefa(id = 2, titulo = "Enviar atividade", descricao = "Upload no portal da FIAP", concluida = true)
        ),
        onNovaTarefa = {},
        onEditarTarefa = {},
        onCheckedChange = { _, _ -> },
        onDeletar = {}
    )
}

@Preview(showBackground = true, name = "Lista vazia")
@Composable
private fun ListaTarefasContentVaziaPreview() {
    ListaTarefasContent(
        tarefas = emptyList(),
        onNovaTarefa = {},
        onEditarTarefa = {},
        onCheckedChange = { _, _ -> },
        onDeletar = {}
    )
}

@Preview(showBackground = true, name = "Item pendente")
@Composable
private fun TarefaItemPreview() {
    TarefaItem(
        tarefa = Tarefa(id = 1, titulo = "Estudar Room", descricao = "Revisar anotações e DAO", concluida = false),
        onCheckedChange = {},
        onEditar = {},
        onDeletar = {}
    )
}

@Preview(showBackground = true, name = "Item concluído")
@Composable
private fun TarefaItemConcluidaPreview() {
    TarefaItem(
        tarefa = Tarefa(id = 2, titulo = "Enviar atividade", descricao = "Upload no portal da FIAP", concluida = true),
        onCheckedChange = {},
        onEditar = {},
        onDeletar = {}
    )
}

@Preview(showBackground = true, name = "Item com prazo futuro")
@Composable
private fun TarefaItemComPrazoPreview() {
    val prazo = System.currentTimeMillis() + 86_400_000L
    TarefaItem(
        tarefa = Tarefa(id = 3, titulo = "Entregar atividade", descricao = "Upload no portal da FIAP", concluida = false, dataHora = prazo),
        onCheckedChange = {},
        onEditar = {},
        onDeletar = {}
    )
}

@Preview(showBackground = true, name = "Item atrasado")
@Composable
private fun TarefaItemAtrasadaPreview() {
    val prazo = System.currentTimeMillis() - 86_400_000L
    TarefaItem(
        tarefa = Tarefa(id = 4, titulo = "Enviar atividade", descricao = "Upload no portal da FIAP", concluida = false, dataHora = prazo),
        onCheckedChange = {},
        onEditar = {},
        onDeletar = {}
    )
}
