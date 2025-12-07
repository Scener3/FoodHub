package org.FoodHub;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class OrderFacade {

    private final OrderManager orderManager;
    private final OrderProcessor processor;
    private final SaveState saveData;
    private final FileAccesser accesser;
    private final File filePath;

    private ObservableList<Order> allOrdersList;
    private FetchFilesService fileListenerService;
    private Thread fileListenerThread;

    public OrderFacade() {
        this.orderManager = new OrderManager();
        this.processor = new OrderProcessor();
        this.accesser = new FileAccesser();
        this.filePath = new File("SavedDataForLoad.json");
        this.saveData = new jsonSaveData(orderManager);
    }

    public void init(ObservableList<Order> tableBackingList, Runnable priceUpdateCallback)
            throws IOException, ParseException, ParserConfigurationException, SAXException {

        List<Order> allOrders;
        if (filePath.exists() && filePath.length() > 0) {
            allOrders = processor.processSingleOrder("SavedDataForLoad.json");
            allOrders.addAll(processor.processAllOrder());
        } else {
            allOrders = processor.processAllOrder();
        }

        orderManager.setAllOrder(allOrders);
        allOrdersList = FXCollections.observableArrayList(allOrders);
        tableBackingList.setAll(allOrdersList);

        priceUpdateCallback.run();
        saveData.save(orderManager, filePath);

        fileListenerService = new FetchFilesService(
                allOrdersList,
                orderManager,
                processor,
                accesser,
                () -> {
                    priceUpdateCallback.run();
                    saveData.save(orderManager, filePath);
                }
        );
        fileListenerThread = new Thread(fileListenerService, "FileWatcherThread");
        fileListenerThread.setDaemon(true);
        fileListenerThread.start();
    }

    public void shutdown() {
        if (fileListenerService != null) {
            fileListenerService.stop();
        }
    }

    public ObservableList<Order> getAllOrdersList() {
        return allOrdersList;
    }

    public double getTotalPrice() {
        return orderManager.getAllOrderPrice();
    }

    public void save() {
        saveData.save(orderManager, filePath);
    }

    public void addOrderFromUi(Order newOrder) {
        orderManager.addOrder(newOrder);
        allOrdersList.add(newOrder);
        save();
    }

    public List<FoodItem> getMenuList() {
        return orderManager.getMenuList();
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }

    public void exportAllOrders() {
        processor.writeAllOrdersToFile(orderManager.getOrders());
    }

}
