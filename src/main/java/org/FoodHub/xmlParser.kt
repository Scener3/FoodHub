package org.FoodHub

import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.concurrent.Volatile

object xmlParser : OrderParserInterface {
    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    override fun loadToOrder(orderFile: File): MutableList<Order> {
        val allOrder: MutableList<Order> = ArrayList<Order>()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(orderFile)

        val orderList = document.getElementsByTagName("Order")

        for (i in 0..<orderList.getLength()) {
            val allFood: MutableList<FoodItem> = ArrayList<FoodItem>()
            val order = orderList.item(i) as Element
            //            String orderID = order.getAttribute("id");
            val orderType = order.getElementsByTagName("OrderType").item(0).getTextContent()
            if (orderType.isBlank()) {
                throw SAXException("Missing Data Will Not Accept")
            }

            val itemList = order.getElementsByTagName("Item")
            for (j in 0..<itemList.getLength()) {
                val item = itemList.item(j) as Element
                val type = item.getAttribute("type")
                val priceString = item.getElementsByTagName("Price").item(0).getTextContent()
                val quantityString = item.getElementsByTagName("Quantity").item(0).getTextContent()

                if (type.isBlank() || priceString.isBlank() || quantityString.isBlank()) {
                    throw SAXException("Missing Data Will Not Accept")
                }

                val price = priceString.toDouble()
                val quantity = quantityString.toInt()
                val currentFood = FoodItem(type, quantity, price)
                allFood.add(currentFood)
            }

            val orderedTime = Instant.now()
            val convertedToMili = orderedTime.toEpochMilli()
            var enumType = xmlParseOrderType(orderType)
            if (enumType == null) enumType = OrderType.PICKUP // Default fallback

            val currentO = Order(allFood, OrderStatus.INCOMING, convertedToMili, enumType)
            allOrder.add(currentO)
        }
        return allOrder
    }

    private fun xmlParseOrderType(type: String?): OrderType? {
        if (type == null) return null
        try {
            return OrderType.valueOf(type.trim { it <= ' ' }.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            return null
        }
    }

}
