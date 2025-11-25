package org.FoodHub

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import org.xml.sax.SAXException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.xml.parsers.ParserConfigurationException

class jsonSaveData(private val orderManager: OrderManager) : SaveState {
    private val orderParser = jsonOrderParser

    override fun save(om: OrderManager, filePath: File) {
        val ordersArray = JSONArray()
        var orderObj: JSONObject?

        for (o in om.getOrders()) {
            orderObj = orderParser.formatForWriting(o)
            ordersArray.add(orderObj)
        }

        try {
            FileWriter(filePath).use {writer -> writer.write(ordersArray.toJSONString())}
        } catch(e: Exception){
            e.printStackTrace()
        }
    }

    override fun load(filePath: File, om: OrderManager) {
        try {
            if (!filePath.exists()) {
                println("There is no save data, starting a new session")
            } else {
                val loadOrders: MutableList<Order> = orderParser.loadToOrder(filePath)
                for (o in loadOrders) {
                    om.addOrder(o)
                }
            }
        } catch (e: IOException) {
            System.err.println("\nSavedDataForLoad.json Corrupted")
        } catch (e: ParseException) {
            System.err.println("\nSavedDataForLoad.json Corrupted")
        } catch (e: ParserConfigurationException) {
            System.err.println("\nSavedDataForLoad.json Corrupted")
        } catch (e: SAXException) {
            System.err.println("\nSavedDataForLoad.json Corrupted")
        }
    }
}
