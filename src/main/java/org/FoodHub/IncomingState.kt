package org.FoodHub

class IncomingState : OrderState {
    override fun next(order: Order){
        order.changeState(StartedState())
    }

    override fun cancel(order: Order){
        order.changeState(CancelledState())
    }

    override fun getStatusEnum() = OrderStatus.INCOMING
}