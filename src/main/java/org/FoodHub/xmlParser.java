package org.FoodHub;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class xmlParser implements OrderParserInterface{
    /*
     * Singleton Pattern for the xmlParser
     *
     * */
    private static volatile xmlParser instance;

    private xmlParser(){}

    public static xmlParser getInstance(){
        if (instance == null){
            synchronized (xmlParser.class){
                if (instance == null){
                    instance = new xmlParser();
                }
            }
        }
        return instance;
    }

    @Override
    public List<Order> loadToOrder(File orderFile) throws IOException, ParserConfigurationException, SAXException {
        List<Order> allOrder = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(orderFile);

        NodeList orderList = document.getElementsByTagName("Order");

        for (int i = 0; i < orderList.getLength(); i++){
            List<FoodItem> allFood = new ArrayList<>();
            Element order = (Element)orderList.item(i);
//            String orderID = order.getAttribute("id");
            String orderType = order.getElementsByTagName("OrderType").item(0).getTextContent();
            if (orderType.isBlank()){
                throw new SAXException("Missing Data Will Not Accept");
            }

            NodeList itemList = order.getElementsByTagName("Item");
            for (int j = 0; j < itemList.getLength(); j++){
                Element item = (Element)itemList.item(j);
                String type = item.getAttribute("type");
                String priceString = item.getElementsByTagName("Price").item(0).getTextContent();
                String quantityString = item.getElementsByTagName("Quantity").item(0).getTextContent();

                if (type.isBlank() || priceString.isBlank() || quantityString.isBlank()){
                    throw new SAXException("Missing Data Will Not Accept");
                }

                double price = Double.parseDouble(priceString);
                int quantity = Integer.parseInt(quantityString);
                FoodItem currentFood = new FoodItem(type, quantity, price);
                allFood.add(currentFood);
            }

            Instant orderedTime = Instant.now();
            Long convertedToMili = orderedTime.toEpochMilli();
            OrderType enumType = xmlParseOrderType(orderType);
            if (enumType == null) enumType = OrderType.PICKUP; // Default fallback
            Order currentO = new Order(allFood, OrderStatus.INCOMING, convertedToMili, enumType);
            allOrder.add(currentO);
        }
        return allOrder;
    }

    private OrderType xmlParseOrderType(String type){
        if (type == null) return null;
        try {
            return OrderType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


}
