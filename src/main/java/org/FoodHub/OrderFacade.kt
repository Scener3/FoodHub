package org.FoodHub

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.json.simple.parser.ParseException
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import javax.xml.parsers.ParserConfigurationException

class OrderFacade {
    @JvmField
    val orderManager: OrderManager = OrderManager()
    private val processor: OrderProcessor = OrderProcessor()
    private val saveData: SaveState
    private val accesser: FileAccesser = FileAccesser()
    private val filePath: File = File("SavedDataForLoad.json")

    private var allOrdersList: ObservableList<Order?>? = null
    private var fileListenerService: FetchFilesService? = null
    private var fileListenerThread: Thread? = null

    init {
        this.saveData = JSONSaveData(orderManager)
    }

    @Throws(IOException::class, ParseException::class, ParserConfigurationException::class, SAXException::class)
    fun init(tableBackingList: ObservableList<Order?>, priceUpdateCallback: Runnable) {
        val allOrders: MutableList<Order?>
        if (filePath.exists() && filePath.length() > 0) {
            allOrders = processor.processSingleOrder("SavedDataForLoad.json")
            allOrders.addAll(processor.processAllOrder())
        } else {
            allOrders = processor.processAllOrder()
        }

        orderManager.setAllOrder(allOrders)
        allOrdersList = FXCollections.observableArrayList<Order?>(allOrders)
        tableBackingList.setAll(allOrdersList)

        priceUpdateCallback.run()
        saveData.save(orderManager, filePath)

        fileListenerService = FetchFilesService(
            allOrdersList,
            orderManager,
            processor,
            accesser,
            Runnable {
                priceUpdateCallback.run()
                saveData.save(orderManager, filePath)
            }
        )
        fileListenerThread = Thread(fileListenerService, "FileWatcherThread")
        fileListenerThread!!.setDaemon(true)
        fileListenerThread!!.start()
    }

    fun shutdown() {
        if (fileListenerService != null) {
            fileListenerService!!.stop()
        }
    }

    fun getAllOrdersList(): ObservableList<Order?> {
        return allOrdersList!!
    }

    val totalPrice: Double
        get() = orderManager.getAllOrderPrice()

    fun save() {
        saveData.save(orderManager, filePath)
    }

    fun addOrderFromUi(newOrder: Order?) {
        orderManager.addOrder(newOrder)
        allOrdersList!!.add(newOrder)
        save()
    }

    val menuList: MutableList<FoodItem?>?
        get() = orderManager.getMenuList()

    fun exportAllOrders() {
        processor.writeAllOrdersToFile(orderManager.getOrders())
    }
}
