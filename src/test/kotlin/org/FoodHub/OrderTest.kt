package org.FoodHub

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderTest {

    @BeforeEach
    fun resetCounter() {
        val counterField = Order::class.java.getDeclaredField("orderIDCounter")
        counterField.isAccessible = true
        counterField.setInt(null, 0)
    }


    @Test
    fun `order initializes with correct values`() {
        val order = Order(
            foodItems = mutableListOf(FoodItem("Apple", 2, 1.0)),
            _status = OrderStatus.INCOMING,
            orderTime = 12345L,
            orderType = OrderType.DELIVERY
        )

        Assertions.assertEquals(OrderType.DELIVERY, order.orderType)
        Assertions.assertEquals(DeliveryStatus.PENDING, order.deliveryStatus)
        Assertions.assertEquals(OrderStatus.INCOMING, order.orderStatus)
        Assertions.assertEquals(0, order.orderID)
    }

    @Test
    fun `calculateTotalPrice adds up each item price times quantity`() {
        val order = Order(
            foodItems = mutableListOf(
                FoodItem("Apple", 2, 1.5),
                FoodItem("Banana", 3, 2.0)
            ),
            _status = OrderStatus.INCOMING,
            orderTime = 0L,
            orderType = OrderType.PICKUP
        )

        Assertions.assertEquals(9.0, order.calculateTotalPrice())
    }

    @Test
    fun `orderTypeIcon returns correct icon`() {
        val deliveryOrder = Order(mutableListOf(), OrderStatus.INCOMING, 0L, OrderType.DELIVERY)
        Assertions.assertEquals(OrderTypeIcon.Delivery_Icon, deliveryOrder.orderTypeIcon)

        val pickupOrder = Order(mutableListOf(), OrderStatus.INCOMING, 0L, OrderType.PICKUP)
        Assertions.assertEquals(OrderTypeIcon.Pick_Up_Icon, pickupOrder.orderTypeIcon)

        val toGoOrder = Order(mutableListOf(), OrderStatus.INCOMING, 0L, OrderType.TOGO)
        Assertions.assertEquals(OrderTypeIcon.To_Go_Icon, toGoOrder.orderTypeIcon)
    }

    @Test
    fun `updateDeliveryStatus sets value correctly`() {
        val order = Order(
            mutableListOf(),
            OrderStatus.INCOMING,
            0L,
            OrderType.DELIVERY
        )

        order.updateDeliveryStatus(DeliveryStatus.PENDING)
        Assertions.assertEquals(DeliveryStatus.PENDING, order.deliveryStatus)
    }

    @Test
    fun `updateStatus changes underlying state and status enum`() {
        val order = Order(
            mutableListOf(),
            OrderStatus.INCOMING,
            0L,
            OrderType.TOGO
        )

        order.updateStatus(OrderStatus.STARTED)
        Assertions.assertEquals(OrderStatus.STARTED, order.orderStatus)

        order.updateStatus(OrderStatus.COMPLETED)
        Assertions.assertEquals(OrderStatus.COMPLETED, order.orderStatus)
    }

    @Test
    fun `changeState updates state object and enum`() {
        val order = Order(
            mutableListOf(),
            OrderStatus.INCOMING,
            0L,
            OrderType.TOGO
        )

        val newState = StartedState()
        order.changeState(newState)

        Assertions.assertEquals(OrderStatus.STARTED, order.orderStatus)
        Assertions.assertSame(newState, order.state)
    }

    @Test
    fun `proceedToNextStep delegates to state implementation`() {
        val order = Order(
            mutableListOf(),
            OrderStatus.INCOMING,
            0L,
            OrderType.PICKUP
        )

        order.proceedToNextStep()
        Assertions.assertEquals(OrderStatus.STARTED, order.orderStatus)
    }

    @Test
    fun `cancelOrder delegates to state implementation`() {
        val order = Order(
            mutableListOf(),
            OrderStatus.INCOMING,
            0L,
            OrderType.PICKUP
        )

        order.cancelOrder()

        Assertions.assertEquals(OrderStatus.CANCELLED, order.orderStatus)
    }

    @Test
    fun `ID increments for each new order`() {
        val a = Order(mutableListOf(), OrderStatus.INCOMING, 1L, OrderType.PICKUP)
        val b = Order(mutableListOf(), OrderStatus.INCOMING, 2L, OrderType.PICKUP)

        Assertions.assertEquals(0, a.orderID)
        Assertions.assertEquals(1, b.orderID)
    }
}