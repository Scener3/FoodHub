package org.FoodHub

class CancelledState : OrderState {

    override fun next(order: Order) {
        println("Order cancelled cannot reprocess")
    }

    override fun cancel(order: Order) {
        println("Already cancelled")
    }

    override fun getStatusEnum() = OrderStatus.CANCELLED
}