package pl.pysznabasia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.pysznabasia.AppViewModel
import pl.pysznabasia.data.DeliveryType
import pl.pysznabasia.data.Order
import pl.pysznabasia.data.PL
import pl.pysznabasia.data.PaymentMethod
import pl.pysznabasia.data.formatPrice
import pl.pysznabasia.ui.components.SummaryRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", PL)
private val createdFormat = DateTimeFormatter.ofPattern("d.MM.yyyy, HH:mm", PL)

private fun Order.realizationDate(): String = LocalDate.ofEpochDay(customer.date).format(dateFormat)
private fun Order.createdText(): String =
    Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).format(createdFormat)

private enum class Status(val label: String) { NEW("Przyjęte"), DONE("Zrealizowane") }
private fun Order.status(): Status =
    if (LocalDate.ofEpochDay(customer.date).isBefore(LocalDate.now())) Status.DONE else Status.NEW

@Composable
fun ConfirmationScreen(vm: AppViewModel, orderId: String, onBackToMenu: () -> Unit, onShowOrders: () -> Unit) {
    val orders by vm.orders.collectAsStateWithLifecycle()
    val order = orders.firstOrNull { it.id == orderId }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text("🎉", fontSize = 72.sp)
        Text(
            "Dziękujemy za zamówienie!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (order != null) {
            Text("Numer: ${order.number}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Basia potwierdzi zamówienie telefonicznie pod numerem ${order.customer.phone}." +
                    if (order.customer.payment == PaymentMethod.TRANSFER)
                        " Dane do przelewu wyślemy SMS-em." else "",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OrderDetailsCard(order)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBackToMenu, modifier = Modifier.fillMaxWidth()) { Text("Wróć do menu") }
        OutlinedButton(onClick = onShowOrders, modifier = Modifier.fillMaxWidth()) { Text("Moje zamówienia") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(vm: AppViewModel, onReordered: () -> Unit) {
    val orders by vm.orders.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Moje zamówienia") }) },
    ) { padding ->
        if (orders.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("📋", fontSize = 64.sp)
                Text("Nie masz jeszcze zamówień", style = MaterialTheme.typography.titleLarge)
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(orders, key = { it.id }) { order ->
                val isExpanded = expanded == order.id
                Card(onClick = { expanded = if (isExpanded) null else order.id }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(order.number, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f))
                            val status = order.status()
                            AssistChip(
                                onClick = {},
                                label = { Text(status.label) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (status == Status.NEW) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            )
                        }
                        Text(
                            "${order.lines.joinToString { it.emoji }}  •  ${order.lines.sumOf { it.quantity }} poz.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "Na: ${order.realizationDate()}, ${order.customer.timeSlot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatPrice(order.total), style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            TextButton(onClick = { vm.reorder(order); onReordered() }) { Text("Zamów ponownie") }
                        }
                        if (isExpanded) {
                            Spacer(Modifier.height(8.dp))
                            OrderDetailsCard(order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsCard(order: Order) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            order.lines.forEach { l ->
                SummaryRow("${l.emoji} ${l.quantity} × ${l.name} (${l.variantName})", formatPrice(l.total))
                if (l.note.isNotBlank()) {
                    Text("   „${l.note}”", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SummaryRow("Produkty", formatPrice(order.subtotal))
            SummaryRow("Dostawa", if (order.deliveryFee == 0) "0 zł" else formatPrice(order.deliveryFee))
            SummaryRow("Razem", formatPrice(order.total), bold = true)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            val c = order.customer
            Text("Termin: ${order.realizationDate()}, ${c.timeSlot}", style = MaterialTheme.typography.bodySmall)
            Text(
                if (c.deliveryType == DeliveryType.DELIVERY) "Dostawa: ${c.street}, ${c.postalCode} ${c.city}"
                else "Odbiór osobisty",
                style = MaterialTheme.typography.bodySmall,
            )
            Text("Płatność: ${c.payment.label}", style = MaterialTheme.typography.bodySmall)
            if (c.comments.isNotBlank()) Text("Uwagi: ${c.comments}", style = MaterialTheme.typography.bodySmall)
            Text("Złożono: ${order.createdText()}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
