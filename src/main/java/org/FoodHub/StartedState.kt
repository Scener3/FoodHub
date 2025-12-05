package org.FoodHub

class StartedState : OrderState {
    override fun next(order: Order) {
        order.changeState(CompletedState())
    }

    override fun cancel(order: Order) {
        order.changeState(CancelledState())
    }

    override fun getStatusEnum() = OrderStatus.STARTED
}