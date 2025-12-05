package org.FoodHub;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import javafx.scene.control.Label;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class OrderTrackerController {
    private OrderProcessor process = new OrderProcessor();
    private OrderManager orderManager = new OrderManager();
    private ObservableList<Order> allOrdersList;
    private SaveState saveData = new jsonSaveData(orderManager);
    private File filePath = new File("SavedDataForLoad.json");
    private FileAccesser accesser = new FileAccesser();
    private List<Order> allOrders;
    private FetchFilesService fileListenerService;
    private Thread fileListenerThread;
    private OrderTrackerView view = new OrderTrackerView();


    @FXML
    public TableView<Order> orderTable;
    @FXML
    public TableColumn<Order, Integer> idColumn;
    @FXML
    public TableColumn<Order, String> dateColumn;
    @FXML
    public TableColumn<Order, String> typeColumn;
    @FXML
    public TableColumn<Order, String> statusColumn;
    @FXML
    public TableColumn<Order, String> deliveryStatusColumn;
    @FXML
    public ComboBox<OrderStatus> statusBox;
    @FXML
    public ComboBox<DeliveryStatus> deliveryStatusBox;
    @FXML
    public Label totalPriceLabel;
    @FXML
    public Button updateButton;
    @FXML
    public Button exportButton;
    @FXML
    public Button displayOrder;
    @FXML
    public Button createOrder;


    /**
     * Will initialize with SavedDataForLoad.json being processed
     * **/
    @FXML
    public void initialize() throws IOException, ParseException, ParserConfigurationException, SAXException {
        setupTableColumns();
        setupDropDowns();
        setupListener();
        loadInitData();

        fileListenerService = new FetchFilesService(allOrdersList, orderManager, process, accesser, () -> {
            updatePriceDisplay();
            saveData.save(orderManager, filePath);
        });
        fileListenerThread = new Thread(fileListenerService, "FileWatcherThread");
        fileListenerThread.setDaemon(true);
        fileListenerThread.start();
    }

    public void shutdown(){
        if (fileListenerService != null){
            fileListenerService.stop();
        }
    }

    private void setupTableColumns(){
        idColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        dateColumn.setCellValueFactory(cellData -> {
            Long orderTime = cellData.getValue().getOrderTime();
            if (orderTime == null){
                return new SimpleStringProperty("N/A");
            }else{
                return new SimpleStringProperty(new Date(orderTime).toString());
            }
        });
        deliveryStatusColumn.setCellValueFactory(order ->
                new SimpleStringProperty(order.getValue().getDeliveryStatus() != null ?
                        order.getValue().getDeliveryStatus().toString() : ""));
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrderType().toString()));

        typeColumn.setCellFactory(view.getOrderTypeCellFactory());
    }

    private void setupDropDowns(){
        // Setting Status Box
        List<OrderStatus> statuses = Arrays.asList(OrderStatus.values());
        statusBox.setItems(FXCollections.observableArrayList(statuses));

        // Delivery Status Box
        deliveryStatusBox.setItems(FXCollections.observableArrayList(DeliveryStatus.values()));
        deliveryStatusBox.setDisable(true);
    }

    private void setupListener(){
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateUIForSelectedOrder(newSelection);
        });
    }

    private void loadInitData() throws IOException, ParseException, ParserConfigurationException, SAXException {
        if (filePath.exists() && filePath.length() > 0) {
            allOrders = process.processSingleOrder("SavedDataForLoad.json");
            allOrders.addAll(process.processAllOrder());
        }
        else
        {
            allOrders = process.processAllOrder();
        }

        orderManager.setAllOrder(allOrders);
        allOrdersList = FXCollections.observableArrayList(allOrders);
        orderTable.setItems(allOrdersList);


        updatePriceDisplay();
        saveData.save(orderManager, filePath);
    }

    public void handleExportOrders(ActionEvent actionEvent) {
        process.writeAllOrdersToFile(orderManager.getOrders());
    }

    /**
     * Updates UI components based on the currently selected order type.
     * Enables or disables the delivery status ComboBox.
     * @param selected The currently selected Order from the TableView.
     */
    private void updateUIForSelectedOrder(Order selected) {
        if (selected != null && selected.getOrderType() == OrderType.DELIVERY) {
            deliveryStatusBox.setDisable(false);
            deliveryStatusBox.setValue(selected.getDeliveryStatus());
        } else {
            deliveryStatusBox.setDisable(true);
            deliveryStatusBox.setValue(null);
        }

        statusBox.setDisable(false);
        // Disables StatusBox if the order is completed
        if (selected != null && selected.getOrderStatus() == OrderStatus.COMPLETED || selected.getOrderStatus() == OrderStatus.CANCELLED){
            statusBox.setDisable(true);
        }
        else if (selected != null && selected.getOrderStatus() == OrderStatus.STARTED){
            statusBox.setItems(FXCollections.observableArrayList(
                    OrderStatus.CANCELLED,
                    OrderStatus.COMPLETED
            ));
        }
        else{
            statusBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        }

        if (selected != null) {
            statusBox.setValue(selected.getOrderStatus());
        }
    }

    public void handleUpdateStatus(ActionEvent actionEvent) throws IOException, ParseException, ParserConfigurationException, SAXException {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        OrderStatus newStatus = statusBox.getValue();
        if (newStatus == OrderStatus.COMPLETED){
            selected.setOrderStatus(OrderStatus.COMPLETED);
            process.writeToJSON(selected);
        }
        if (selected != null && newStatus != null) {
            orderManager.findOrder(selected.getOrderID()).setOrderStatus(newStatus);
            orderTable.refresh();
        }
        if (selected != null && !deliveryStatusBox.isDisable() && deliveryStatusBox.getValue() != null) {
            orderManager.findOrder(selected.getOrderID()).setDeliveryStatus(deliveryStatusBox.getValue());
        }

        orderTable.refresh();
        saveData.save(orderManager, filePath);
        updatePriceDisplay();
        updateUIForSelectedOrder(selected);
    }

    public void handleDisplayOrder(ActionEvent actionEvent) {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        view.showOrderDetailsPopup(selected);
    }

    private void updatePriceDisplay(){
        double total = orderManager.getAllOrderPrice();
        view.updatePriceDisplay(totalPriceLabel, total);
    }

    public void handleCreateOrder(ActionEvent actionEvent){
        OrderManager tempOrderManager = new OrderManager();
        List<FoodItem> menuOption = orderManager.getMenuList();
        view.showCreateOrderPopup(menuOption, (selectedItems) ->  {
            Order newOrder = tempOrderManager.createNewOrder(selectedItems);
            newOrder.setOrderID(orderManager.getOrders().size());
            process.writeToJSON(newOrder);
        });
    }
}

