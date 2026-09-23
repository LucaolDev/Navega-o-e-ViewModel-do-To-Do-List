package carreiras.com.github.todolist.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatarDataHora(millis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
    return formato.format(Date(millis))
}

// O DatePicker do Material3 representa a data escolhida em milissegundos UTC
// (meia-noite do dia, fuso UTC), independente do fuso do dispositivo.
fun extrairDataDoDatePicker(millisUtc: Long): Triple<Int, Int, Int> {
    val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millisUtc }
    return Triple(
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH),
        calendario.get(Calendar.DAY_OF_MONTH)
    )
}

fun paraMillisUtcDoDatePicker(ano: Int, mes: Int, dia: Int): Long {
    val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendario.clear()
    calendario.set(ano, mes, dia)
    return calendario.timeInMillis
}

fun combinarDataHora(ano: Int, mes: Int, dia: Int, hora: Int, minuto: Int): Long {
    val calendario = Calendar.getInstance()
    calendario.clear()
    calendario.set(ano, mes, dia, hora, minuto)
    return calendario.timeInMillis
}

fun tarefaAtrasada(dataHora: Long?, concluida: Boolean, agora: Long = System.currentTimeMillis()): Boolean {
    return dataHora != null && !concluida && dataHora < agora
}

fun statusPrazo(dataHora: Long?, concluida: Boolean, agora: Long = System.currentTimeMillis()): String? {
    if (dataHora == null || concluida) return null

    val calendarioAgora = Calendar.getInstance().apply { timeInMillis = agora }
    val calendarioPrazo = Calendar.getInstance().apply { timeInMillis = dataHora }

    val mesmoDia = calendarioAgora.get(Calendar.YEAR) == calendarioPrazo.get(Calendar.YEAR) &&
        calendarioAgora.get(Calendar.DAY_OF_YEAR) == calendarioPrazo.get(Calendar.DAY_OF_YEAR)

    return when {
        dataHora < agora -> "Atrasada"
        mesmoDia -> "Vence hoje"
        else -> null
    }
}
