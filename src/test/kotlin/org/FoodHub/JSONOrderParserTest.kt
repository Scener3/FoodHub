package org.FoodHub

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class JSONOrderParserTest {

    private lateinit var tempDir: File

    @BeforeEach
    fun setup() {
        tempDir = Files.createTempDirectory("jsonparser-tests").toFile()
    }

    @Test
    fun `loadToOrder - single order parses correctly`() {
        val json = """
            {
              "order": {
                "order_status": "INCOMING",
                "type": "PICKUP",
                "order_date": 123456789,
                "items": []
              }
            }
        """

        val file = File(tempDir, "order1.json")
        file.writeText(json)

        val result = JSONOrderParser.loadToOrder(file)

        Assertions.assertEquals(1, result.size)

        val order = result[0]
        Assertions.assertEquals(OrderStatus.INCOMING, order.orderStatus)
        Assertions.assertEquals(OrderType.PICKUP, order.orderType)
        Assertions.assertEquals(123456789, order.orderTime)
    }

    @Test
    fun `loadToOrder - multiple orders parsed from array`() {
        val json = """
           [
             { "order": { "order_status": "COMPLETED", "type": "PICKUP", "order_date": 100, "items": [] }},
             { "order": { "order_status": "INCOMING", "type": "DELIVERY", "order_date": 200, "items": [] }}
           ]
        """.trimIndent()

        val file = File(tempDir, "orders.json")
        file.writeText(json)

        val result = JSONOrderParser.loadToOrder(file)

        Assertions.assertEquals(2, result.size)

        Assertions.assertEquals(OrderStatus.COMPLETED, result[0].orderStatus)
        Assertions.assertEquals(OrderType.PICKUP, result[0].orderType)

        Assertions.assertEquals(OrderStatus.INCOMING, result[1].orderStatus)
        Assertions.assertEquals(OrderType.DELIVERY, result[1].orderType)
    }

    @Test
    fun `jsonToOrder - missing delivery status uses PENDING for delivery`() {
        val obj = JSONObject()
        obj["type"] = "DELIVERY"
        obj["items"] = JSONArray()
        obj["order_status"] = "INCOMING"
        obj["order_date"] = 0L

        val order = JSONOrderParser.jsonToOrder(obj)

        Assertions.assertEquals(DeliveryStatus.PENDING, order.deliveryStatus)
    }

    @Test
    fun `loadToOrder - explicit ID is applied from filename`() {
        val json = """
            {
              "order": {
                "order_status": "INCOMING",
                "type": "PICKUP",
                "order_date": 0,
                "items": []
              }
            }
        """.trimIndent()

        val file = File(tempDir, "Order#42.json")
        file.writeText(json)

        val result = JSONOrderParser.loadToOrder(file)

        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals(42, result[0].orderID)
    }

    @Test
    fun `writeOrderToJSON creates correct file`() {
        val order = Order(
            mutableListOf(),
            OrderStatus.INCOMING,
            123L,
            OrderType.PICKUP
        )

        val ordersDir = File("orders")
        if (ordersDir.exists()) ordersDir.deleteRecursively()

        JSONOrderParser.writeOrderToJSON(order)

        val output = File("orders", "Order#${order.orderID}.json")
        Assertions.assertTrue(output.exists())
    }

    @Test
    fun `formatForWriting generates expected JSON`() {
        val item = FoodItem("Orange", 5, 2.5)
        val order = Order(
            mutableListOf(item),
            OrderStatus.INCOMING,
            789L,
            OrderType.DELIVERY
        )
        order.deliveryStatus = DeliveryStatus.PENDING

        val result = JSONOrderParser.formatForWriting(order)

        val orderObj = result["order"] as Map<*, *>

        Assertions.assertEquals("INCOMING", orderObj["order_status"])
        Assertions.assertEquals("DELIVERY", orderObj["type"])
        Assertions.assertEquals("PENDING", orderObj["delivery_status"])
        Assertions.assertEquals(789L, orderObj["order_date"])

        val items = orderObj["items"] as JSONArray
        Assertions.assertEquals(1, items.size)

        val itemObj = items[0] as Map<*, *>
        Assertions.assertEquals("Orange", itemObj["name"])
        Assertions.assertEquals(5L, itemObj["quantity"])
        Assertions.assertEquals(2.5, itemObj["price"])
    }
}