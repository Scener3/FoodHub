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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;



public class OrderTrackerController {
   private final OrderFacade orderFacade = new OrderFacade();
   private ObservableList<Order> allOrdersList;
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
   public TableColumn<Order, OrderStatus> statusColumn;
   @FXML
   public TableColumn<Order, DeliveryStatus> deliveryStatusColumn;
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
       disableContextMenuOnIdAndDate();
       setupOrderTypeContextMenu();
       setupStatusContextMenu();
       setupDeliveryStatusContextMenu();
       setupDropDowns();
       setupListener();

       allOrdersList = FXCollections.observableArrayList();
       orderTable.setItems(allOrdersList);

       orderFacade.init(allOrdersList, this::updatePriceDisplay);
    }



   public void shutdown(){
       orderFacade.shutdown();
   }


   private void setupTableColumns(){
       idColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
       statusColumn.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOrderStatus()));
       dateColumn.setCellValueFactory(cellData -> {
           Long orderTime = cellData.getValue().getOrderTime();
           if (orderTime == null){
               return new SimpleStringProperty("N/A");
           }else{
               return new SimpleStringProperty(new Date(orderTime).toString());
           }
       });
       deliveryStatusColumn.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDeliveryStatus()));
       typeColumn.setCellValueFactory(cellData ->
               new SimpleStringProperty(cellData.getValue().getOrderType().toString()));


       typeColumn.setCellFactory(view.getOrderTypeCellFactory());
   }

   private void setupOrderTypeContextMenu() {
    var baseFactory = view.getOrderTypeCellFactory();

    typeColumn.setCellFactory(col -> {
        @SuppressWarnings("unchecked")
        TableCell<Order, String> cell = (TableCell<Order, String>) baseFactory.call(col);
        ContextMenu menu = new ContextMenu();

        cell.setOnContextMenuRequested(event -> {
            if (cell.isEmpty()) return;
            Order order = cell.getTableView().getItems().get(cell.getIndex());
            if (order == null) return;
            orderTable.getSelectionModel().select(order);
            menu.getItems().clear();

            for (OrderType orderType : OrderType.values()) {
                MenuItem item = new MenuItem(orderType.toString());
                item.setOnAction(e -> changeOrderType(order, orderType));
                menu.getItems().add(item);
            }

            menu.show(cell, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        return cell;
    });
}

private void setupStatusContextMenu() {
    statusColumn.setCellFactory(col -> {
        TableCell<Order, OrderStatus> cell = new TableCell<>() {
            @Override
            protected void updateItem(OrderStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        };

        ContextMenu menu = new ContextMenu();

        cell.setOnContextMenuRequested(event -> {
            Order order = cell.getTableView().getItems().get(cell.getIndex());
            if (order == null) return;

            orderTable.getSelectionModel().select(order);
            menu.getItems().clear();

            OrderStatus current = order.getOrderStatus();
            for (OrderStatus status : OrderStatus.values()) {
                if ((current == OrderStatus.COMPLETED || current == OrderStatus.CANCELLED)
                        && status != current) {
                    continue;
                }
                MenuItem item = new MenuItem(status.toString());
                item.setOnAction(e -> {
                    order.updateStatus(status);
                    orderTable.refresh();
                    orderFacade.save();
                    updatePriceDisplay();
                    updateUIForSelectedOrder(order);
                });
                menu.getItems().add(item);
            }

            menu.show(cell, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        return cell;
    });
}

private void setupDeliveryStatusContextMenu() {
    deliveryStatusColumn.setCellFactory(col -> {
        TableCell<Order, DeliveryStatus> cell = new TableCell<>() {
            @Override
            protected void updateItem(DeliveryStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        };

        ContextMenu menu = new ContextMenu();

        cell.setOnContextMenuRequested(event -> {
            Order order = cell.getTableView().getItems().get(cell.getIndex());
            if (order == null || order.getOrderType() != OrderType.DELIVERY) return;

            orderTable.getSelectionModel().select(order);
            menu.getItems().clear();

            for (DeliveryStatus ds : DeliveryStatus.values()) {
                MenuItem item = new MenuItem(ds.toString());
                item.setOnAction(e -> {
                    order.updateDeliveryStatus(ds);
                    orderTable.refresh();
                    orderFacade.save();
                    updatePriceDisplay();
                    updateUIForSelectedOrder(order);
                });
                menu.getItems().add(item);
            }

            menu.show(cell, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        return cell;
    });
}
private void disableContextMenuOnIdAndDate() {
    idColumn.setCellFactory(col -> {
        TableCell<Order, Integer> cell = new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        };
        cell.setContextMenu(null);
        cell.setOnContextMenuRequested(event -> event.consume());
        return cell;
    });

    dateColumn.setCellFactory(col -> {
        TableCell<Order, String> cell = new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        };
        cell.setContextMenu(null);
        cell.setOnContextMenuRequested(event -> event.consume());
        return cell;
    });
}

private void changeOrderType(Order selectedOrder, OrderType newType) {
    selectedOrder.updateOrderType(newType);
    if (newType != OrderType.DELIVERY) {
        selectedOrder.updateDeliveryStatus(null);
    }
    orderTable.refresh();
    orderFacade.save();
    updatePriceDisplay();
    updateUIForSelectedOrder(selectedOrder);
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




   public void handleExportOrders(ActionEvent actionEvent) {
       orderFacade.exportAllOrders();
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
       } else if (selected != null && selected.getOrderStatus() == OrderStatus.INCOMING) {
           statusBox.setItems(FXCollections.observableArrayList(
                   OrderStatus.STARTED,
                   OrderStatus.CANCELLED
           ));
       }
       else{
           statusBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));
       }

       if (selected != null) {
           statusBox.setValue(selected.getOrderStatus());
       }
   }


   public void handleUpdateStatus(ActionEvent actionEvent)
        throws IOException, ParseException, ParserConfigurationException, SAXException {
       Order selected = (Order) orderTable.getSelectionModel().getSelectedItem();
       OrderStatus newStatus = (OrderStatus) statusBox.getValue();
       DeliveryStatus newDeliveryStatus = (DeliveryStatus) deliveryStatusBox.getValue();
       if (selected == null || newStatus == null) return;

       selected.updateStatus(newStatus);

       if (!deliveryStatusBox.isDisable() && newDeliveryStatus != null) {
           selected.setDeliveryStatus(newDeliveryStatus);
       }

       orderTable.refresh();
       orderFacade.save();
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
       double total = orderFacade.getTotalPrice();
        view.updatePriceDisplay(totalPriceLabel, total);
   }


   public void handleCreateOrder(ActionEvent actionEvent){
    List<FoodItem> menuOption = orderFacade.getMenuList();
    view.showCreateOrderPopup(menuOption, (selectedItems) -> {
        Order newOrder = orderFacade.orderManager.createNewOrder(selectedItems);
        newOrder.setOrderID(orderFacade.orderManager.getOrders().size());
        orderFacade.addOrderFromUi(newOrder);

        Platform.runLater(() -> {
            System.out.println("DEBUG: Adding order to UI list, size now: " + allOrdersList.size());
            orderTable.refresh();
            updatePriceDisplay();
        });
    });
}



}
