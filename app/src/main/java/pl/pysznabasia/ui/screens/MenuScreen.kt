package pl.pysznabasia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.pysznabasia.AppViewModel
import pl.pysznabasia.data.Category
import pl.pysznabasia.data.MenuRepository
import pl.pysznabasia.data.Pricing
import pl.pysznabasia.data.Product
import pl.pysznabasia.data.formatPrice
import pl.pysznabasia.ui.components.EmojiTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(vm: AppViewModel, onProductClick: (String) -> Unit) {
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val products = remember(selectedCategory, query) {
        MenuRepository.products.filter { p ->
            (selectedCategory == null || p.category.name == selectedCategory) &&
                (query.isBlank() || p.name.contains(query, ignoreCase = true) ||
                    p.description.contains(query, ignoreCase = true))
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("Pyszna Basia", fontWeight = FontWeight.Bold)
                    Text(
                        "Catering • Zimne płyty • Torty • Ciasta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { HeroBanner() }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Szukaj: sernik, pierogi, tort…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, "Wyczyść")
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Wszystko") },
                        )
                    }
                    items(Category.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = if (selectedCategory == cat.name) null else cat.name },
                            label = { Text("${cat.emoji} ${cat.title}") },
                        )
                    }
                }
            }
            if (products.isEmpty()) {
                item {
                    Text(
                        "Nic nie znaleźliśmy. Spróbuj innej nazwy 🙂",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    onQuickAdd = {
                        if (product.needsOptions) {
                            onProductClick(product.id)
                        } else {
                            vm.addToCart(product, product.variants.first(), 1)
                            Toast.makeText(context, "Dodano: ${product.name}", Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HeroBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Domowa kuchnia na każdą okazję",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Komunie, urodziny, spotkania firmowe i rodzinne obiady. " +
                    "Dostawa gratis od ${formatPrice(Pricing.FREE_DELIVERY_FROM)}.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Zamówienia: dania i catering min. 1 dzień wcześniej, torty min. 2 dni.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit, onQuickAdd: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiTile(product.emoji)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                val prefix = if (product.variants.size > 1) "od " else ""
                val suffix = if (product.minQty > 1) " / os. (min. ${product.minQty})" else ""
                Text(
                    prefix + formatPrice(product.fromPrice) + suffix,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            FilledIconButton(onClick = onQuickAdd) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj do koszyka")
            }
        }
    }
}
