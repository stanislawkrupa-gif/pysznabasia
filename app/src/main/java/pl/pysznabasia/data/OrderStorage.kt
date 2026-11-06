package pl.pysznabasia.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lokalny zapis historii zamówień (SharedPreferences + JSON).
 * W wersji produkcyjnej zamówienie wysyła się dodatkowo na serwer – patrz README.
 */
class OrderStorage(context: Context) {
    private val prefs = context.getSharedPreferences("orders", Context.MODE_PRIVATE)

    fun load(): List<Order> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getJSONObject(it).toOrder() }
        }.getOrDefault(emptyList())
    }

    fun save(orders: List<Order>) {
        val arr = JSONArray()
        orders.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private fun Order.toJson() = JSONObject().apply {
        put("id", id); put("number", number); put("createdAt", createdAt)
        put("subtotal", subtotal); put("deliveryFee", deliveryFee)
        put("lines", JSONArray().apply {
            lines.forEach { l ->
                put(JSONObject().apply {
                    put("productId", l.productId); put("name", l.name); put("emoji", l.emoji)
                    put("variantName", l.variantName); put("unitPrice", l.unitPrice)
                    put("quantity", l.quantity); put("note", l.note)
                })
            }
        })
        put("customer", JSONObject().apply {
            with(customer) {
                put("name", name); put("phone", phone); put("email", email)
                put("deliveryType", deliveryType.name); put("street", street); put("city", city)
                put("postalCode", postalCode); put("date", date); put("timeSlot", timeSlot)
                put("payment", payment.name); put("comments", comments)
            }
        })
    }

    private fun JSONObject.toOrder(): Order {
        val linesArr = getJSONArray("lines")
        val c = getJSONObject("customer")
        return Order(
            id = getString("id"),
            number = getString("number"),
            createdAt = getLong("createdAt"),
            subtotal = getInt("subtotal"),
            deliveryFee = getInt("deliveryFee"),
            lines = (0 until linesArr.length()).map {
                val l = linesArr.getJSONObject(it)
                OrderLine(
                    l.getString("productId"), l.getString("name"), l.getString("emoji"),
                    l.getString("variantName"), l.getInt("unitPrice"), l.getInt("quantity"), l.getString("note"),
                )
            },
            customer = CustomerDetails(
                name = c.getString("name"),
                phone = c.getString("phone"),
                email = c.getString("email"),
                deliveryType = DeliveryType.valueOf(c.getString("deliveryType")),
                street = c.getString("street"),
                city = c.getString("city"),
                postalCode = c.getString("postalCode"),
                date = c.getLong("date"),
                timeSlot = c.getString("timeSlot"),
                payment = PaymentMethod.valueOf(c.getString("payment")),
                comments = c.getString("comments"),
            ),
        )
    }

    private companion object {
        const val KEY = "orders_json"
    }
}
