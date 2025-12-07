### 1. The software shall have a User Interface titled Order Manager, containing OrderID, OrderDate, OrderType, OrderStatus, and DeliveryStatus columns
### 2. The software shall only show delivery status if the order type is delivery
### 3. Any order with missing data shall be thrown out into "orders/ErrorOrders" directory
### 4. The software shall be able to update visually to the UI in real time while orders are being put into the "orders" directory
### 5. When setting the order status to "completed" and click "update status", the software shall automatically export the order into the "orders/completedOrder" directory
### 6. When selecting an order there shall be a button to display order and when clicked the software shall present all the order's details
### 7. In each order type, the software shall have a logo to represent their order type for pick up, delivery, togo
### 8. When an order is being processed with no date, the software shall create order_date at the moment it is being processed
### 9. If SavedDataForLoad.json file already exists but is completely empty, the software shall still be able to startup the program and load all the orders that still needs to be processed
### 10. The software shall have a button that will export all orders called "Export Orders", and shall export all current processed orders into "orders/all_orders" directory
### 11. The software shall ignore order file "orderID" if one exists and create import it into the next available orderID of the manager
### 12. The software shall only allow order type "delivery" to be able to use the "Delivery Status" bar and update it's status
### 13. The software shall have four status for the order, "Incoming", "Started", "Completed", "Cancelled" and allowed to be updated at runtime and display to the UI
### 14. The delivery status for the orders shall have two status, "Pending", "Out For Delivery" and allowed to be updated only for delivery type orders and displayed to the UI
### 15. The software shall display all available orders onto the each columns of their respected information
### 16. The order details shall display the total cost of the specified order
### 17. The User Interface shall have the total price for all the orders excluding cancelled order
### 18. The User Interface's total order price display shall automatically update when an order has been cancelled or when a new order file has been processed
### 19. The software shall convert Long order_date into a readable string date to display onto the Order Manager Interface
### 20. The software shall be able to process both JSON and XML file in the "orders" directory
### 21. The software shall include use of the Kotlin programming language