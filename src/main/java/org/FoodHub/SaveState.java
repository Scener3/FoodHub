package org.FoodHub;

import java.io.File;

public interface SaveState {
    void save(OrderManager om, File filePath);
    void load(File filePath, OrderManager om);
}
