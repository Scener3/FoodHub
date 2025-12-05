package org.FoodHub

interface OrderState {
    // Moves to the next valid state
    fun next(order: Order)

    // Cancels the order
    fun cancel(order: Order)

    // Gets the enum
    fun getStatusEnum() : OrderStatus
}