package org.FoodHub;

import javafx.collections.ObservableList;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderFacadeTest {

    @Mock private OrderManager mockOrderManager;
    @Mock private OrderProcessor mockProcessor;
    @Mock private SaveState mockSaveState;
    @Mock private FileAccesser mockAccesser;
    @Mock private ObservableList<Order> mockList;
    @Mock private FetchFilesService mockFileService;

    private OrderFacade facade;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        facade = new OrderFacade();

        inject("orderManager", mockOrderManager);
        inject("processor", mockProcessor);
        inject("saveData", mockSaveState);
        inject("accesser", mockAccesser);
        inject("allOrdersList", null);
        inject("fileListenerService", null);
    }

    private void inject(String field, Object value) throws Exception {
        Field f = OrderFacade.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(facade, value);
    }

    private void injectFilePath(File f) throws Exception {
        Field fileField = OrderFacade.class.getDeclaredField("filePath");
        fileField.setAccessible(true);
        fileField.set(facade, f);
    }

    @Test
    void testInitLoadsOrdersAndStartsWatcher()
            throws IOException, ParseException, ParserConfigurationException, SAXException, Exception {

        File fake = mock(File.class);
        when(fake.exists()).thenReturn(true);
        when(fake.length()).thenReturn(10L);
        injectFilePath(fake);

        List<Order> first = new ArrayList<>(List.of(mock(Order.class)));
        List<Order> second = new ArrayList<>(List.of(mock(Order.class)));

        when(mockProcessor.processSingleOrder("SavedDataForLoad.json")).thenReturn(first);
        when(mockProcessor.processAllOrder()).thenReturn(second);

        Runnable callback = mock(Runnable.class);

        facade.init((ObservableList) mockList, callback);

        verify(mockProcessor).processSingleOrder("SavedDataForLoad.json");
        verify(mockProcessor).processAllOrder();
        verify(mockOrderManager).setAllOrder(anyList());
        verify(mockList).setAll(anyList());

        verify(callback).run();
        verify(mockSaveState).save(eq(mockOrderManager), any(File.class));
    }

    @Test
    void testShutdownStopsService() throws Exception {
        inject("fileListenerService", mockFileService);

        facade.shutdown();

        verify(mockFileService).stop();
    }

    @Test
    void testGetTotalPriceDelegatesToOrderManager() {
        when(mockOrderManager.getAllOrderPrice()).thenReturn(42.5);

        double result = facade.getTotalPrice();

        assertEquals(42.5, result);
        verify(mockOrderManager).getAllOrderPrice();
    }

    @Test
    void testSaveCallsSaveData() {
        facade.save();

        verify(mockSaveState).save(eq(mockOrderManager), any(File.class));
    }

    @Test
    void testAddOrderFromUiAddsToManagerAndListAndSaves() throws Exception {
        Order newOrder = mock(Order.class);

        ObservableList<Order> localList = mock(ObservableList.class);
        inject("allOrdersList", localList);

        facade.addOrderFromUi(newOrder);

        verify(mockOrderManager).addOrder(newOrder);
        verify(localList).add(newOrder);
        verify(mockSaveState).save(eq(mockOrderManager), any(File.class));
    }

    @Test
    void testExportAllOrdersDelegatesToProcessor() {
        List<Order> mockOrders = List.of(mock(Order.class));
        when(mockOrderManager.getOrders()).thenReturn(mockOrders);

        facade.exportAllOrders();

        verify(mockProcessor).writeAllOrdersToFile(mockOrders);
    }
}
