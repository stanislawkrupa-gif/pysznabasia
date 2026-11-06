package pl.pysznabasia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
import pl.pysznabasia.data.CartItem
import pl.pysznabasia.data.Pricing
import pl.pysznabasia.data.formatPrice
import pl.pysznabasia.ui.components.EmojiTile
import pl.pysznabasia.ui.components.QuantityStepper
import pl.pysznabasia.ui.components.SummaryRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(vm: AppViewModel, onBrowseMenu: () -> Unit, onCheckout: () -> Unit) {
    val cart by vm.cart.collectAsStateWithLifecycle()
    val subtotal = cart.sumOf { it.total }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Koszyk") }) },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    ) { Text("Dalej • ${formatPrice(subtotal)}", style = MaterialTheme.typography.titleMedium) }
                }
            }
        },
    ) { padding ->
        if (cart.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("🧺", fontSize = 64.sp)
                Spacer(Modifier.height(12.dp))
                Text("Koszyk jest pusty", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Wybierz coś pysznego z menu Basi.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBrowseMenu) { Text("Przejdź do menu") }
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cart, key = { it.id }) { item ->
                CartItemCard(
                    item,
                    onQuantity = { vm.setQuantity(item.id, it) },
                    onRemove = { vm.remove(item.id) },
                )
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        SummaryRow("Wartość produktów", formatPrice(subtotal), bold = true)
                        Spacer(Modifier.height(4.dp))
                        val missing = Pricing.FREE_DELIVERY_FROM - subtotal
                        Text(
                            if (missing > 0)
                                "Do darmowej dostawy brakuje ${formatPrice(missing)}. Koszt dostawy: ${formatPrice(Pricing.DELIVERY_FEE)}."
                            else
                                "🎉 Masz darmową dostawę!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(item: CartItem, onQuantity: (Int) -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                EmojiTile(item.product.emoji, size = 52.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${item.variant.name} • ${formatPrice(item.variant.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (item.note.isNotBlank()) {
                        Text(
                            "${item.product.noteLabel}: „${item.note}”",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                IconButton(onClick = onRemove) { Icon(Icons.Outlined.Delete, "Usuń") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(item.quantity, onQuantity, min = item.product.minQty)
                Spacer(Modifier.weight(1f))
                Text(formatPrice(item.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
