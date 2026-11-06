package pl.pysznabasia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.pysznabasia.AppViewModel
import pl.pysznabasia.data.*
import pl.pysznabasia.ui.components.SectionTitle
import pl.pysznabasia.ui.components.SummaryRow
import java.time.LocalDate
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(vm: AppViewModel, onBack: () -> Unit, onOrderPlaced: (Order) -> Unit) {
    val cart by vm.cart.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var deliveryType by rememberSaveable { mutableStateOf(DeliveryType.DELIVERY) }
    var street by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var postal by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf<Long?>(null) }
    var slot by rememberSaveable { mutableStateOf<String?>(null) }
    var payment by rememberSaveable { mutableStateOf(PaymentMethod.BLIK) }
    var comments by rememberSaveable { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val earliest = remember(cart) { vm.earliestDate() }
    val dates = remember(earliest) { (0L until 21L).map { earliest.plusDays(it) } }
    // Jeśli koszyk zmienił się tak, że wybrana data jest za wczesna – wyzeruj ją.
    LaunchedEffect(earliest) { if (date != null && date!! < earliest.toEpochDay()) date = null }

    val subtotal = cart.sumOf { it.total }
    val fee = Pricing.deliveryFee(subtotal, deliveryType)
    val isDelivery = deliveryType == DeliveryType.DELIVERY

    val phoneDigits = phone.filter(Char::isDigit).let { if (it.length == 11 && it.startsWith("48")) it.drop(2) else it }
    val errName = name.isBlank()
    val errPhone = phoneDigits.length != 9
    val errEmail = email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val errStreet = isDelivery && street.isBlank()
    val errCity = isDelivery && city.isBlank()
    val errPostal = isDelivery && !Regex("""\d{2}-\d{3}""").matches(postal.trim())
    val errDate = date == null
    val errSlot = slot == null
    val errConsent = !consent
    val hasErrors = listOf(errName, errPhone, errEmail, errStreet, errCity, errPostal, errDate, errSlot, errConsent).any { it }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Zamówienie") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć") } },
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) Surface(tonalElevation = 3.dp) {
                Column(Modifier.padding(16.dp)) {
                    if (showErrors && hasErrors) {
                        Text(
                            "Uzupełnij zaznaczone pola.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Button(
                        onClick = {
                            showErrors = true
                            if (!hasErrors) {
                                val order = vm.placeOrder(
                                    CustomerDetails(
                                        name = name.trim(),
                                        phone = phoneDigits,
                                        email = email.trim(),
                                        deliveryType = deliveryType,
                                        street = if (isDelivery) street.trim() else "",
                                        city = if (isDelivery) city.trim() else "",
                                        postalCode = if (isDelivery) postal.trim() else "",
                                        date = date!!,
                                        timeSlot = slot!!,
                                        payment = payment,
                                        comments = comments.trim(),
                                    )
                                )
                                onOrderPlaced(order)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Text("Zamawiam i płacę • ${formatPrice(subtotal + fee)}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
    ) { padding ->
        if (cart.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Koszyk jest pusty")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // ---- Dane kontaktowe ----
            SectionTitle("Dane kontaktowe")
            FormField(name, { name = it }, "Imię i nazwisko*", showErrors && errName, "Podaj imię i nazwisko")
            FormField(
                phone, { phone = it }, "Telefon*", showErrors && errPhone, "Podaj 9-cyfrowy numer",
                keyboardType = KeyboardType.Phone,
            )
            FormField(
                email, { email = it }, "E-mail (potwierdzenie)", showErrors && errEmail, "Nieprawidłowy e-mail",
                keyboardType = KeyboardType.Email,
            )

            // ---- Sposób odbioru ----
            SectionTitle("Sposób odbioru")
            DeliveryType.entries.forEach { type ->
                RadioRow(
                    selected = deliveryType == type,
                    onClick = { deliveryType = type },
                    label = type.label,
                    trailing = when {
                        type == DeliveryType.PICKUP -> "0 zł"
                        Pricing.deliveryFee(subtotal, type) == 0 -> "gratis"
                        else -> formatPrice(Pricing.DELIVERY_FEE)
                    },
                )
            }
            if (isDelivery) {
                FormField(street, { street = it }, "Ulica i numer*", showErrors && errStreet, "Podaj adres")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormField(
                        postal, { if (it.length <= 6) postal = it }, "Kod*", showErrors && errPostal, "00-000",
                        keyboardType = KeyboardType.Number, modifier = Modifier.weight(0.4f),
                    )
                    FormField(
                        city, { city = it }, "Miejscowość*", showErrors && errCity, "Podaj miasto",
                        modifier = Modifier.weight(0.6f),
                    )
                }
            } else {
                Text(
                    "Odbiór: Pracownia Pysznej Basi, ul. Piekarska 7 (adres przykładowy).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ---- Termin ----
            SectionTitle("Termin realizacji")
            Text(
                "Najwcześniej: ${earliest.dayOfMonth}.${"%02d".format(earliest.monthValue)} " +
                    "(zależy od produktów w koszyku)",
                style = MaterialTheme.typography.bodySmall,
                color = if (showErrors && errDate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dates) { d ->
                    DateChip(d, selected = date == d.toEpochDay(), onClick = { date = d.toEpochDay() })
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Godzina",
                style = MaterialTheme.typography.bodySmall,
                color = if (showErrors && errSlot) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TIME_SLOTS) { s ->
                    FilterChip(selected = slot == s, onClick = { slot = s }, label = { Text(s) })
                }
            }

            // ---- Płatność ----
            SectionTitle("Płatność")
            PaymentMethod.entries.forEach { method ->
                RadioRow(selected = payment == method, onClick = { payment = method }, label = method.label)
            }

            // ---- Uwagi ----
            SectionTitle("Uwagi do zamówienia")
            OutlinedTextField(
                value = comments,
                onValueChange = { if (it.length <= 300) comments = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("np. kod do domofonu, alergie, prośby") },
                minLines = 2,
            )

            // ---- Podsumowanie ----
            SectionTitle("Podsumowanie")
            Card {
                Column(Modifier.padding(16.dp)) {
                    cart.forEach {
                        SummaryRow("${it.quantity} × ${it.product.name} (${it.variant.name})", formatPrice(it.total))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    SummaryRow("Produkty", formatPrice(subtotal))
                    SummaryRow("Dostawa", if (fee == 0) "0 zł" else formatPrice(fee))
                    SummaryRow("Do zapłaty", formatPrice(subtotal + fee), bold = true)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .toggleable(value = consent, onValueChange = { consent = it }, role = Role.Checkbox),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = consent, onCheckedChange = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Akceptuję regulamin i politykę prywatności*",
                    color = if (showErrors && errConsent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorText: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        supportingText = { if (isError) Text(errorText) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.padding(vertical = 2.dp),
    )
}

@Composable
private fun RadioRow(selected: Boolean, onClick: () -> Unit, label: String, trailing: String? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(label, Modifier.weight(1f))
        if (trailing != null) Text(trailing, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChip(date: LocalDate, selected: Boolean, onClick: () -> Unit) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, PL).replaceFirstChar { it.uppercase() }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Column(Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dayName, style = MaterialTheme.typography.labelSmall)
                Text(
                    "${date.dayOfMonth}.${"%02d".format(date.monthValue)}",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}
