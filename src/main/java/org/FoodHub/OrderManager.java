package org.FoodHub;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing and managing orders
 *
 * Domain Layer
 *
 */
public class OrderManager{

    /**
     * The list of orders that an OrderManager will manage.
     */
    private List<Order> orders = new ArrayList<>();
    Menu currentMenu = new Menu();
    /**
     * Adds an order to orders list.
     *
     * @param order - The order to be added to orders.
     */
    void addOrder(Order order) {
        this.orders.add(order);
    }

    /**
     * Changes an order's status to CANCELLED based on its order ID.
     *
     * @param orderID the order ID of the order to be CANCELLED.
     */
    void cancelOrder(int orderID) {
        Order order = findOrder(orderID);
        if (order != null) {
            order.setOrderStatus(OrderStatus.CANCELLED);
        } else {
            System.out.printf("Order with ID %d doesn't exist.\n", orderID);
        }
    }
/*

///////////////////// Commented out unused codes maybe will need it for restructuring later
    */
/**
 * Changes an incoming order's status to Started.
 *
 * @param orderID - the orderID of the order to be started.
 *//*

    void startIncomingOrder(int orderID) {
        Order order = findOrder(orderID);

        if (order != null) {
            if(!order.getOrderStatus().equals(OrderStatus.INCOMING)) {
                System.out.println("Only incoming orders can be started.\n");
            } else {
                order.setOrderStatus(OrderStatus.STARTED);
            }
        } else {
            System.out.printf("Order with ID %d doesn't exist.\n", orderID);
        }
    }

    */
/**
 * Sets a given order's status to COMPLETE.
 *
 * @param orderID - the orderID of the order to be completed.
 *//*

    void completeIncomingOrder(int orderID) {
        Order order = findOrder(orderID);
        if(findOrder(orderID) != null && order.getOrderStatus().equals(OrderStatus.STARTED)) {
            order.setOrderStatus(OrderStatus.COMPLETED);
        } else if(order == null) {
            System.out.printf("Order with ID %d doesn't exist.\n", orderID);
        } else {
            System.out.printf("Order with ID %d must be started first.\n", orderID);
        }
    }
*/


    /**
     * Finds an order based on its orderID.
     *
     * @param orderID - the orderID of the order to be found.
     * @return the order with the specified orderID.
     */
    Order findOrder(int orderID) {
        Order order = null;
        for(Order o : orders) {
            if (o.getOrderID() == orderID) {
                order = o;
            }
        }
        return order;
    }

    /**
     * @return the list of orders managed by the OrderManager.
     */
    List<Order> getOrders() {
        return this.orders;
    }

    protected void setAllOrder(List<Order> allOrders){
        orders.addAll(allOrders);
    }

    protected double calculateAllOrderPrice(){
        double currentTotal = 0;
        for (Order o : getOrders()){
            if (o.getOrderStatus() == OrderStatus.CANCELLED){
                continue;
            }
            currentTotal += o.calculateTotalPrice();
        }
        return currentTotal;
    }

    protected double getAllOrderPrice(){
        return calculateAllOrderPrice();
    }

    public List<FoodItem> getMenuList(){
        return currentMenu.getAvailableFoodItem();
    }

    public Order createNewOrder(List<FoodItem> selectedItems){
        Order newOrder = new Order(
                selectedItems,
                OrderStatus.INCOMING,
                Instant.now().toEpochMilli(),
                OrderType.PICKUP
        );

        System.out.println("Created Order File");
        return newOrder;
    }


}

