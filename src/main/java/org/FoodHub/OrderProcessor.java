package org.FoodHub;

import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    private final FileAccesser fileAccesser = new FileAccesser();

    public OrderProcessor(){
    }

    public List<Order> processAllOrder() throws IOException {
        List<String> allFiles = fileAccesser.fetchOrderFolderList();
        List<Order> allOrder = new ArrayList<>();

        if (allFiles.isEmpty()) {
            System.out.println("No order to process");
            return allOrder;
        }
        for (String file : allFiles){
            try {
                allOrder.addAll(processSingleOrder(file));
                fileAccesser.moveProcessedFile(file);
            } catch (Exception e){
                String errorMessage = e.toString();
                System.err.println("Failed to process order: " + file + " for " + errorMessage);
                try{
                    fileAccesser.moveErrorFile(file);
                } catch(IOException e2){
                    e2.printStackTrace();
                }
            }
        }
        return allOrder;
    }

    public List<Order> processSingleOrder(String fileName) throws IOException,ParseException,ParserConfigurationException,SAXException{
        System.out.println("Test Single Order");
        File orderFile;
        String fileExtension;
        if (!fileName.equals("SavedDataForLoad.json")) {
            orderFile = new File("orders" + File.separator + fileName);
            fileExtension = fileAccesser.getExtension(fileName);
        }
        else{
            orderFile = new File(fileName);
            fileExtension = fileAccesser.getExtension(fileName);
        }

        OrderParserInterface parser = AbstractedOrderParserFactory.getParser(fileExtension);
        if (parser != null){
            return parser.loadToOrder(orderFile);
        }
        return List.of();
    }

    public void writeToJSON(Order theOrder){
        jsonOrderParser parser = jsonOrderParser.INSTANCE;
        parser.writeOrderToJSON(theOrder);
    }

    public void writeAllOrdersToFile(List<Order> allOrders){
        jsonOrderParser parser = jsonOrderParser.INSTANCE;
        parser.writeAllOrderToFile(allOrders);
    }


}
