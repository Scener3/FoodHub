package org.FoodHub;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderProcessorTest {

    private OrderProcessor processor;
    private FileAccesser mockFileAccesser;

    @BeforeEach
    void setup() throws Exception {
        processor = new OrderProcessor();

        mockFileAccesser = mock(FileAccesser.class);
        Field field = OrderProcessor.class.getDeclaredField("fileAccesser");
        field.setAccessible(true);
        field.set(processor, mockFileAccesser);
    }

    @Test
    void testProcessAllOrderSuccessfulProcessing() throws Exception {

        List<String> mockFiles = List.of("order1.json", "order2.xml");
        when(mockFileAccesser.fetchOrderFolderList()).thenReturn(mockFiles);
        when(mockFileAccesser.getExtension("order1.json")).thenReturn("json");
        when(mockFileAccesser.getExtension("order2.xml")).thenReturn("xml");

        Order mockOrder = mock(Order.class);

        OrderParserInterface jsonParser = mock(OrderParserInterface.class);
        OrderParserInterface xmlParser = mock(OrderParserInterface.class);

        when(jsonParser.loadToOrder(any(File.class))).thenReturn(List.of(mockOrder));
        when(xmlParser.loadToOrder(any(File.class))).thenReturn(List.of(mockOrder));

        try (MockedStatic<AbstractedOrderParserFactory> factoryMock = mockStatic(AbstractedOrderParserFactory.class)) {
            factoryMock.when(() -> AbstractedOrderParserFactory.getParser("json")).thenReturn(jsonParser);
            factoryMock.when(() -> AbstractedOrderParserFactory.getParser("xml")).thenReturn(xmlParser);

            List<Order> result = processor.processAllOrder();

            assertEquals(2, result.size());

            verify(mockFileAccesser).moveProcessedFile("order1.json");
            verify(mockFileAccesser).moveProcessedFile("order2.xml");
        }
    }

    @Test
    void testProcessAllOrderProcessingErrorMovesFileToError() throws Exception {
        List<String> mockFiles = List.of("badfile.json");
        when(mockFileAccesser.fetchOrderFolderList()).thenReturn(mockFiles);
        when(mockFileAccesser.getExtension("badfile.json")).thenReturn("json");

        OrderParserInterface jsonParser = mock(OrderParserInterface.class);
        when(jsonParser.loadToOrder(any(File.class))).thenThrow(new ParseException(0));

        try (MockedStatic<AbstractedOrderParserFactory> factoryMock = mockStatic(AbstractedOrderParserFactory.class)) {
            factoryMock.when(() -> AbstractedOrderParserFactory.getParser("json")).thenReturn(jsonParser);

            processor.processAllOrder();

            verify(mockFileAccesser).moveErrorFile("badfile.json");
        }
    }

    @Test
    void testProcessSingleOrderUsesCorrectParser() throws Exception {
        when(mockFileAccesser.getExtension("order.xml")).thenReturn("xml");

        OrderParserInterface xmlParser = mock(OrderParserInterface.class);
        Order mockOrder = mock(Order.class);
        when(xmlParser.loadToOrder(any(File.class))).thenReturn(List.of(mockOrder));

        try (MockedStatic<AbstractedOrderParserFactory> factoryMock = mockStatic(AbstractedOrderParserFactory.class)) {
            factoryMock.when(() -> AbstractedOrderParserFactory.getParser("xml")).thenReturn(xmlParser);

            List<Order> result = processor.processSingleOrder("order.xml");

            assertEquals(1, result.size());
            verify(xmlParser).loadToOrder(any(File.class));
        }
    }
}
