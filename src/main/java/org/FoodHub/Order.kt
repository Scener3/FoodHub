package org.FoodHub
import kotlin.jvm.JvmOverloads

/**
 * Class for storing the attributes and contents of an Order.
 *
 * Foundational Layer
 *
 */
data class Order @JvmOverloads constructor(
    /**
     * @return the list of FoodItems in an Order.
     */
    val foodItems: MutableList<FoodItem> = mutableListOf(),
    /**
     * Sets an Order's status.
     *
     * @param orderStatus - the Order's new status.
     */
    var orderStatus: OrderStatus,
    /**
     * @return the time of an Order.
     */
    val orderTime: Long,
    /**
     * @return the type of Order.
     */
    var orderType: OrderType,

    /**
     * @return the orderID of an Order.
     */
    val orderID: Int = getNextInt()
) {

    /**
     * @return the status of an Order.
     */
    var deliveryStatus: DeliveryStatus? = if (orderType == OrderType.DELIVERY)
        DeliveryStatus.PENDING else null

    /*    *//**
     * Creates a new Order with the given attributes.
     *
     * @param foodItems - the FoodItems contained in the order.
     * @param orderStatus - the status of an Order.
     * @param orderTime - the time of an Order.
     * @param orderType - the type of Order.
     */

    init {
        // This prints every time an Order is born
        println("DEBUG: Created Order ID $orderID. StackTrace:")
        // This prints "Who called me?"
        java.lang.Exception().printStackTrace()
        println("--------------------------------------------------")
    }


    /**
     * Calculates an Order's total price.
     *
     * @return the total price of an order.
     */
    fun calculateTotalPrice(): Double {
        var totalPrice = 0.0
        for (i in this.foodItems) {
            totalPrice += (i.price * i.quantity)
        }
        return totalPrice
    }

    /*    *//**
     * Displays the details of an order.
     *//*
    fun displayOrder() {
        val readableDate = DateFormatter(this.orderTime)
        val finalDateOutput = readableDate.getDate()
        val header = """
        Order ID: %d
        Order Type: %s
        Date: %s
        Price Total: ${'$'}%.2f
            Items       Quantity    Price

                """.trimIndent()
        System.out.printf(header, this.orderID, this.orderType, finalDateOutput, this.calculateTotalPrice())
        for (i in this.foodItems) {
            System.out.printf("    %-10s    %-10d$%-10.2f\n", i.name, i.getQuantity(), i.price)
        }
        println()
    }*/

    val orderTypeIcon: OrderTypeIcon
        /**
         *
         * @return OrderTypeIcon
         */
        get() = when (this.orderType) {
            OrderType.DELIVERY -> OrderTypeIcon.Delivery_Icon
            OrderType.PICKUP -> OrderTypeIcon.Pick_Up_Icon
            OrderType.TOGO -> OrderTypeIcon.To_Go_Icon
        }


    companion object {
        private var orderIDCounter = 0
        fun getNextInt(): Int {
            return orderIDCounter++
        }
    }
}

