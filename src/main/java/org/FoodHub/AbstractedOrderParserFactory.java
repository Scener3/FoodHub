package org.FoodHub;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AbstractedOrderParserFactory {
    private static final Map<String, OrderParserInterface> registeredParser = new ConcurrentHashMap<>();

    static{
        registerParser("JSON", jsonOrderParser.INSTANCE);
        registerParser("XML", xmlParser.getInstance());
    }

    public static void registerParser(String format, OrderParserInterface parserInstance){
        registeredParser.put(format.toUpperCase(), parserInstance);
    }

    public static OrderParserInterface getParser(String format){
        OrderParserInterface parser = registeredParser.get(format.toUpperCase());

        if (parser == null){
            System.out.println("Format not supported");
            System.out.println("Available Format: " + registeredParser.keySet());
        }
        return parser;
    }


}
