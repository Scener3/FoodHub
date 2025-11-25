package org.FoodHub;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Threading Service
 *  Will monitor orders directory for any input, if a file gets added into the orders folder,
 *  it will parse that file and add into the UI, all of this will happen in the background
 *  to prevent the UI from freezing
 *
 * */

public class FetchFilesService implements Runnable{

    private final ObservableList<Order> allOrdersList;
    private final OrderManager orderManager;
    private final OrderProcessor processor;
    private final FileAccesser accesser;
    private final Runnable priceUpdateCallBack;

    private volatile boolean run = true;
    private volatile WatchService ping;

    public FetchFilesService(ObservableList<Order> allOrdersList, OrderManager orderManager,
                             OrderProcessor processor, FileAccesser accesser, Runnable priceUpdateCallBack){
        this.allOrdersList = allOrdersList;
        this.orderManager = orderManager;
        this.processor = processor;
        this.accesser = accesser;
        this.priceUpdateCallBack = priceUpdateCallBack;
    }

    public void stop(){
        run = false;
        try{
            if (ping != null){
                ping.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Runs in the background and checks for any file being added
     *
     * **/
    @Override
    public void run(){
        try(WatchService ping = FileSystems.getDefault().newWatchService()){
            this.ping = ping;
            Path dir = Paths.get("orders");

            dir.register(ping, StandardWatchEventKinds.ENTRY_CREATE);
            while(run){
                WatchKey key;
                try{
                    key = ping.take();
                } catch(InterruptedException|ClosedWatchServiceException e){
                    if (!run){
                        System.out.println("Ping service stopping");
                        break;
                    }
                    continue;
                }
                for (WatchEvent<?> event : key.pollEvents()){
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW){
                        continue;
                    }

                    WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>)event;
                    Path fileNamePath = pathWatchEvent.context();
                    String fileName = fileNamePath.toString();

                    if (fileName.endsWith(".json") || fileName.endsWith(".xml")){
                        try{
                            List<Order> newOrders = processor.processSingleOrder(fileName);
                            accesser.moveProcessedFile(fileName);

                            if (newOrders != null && !newOrders.isEmpty()){
                                Platform.runLater(()->{
                                    for (Order o : newOrders){
                                        orderManager.addOrder(o);
                                    }
                                    allOrdersList.addAll(newOrders);
                                    priceUpdateCallBack.run();
                                });
                            }
                        }catch(Exception e){
                            System.err.println("Failed to process file");
                            e.printStackTrace();
                            try{
                                accesser.moveErrorFile(fileName);
                            }catch (IOException ioE){
                                ioE.printStackTrace();
                            }
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid){
                    System.err.println("Invalid Key");
                    run = false;
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }


    }
}
