package org.FoodHub;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderManagerTest {

    @Mock
    private Order mockOrderA;

    @Mock
    private Order mockOrderB;

    @Mock
    private Order mockOrderC;

    @Mock
    private Menu mockMenu;

    @InjectMocks
    private OrderManager manager;

    static AutoCloseable autoCloseable;

    @BeforeEach
    void setup() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    public static void releaseMocks() throws Exception {
        autoCloseable.close();
    }

    @Test
    void testAddOrder() {
        manager.addOrder(mockOrderA);

        assertEquals(1, manager.getOrders().size());
        assertSame(mockOrderA, manager.getOrders().getFirst());
    }

    @Test
    void testFindOrderFound() {
        when(mockOrderA.getOrderID()).thenReturn(10);
        when(mockOrderB.getOrderID()).thenReturn(20);

        manager.addOrder(mockOrderA);
        manager.addOrder(mockOrderB);

        assertEquals(mockOrderA, manager.findOrder(10));
        assertEquals(mockOrderB, manager.findOrder(20));
    }

    @Test
    void testFindOrderNotFound() {
        assertNull(manager.findOrder(999));
    }

    @Test
    void testCancelOrderCallsOrderCancel() {
        when(mockOrderA.getOrderID()).thenReturn(1);

        manager.addOrder(mockOrderA);

        manager.cancelOrder(1);

        verify(mockOrderA, times(1)).cancelOrder();
    }

    @Test
    void testCancelOrderInvalidIdDoesNothing() {
        manager.cancelOrder(999);

        verifyNoInteractions(mockOrderA, mockOrderB, mockOrderC);
    }

    @Test
    void testCalculateAllOrderPriceIgnoresCancelled() {
        when(mockOrderA.getOrderStatus()).thenReturn(OrderStatus.INCOMING);
        when(mockOrderB.getOrderStatus()).thenReturn(OrderStatus.INCOMING);
        when(mockOrderC.getOrderStatus()).thenReturn(OrderStatus.CANCELLED);

        when(mockOrderA.calculateTotalPrice()).thenReturn(5.0);
        when(mockOrderB.calculateTotalPrice()).thenReturn(10.0);
        when(mockOrderC.calculateTotalPrice()).thenReturn(999.0);

        manager.addOrder(mockOrderA);
        manager.addOrder(mockOrderB);
        manager.addOrder(mockOrderC);

        double result = manager.getAllOrderPrice();

        assertEquals(15.0, result);
    }

    @Test
    void testSetAllOrderAddsAllOrders() {
        List<Order> orders = Arrays.asList(mockOrderA, mockOrderB);

        manager.setAllOrder(orders);

        assertEquals(2, manager.getOrders().size());
        assertTrue(manager.getOrders().contains(mockOrderA));
        assertTrue(manager.getOrders().contains(mockOrderB));
    }

    @Test
    void testGetMenuListUsesMenuDependency() {
        List<FoodItem> fakeMenuList = Collections.emptyList();

        when(mockMenu.getAvailableFoodItem()).thenReturn(fakeMenuList);

        List<FoodItem> result = manager.getMenuList();

        assertSame(fakeMenuList, result);
        verify(mockMenu, times(1)).getAvailableFoodItem();
    }
}
