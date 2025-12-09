package org.FoodHub

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.Instant

object JSONOrderParser : OrderParserInterface {
    override fun loadToOrder(orderFile: File): MutableList<Order> {
        val allOrders = ArrayList<Order>()
        val parser = JSONParser()

        if (orderFile.exists()) {
            try {
                FileReader(orderFile).use { reader ->
                    val orderLevelObject = parser.parse(reader)
                    if (orderLevelObject is JSONArray) {
                        for (obj in orderLevelObject) {
                            val orderInfo = obj as JSONObject
                            val orderData = orderInfo["order"] as? JSONObject ?: orderInfo
                            allOrders.add(jsonToOrder(orderData))
                        }
                    } else if (orderLevelObject is JSONObject) {
                        val orderData = orderLevelObject["order"] as? JSONObject ?: orderLevelObject

                        if (orderFile.nameWithoutExtension.contains('#')){
                            val explicitID = orderFile.nameWithoutExtension.substringAfter("#").toInt()
                            allOrders.add(jsonToOrder(orderData, explicitID))
                        }else {
                            allOrders.add(jsonToOrder(orderData))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return allOrders
    }

    fun jsonToOrder(orderData: JSONObject, explicitID: Int? = null): Order {
        val typeString = orderData["type"] as? String
        val orderDate = (orderData["order_date"] as? Long) ?: Instant.now().toEpochMilli()
        val itemsArray = orderData["items"] as? JSONArray ?: JSONArray()
        val orderedItems = mutableListOf<FoodItem>()
        val items = mutableListOf<FoodItem>()
        for (item in itemsArray) {
            val itemData = item as JSONObject
            val name = itemData["name"] as? String
            val quantity = itemData["quantity"] as? Int
            val price = itemData["price"] as? Double

            if (name != null && quantity != null && price != null) {
                val item = FoodItem(name, quantity.toLong(), price)
                items.add(item)
}
        }

        val orderStatusString = orderData["order_status"] as? String
        val enumStatus = parseOrderStatus(orderStatusString)
        val enumType = parserOrderType(typeString)

        // Create delivery status here if it exists
        val deliveryStatusString = orderData["delivery_status"] as? String
        var enumDeliveryStatus = if (deliveryStatusString != null){
            try{
                DeliveryStatus.valueOf(deliveryStatusString)
            } catch(e: Exception){null}
        } else{null}

        if (enumDeliveryStatus == null && enumType == OrderType.DELIVERY){
            enumDeliveryStatus = DeliveryStatus.PENDING
        }

        val newOrder = if (explicitID != null) {
            Order(orderedItems, enumStatus, orderDate, enumType, explicitID)
        } else{
            Order(orderedItems, enumStatus, orderDate, enumType)
        }

        newOrder.deliveryStatus = enumDeliveryStatus


        return newOrder
    }


    private fun parseOrderStatus(status: String?): OrderStatus {
        return try {
            status?.let { OrderStatus.valueOf(it.trim().uppercase()) } ?: OrderStatus.INCOMING
        } catch (e: IllegalArgumentException) {
            OrderStatus.INCOMING
        }
    }

    private fun parserOrderType(type: String?): OrderType {
        return try {
            type?.let { OrderType.valueOf(it.trim().uppercase()) } ?: OrderType.PICKUP
        } catch (e: IllegalArgumentException) {
            OrderType.PICKUP
        }
    }

    fun writeOrderToJSON(theOrder: Order) {
        val targetFolder = if (theOrder.orderStatus == OrderStatus.INCOMING) {
            File("orders")
        } else {
            File("orders/completedOrders")
        }

        if (!targetFolder.exists()){
            targetFolder.mkdirs()
            println("Created Directory: ${targetFolder.path}")
        }

        val outputFile = File(targetFolder, "Order#${theOrder.orderID}.json")
        try {
            FileWriter(outputFile).use { file -> file.write(formatForWriting(theOrder).toJSONString())}
        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun writeAllOrderToFile(allOrders: List<Order>)  {
        val allOrdersArray = JSONArray()
        for (order in allOrders){
            allOrdersArray.add(formatForWriting(order))
        }

        val allOrderPath = File("orders/all_orders")
        if (!allOrderPath.exists()){
            allOrderPath.mkdirs()
            println("Created Directory")
        }

        try{
            FileWriter(File(allOrderPath, "all_orders.json")).use { file -> file.write(allOrdersArray.toJSONString())
                println("Success")
            }
        } catch(e: Exception){
            e.printStackTrace()
        }
    }

    fun formatForWriting(incomingOrder: Order): JSONObject {
        val itemArray = JSONArray()
        for (itemData in incomingOrder.foodItems) {
            val itemObj = LinkedHashMap<String, Any>()
            itemObj["name"] = itemData.name
            itemObj["quantity"] = itemData.quantity
            itemObj["price"] = itemData.price
            itemArray.add(itemObj)
        }

        val orderObj = LinkedHashMap<String, Any>()
        orderObj["order_status"] = incomingOrder.orderStatus.name
        orderObj["type"] = incomingOrder.orderType.name
        if (incomingOrder.deliveryStatus != null){
            orderObj["delivery_status"] = incomingOrder.deliveryStatus!!.name
        }
        orderObj["order_date"] = incomingOrder.orderTime
        orderObj["items"] = itemArray


        val finalObj = JSONObject()
        finalObj["order"] = orderObj
        return finalObj
    }
}