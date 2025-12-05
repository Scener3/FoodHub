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
    private var _status: OrderStatus,
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
    var orderID: Int = getNextInt()

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


    private fun mapEnumToState(status : OrderStatus) : OrderState{
        return when(status){
            OrderStatus.INCOMING -> IncomingState()
            OrderStatus.STARTED -> StartedState()
            OrderStatus.COMPLETED -> CompletedState()
            OrderStatus.CANCELLED -> CancelledState()
        }
    }

    @Transient
    var state: OrderState = mapEnumToState(_status)
        private set

    val orderStatus: OrderStatus
        get() = state.getStatusEnum()

    fun changeState(newState : OrderState){
        this.state = newState
        this._status = newState.getStatusEnum()
    }

    fun proceedToNextStep(){
        state.next(this)
    }

    fun cancelOrder(){
        state.cancel(this)
    }

    fun updateStatus(newStatus : OrderStatus){
        val newStateObject = mapEnumToState(newStatus)
        changeState(newStateObject)
    }


    companion object {
        private var orderIDCounter = 0
        fun getNextInt(): Int {
            return orderIDCounter++
        }
    }
}

