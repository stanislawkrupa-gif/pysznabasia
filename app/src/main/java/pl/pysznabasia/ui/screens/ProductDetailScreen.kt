package pl.pysznabasia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.pysznabasia.AppViewModel
import pl.pysznabasia.data.Category
import pl.pysznabasia.data.MenuRepository
import pl.pysznabasia.data.formatPrice
import pl.pysznabasia.ui.components.EmojiTile
import pl.pysznabasia.ui.components.QuantityStepper
import pl.pysznabasia.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String, vm: AppViewModel, onBack: () -> Unit) {
    val product = remember(productId) { MenuRepository.find(productId) }
    val context = LocalContext.current

    var variantIndex by rememberSaveable { mutableIntStateOf(0) }
    var quantity by rememberSaveable { mutableIntStateOf(product?.minQty ?: 1) }
    var note by rememberSaveable { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(product?.category?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć")
                    }
                },
            )
        },
        bottomBar = {
            if (product != null) {
                val total = product.variants[variantIndex].price * quantity
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = {
                            vm.addToCart(product, product.variants[variantIndex], quantity, note)
                            Toast.makeText(context, "Dodano do koszyka", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    ) {
                        Text("Dodaj do koszyka • ${formatPrice(total)}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
    ) { padding ->
        if (product == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nie znaleziono produktu")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            EmojiTile(product.emoji, size = 120.dp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(product.description, style = MaterialTheme.typography.bodyLarge)

            if (product.leadDays > 1 || product.category == Category.CAKES) {
                Spacer(Modifier.height(8.dp))
                AssistChip(onClick = {}, label = { Text("⏰ Zamów min. ${product.leadDays} dni wcześniej") })
            }

            if (product.variants.size > 1) {
                SectionTitle("Wybierz wariant")
                product.variants.forEachIndexed { index, variant ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = index == variantIndex,
                                onClick = { variantIndex = index },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = index == variantIndex, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(variant.name, Modifier.weight(1f))
                        Text(formatPrice(variant.price), fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    "${product.variants.first().name}: ${formatPrice(product.variants.first().price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            SectionTitle(product.qtyLabel)
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(quantity, { quantity = it }, min = product.minQty)
                if (product.minQty > 1) {
                    Spacer(Modifier.width(12.dp))
                    Text("min. ${product.minQty}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (product.allowsNote) {
                SectionTitle(product.noteLabel)
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 120) note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(if (product.category == Category.CAKES) "np. Sto lat, Kasiu!" else "Opcjonalnie")
                    },
                    supportingText = { Text("${note.length}/120") },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
