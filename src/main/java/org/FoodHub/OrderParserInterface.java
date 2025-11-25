package org.FoodHub;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface OrderParserInterface {

    List<Order> loadToOrder(File orderFile) throws IOException, ParserConfigurationException, SAXException, ParseException;
}
