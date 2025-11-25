package org.FoodHub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileAccesser {
    private List<String> allOrderFiles = new ArrayList<>();
    private String sourceDir = "orders";
    private String processedOrderDir = "orders/processedOrders";
    private String errorOrderDir = "orders/ErrorOrders";

    public FileAccesser(){
        try{
            Files.createDirectories(Paths.get(processedOrderDir));
            Files.createDirectories(Paths.get(errorOrderDir));
        }catch (IOException e){
            System.err.println("Error Creating Directory");
            e.printStackTrace();
        }
    }

    public List<String> fetchOrderFolderList(){
        Path directory = Paths.get(sourceDir);
        try(Stream<Path> stream = Files.list(directory)){
            allOrderFiles = stream.filter(file ->file.toString().endsWith(".json") || file.toString().endsWith(".xml")).map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
            return allOrderFiles;
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("Error");
            return List.of();
        }
    }

    public String getExtension(String fileName){
        int indexOfDot = fileName.lastIndexOf('.');
        if (indexOfDot > 0 && indexOfDot < fileName.length() - 1){
            return fileName.substring(indexOfDot + 1);
        }
        return "";
    }

    private void moveFile(String fileName, Path targetDir) throws IOException {
        Path sourcePath = Paths.get(sourceDir, fileName);
        Path targetPath = targetDir.resolve(fileName);
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public void moveProcessedFile(String fileName) throws IOException {
        moveFile(fileName, Paths.get(processedOrderDir));
    }

    public void moveErrorFile(String fileName) throws IOException {
        moveFile(fileName, Paths.get(errorOrderDir));

    }

}
