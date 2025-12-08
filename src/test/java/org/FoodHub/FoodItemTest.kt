package org.FoodHub

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FoodItemTest {

    @Test
    fun `constructor stores all values correctly`() {
        val item = FoodItem(
            name = "Apple",
            quantity = 5,
            price = 0.99
        )

        Assertions.assertEquals("Apple", item.name)
        Assertions.assertEquals(5, item.quantity)
        Assertions.assertEquals(0.99, item.price)
    }
}