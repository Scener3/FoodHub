package org.FoodHub;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class OrderTrackerView {


    public void showOrderDetailsPopup(Order selected){
        Stage detailStage = new Stage();
        detailStage.setTitle("Order Details - ID " + selected.getOrderID());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Text header = new Text("Order Type: " + selected.getOrderType() +
                "\nStatus: " + selected.getOrderStatus() +
                "\nTime: " + new java.util.Date(selected.getOrderTime()));
        TableView<FoodItem> itemTable = new TableView<>();
        TableColumn<FoodItem, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));

        TableColumn<FoodItem, Number> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getQuantity()));

        TableColumn<FoodItem, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrice()));

        itemTable.getColumns().addAll(nameCol, qtyCol, priceCol);
        itemTable.setItems(FXCollections.observableArrayList(selected.getFoodItems()));

        Label totalLabel = new Label(String.format("Total: $%.2f", selected.calculateTotalPrice()));

        layout.getChildren().addAll(header, itemTable, totalLabel);

        Scene scene = new Scene(layout, 400, 300);
        detailStage.setScene(scene);
        detailStage.show();
    }

    public void updatePriceDisplay(Label label, double price){
        if (label != null){
            label.setText(String.format("Total Price: $%.2f", price));
        }
    }

    public void showCreateOrderPopup(List<FoodItem> menuItem, Consumer<List<FoodItem>> onOrderComplete){
        Stage stage = new Stage();
        stage.setTitle("Create New Order");
        TableView<FoodItem> menuTable = new TableView<>();
        menuTable.setPrefHeight(300);

        TableColumn<FoodItem, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        TableColumn<FoodItem, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()));

        menuTable.getColumns().addAll(nameCol, priceCol);
        menuTable.setItems(FXCollections.observableArrayList(menuItem));

        TextField quantityField = new TextField("1");
        quantityField.setPromptText("Qty");
        quantityField.setMaxWidth(50);

        Button addButton = new Button("Add to Cart ->");
        VBox leftBox = new VBox(10, new Label("Menu"), menuTable, new HBox(10, new Label("Qty"), quantityField, addButton));
        leftBox.setPadding(new Insets(10));

        ObservableList<FoodItem> cartList = FXCollections.observableArrayList();
        ListView<String> cartDisplay = new ListView<>();
        Button submitButton = new Button("Finalize Order");
        VBox rightBox = new VBox(10, new Label("Your Cart"), cartDisplay, submitButton);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(250);

        addButton.setOnAction(e -> {
            FoodItem selected = menuTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    int qty = Integer.parseInt(quantityField.getText());
                    if (qty > 0) {
                        FoodItem itemForCart = new FoodItem(selected.getName(), qty, selected.getPrice());
                        cartList.add(itemForCart);
                        cartDisplay.getItems().add(qty + "x " + selected.getName());
                    }
                } catch (NumberFormatException ex) {
                    quantityField.setText("1");
                }
            }
        });

        submitButton.setOnAction(e -> {
            if (!cartList.isEmpty()) {
                onOrderComplete.accept(new ArrayList<>(cartList));
                stage.close();
            }
        });

        HBox root = new HBox(10, leftBox, rightBox);
        Scene scene = new Scene(root, 600, 450);
        stage.setScene(scene);
        stage.show();
    }

}
