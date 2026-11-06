package pl.pysznabasia.data

import java.util.Locale

enum class Category(val title: String, val emoji: String) {
    CATERING("Catering", "🍽️"),
    COLD_PLATTERS("Zimne płyty", "🧀"),
    HOT_DISHES("Dania gorące", "🍲"),
    CAKES("Torty", "🎂"),
    PASTRIES("Ciasta", "🍰"),
}

/** Wariant produktu, np. rozmiar tortu. Cena w groszach. */
data class Variant(val name: String, val price: Int)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val category: Category,
    val emoji: String,
    val variants: List<Variant>,
    val minQty: Int = 1,
    val qtyLabel: String = "Ilość",
    val allowsNote: Boolean = false,
    val noteLabel: String = "Uwagi do produktu",
    /** Ile dni wcześniej trzeba złożyć zamówienie. */
    val leadDays: Int = 1,
) {
    val fromPrice: Int get() = variants.minOf { it.price }
    val needsOptions: Boolean get() = variants.size > 1 || allowsNote || minQty > 1
}

data class CartItem(
    val id: String,
    val product: Product,
    val variant: Variant,
    val quantity: Int,
    val note: String,
) {
    val total: Int get() = variant.price * quantity
}

enum class DeliveryType(val label: String) {
    DELIVERY("Dostawa pod adres"),
    PICKUP("Odbiór osobisty"),
}

enum class PaymentMethod(val label: String) {
    BLIK("BLIK"),
    CARD("Karta przy odbiorze"),
    CASH("Gotówka przy odbiorze"),
    TRANSFER("Przelew tradycyjny"),
}

data class CustomerDetails(
    val name: String,
    val phone: String,
    val email: String,
    val deliveryType: DeliveryType,
    val street: String,
    val city: String,
    val postalCode: String,
    /** Data realizacji jako LocalDate.toEpochDay(). */
    val date: Long,
    val timeSlot: String,
    val payment: PaymentMethod,
    val comments: String,
)

data class OrderLine(
    val productId: String,
    val name: String,
    val emoji: String,
    val variantName: String,
    val unitPrice: Int,
    val quantity: Int,
    val note: String,
) {
    val total: Int get() = unitPrice * quantity
}

data class Order(
    val id: String,
    val number: String,
    val createdAt: Long,
    val lines: List<OrderLine>,
    val customer: CustomerDetails,
    val subtotal: Int,
    val deliveryFee: Int,
) {
    val total: Int get() = subtotal + deliveryFee
}

object Pricing {
    const val DELIVERY_FEE = 15_00
    const val FREE_DELIVERY_FROM = 300_00

    fun deliveryFee(subtotal: Int, type: DeliveryType): Int =
        if (type == DeliveryType.PICKUP || subtotal >= FREE_DELIVERY_FROM) 0 else DELIVERY_FEE
}

val TIME_SLOTS = listOf("8:00–10:00", "10:00–12:00", "12:00–14:00", "14:00–16:00", "16:00–18:00", "18:00–20:00")

val PL: Locale = Locale.forLanguageTag("pl-PL")

/** 12950 -> "129,50 zł" */
fun formatPrice(grosze: Int): String = String.format(PL, "%,d,%02d zł", grosze / 100, grosze % 100)
    .replace(' ', ' ')
