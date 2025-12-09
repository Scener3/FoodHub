package org.FoodHub

class CompletedState : OrderState{
    override fun next(order: Order) {
        println("Order Completed")
    }

    override fun cancel(order: Order) {
        println("Order Already Completed")
    }

    override fun getStatusEnum() = OrderStatus.COMPLETED
}