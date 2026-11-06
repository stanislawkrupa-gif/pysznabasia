package pl.pysznabasia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.pysznabasia.data.CartItem
import pl.pysznabasia.data.CustomerDetails
import pl.pysznabasia.data.MenuRepository
import pl.pysznabasia.data.Order
import pl.pysznabasia.data.OrderLine
import pl.pysznabasia.data.OrderStorage
import pl.pysznabasia.data.Pricing
import pl.pysznabasia.data.Product
import pl.pysznabasia.data.Variant
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = OrderStorage(app)

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _orders = MutableStateFlow(storage.load())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun addToCart(product: Product, variant: Variant, quantity: Int, note: String = "") {
        val cleanNote = note.trim()
        _cart.update { items ->
            val existing = items.firstOrNull {
                it.product.id == product.id && it.variant == variant && it.note == cleanNote
            }
            if (existing != null) {
                items.map { if (it.id == existing.id) it.copy(quantity = it.quantity + quantity) else it }
            } else {
                items + CartItem(UUID.randomUUID().toString(), product, variant, quantity, cleanNote)
            }
        }
    }

    fun setQuantity(itemId: String, quantity: Int) {
        _cart.update { items -> items.map { if (it.id == itemId) it.copy(quantity = quantity) else it } }
    }

    fun remove(itemId: String) {
        _cart.update { items -> items.filterNot { it.id == itemId } }
    }

    /** Najwcześniejsza możliwa data realizacji – zależy od produktów w koszyku. */
    fun earliestDate(): LocalDate {
        val lead = _cart.value.maxOfOrNull { it.product.leadDays } ?: 1
        return LocalDate.now().plusDays(lead.toLong())
    }

    fun placeOrder(customer: CustomerDetails): Order {
        val items = _cart.value
        val subtotal = items.sumOf { it.total }
        val order = Order(
            id = UUID.randomUUID().toString(),
            number = "PB-" + Random.nextInt(100_000, 999_999),
            createdAt = System.currentTimeMillis(),
            lines = items.map {
                OrderLine(it.product.id, it.product.name, it.product.emoji, it.variant.name,
                    it.variant.price, it.quantity, it.note)
            },
            customer = customer,
            subtotal = subtotal,
            deliveryFee = Pricing.deliveryFee(subtotal, customer.deliveryType),
        )
        // TODO: tu wysłać zamówienie do backendu (np. Retrofit POST /orders) przed zapisem lokalnym.
        _orders.update { listOf(order) + it }
        storage.save(_orders.value)
        _cart.value = emptyList()
        return order
    }

    /** Dodaje do koszyka ponownie produkty z wcześniejszego zamówienia (po aktualnych cenach). */
    fun reorder(order: Order) {
        order.lines.forEach { line ->
            val product = MenuRepository.find(line.productId) ?: return@forEach
            val variant = product.variants.firstOrNull { it.name == line.variantName } ?: return@forEach
            addToCart(product, variant, line.quantity, line.note)
        }
    }
}
