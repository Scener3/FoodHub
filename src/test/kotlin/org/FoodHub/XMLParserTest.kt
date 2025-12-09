package org.FoodHub

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.xml.sax.SAXException
import java.io.File
import java.nio.file.Path

class XMLParserTest {

    @TempDir
    lateinit var tempDir: Path

    private fun writeXmlFile(name: String, xml: String): File {
        val file = tempDir.resolve(name).toFile()
        file.writeText(xml.trimIndent())
        return file
    }

    @Test
    fun `parse valid XML with single order`() {
        val xml = """
            <Orders>
              <Order>
                <OrderType>pickup</OrderType>
                <Item type="Burger">
                    <Price>5.99</Price>
                    <Quantity>2</Quantity>
                </Item>
              </Order>
            </Orders>
        """

        val file = writeXmlFile("singleOrder.xml", xml)
        val result = XMLParser.loadToOrder(file)

        Assertions.assertEquals(1, result.size)
        val order = result[0]

        Assertions.assertEquals(OrderStatus.INCOMING, order.orderStatus)
        Assertions.assertEquals(OrderType.PICKUP, order.orderType)
        Assertions.assertEquals(1, order.foodItems.size)

        val item = order.foodItems[0]
        Assertions.assertEquals("Burger", item.name)
        Assertions.assertEquals(2, item.quantity)
        Assertions.assertEquals(5.99, item.price)
    }

    @Test
    fun `missing order type throws exception`() {
        val xml = """
            <Orders>
              <Order>
                <OrderType></OrderType>
                <Item type="Burger">
                    <Price>5.99</Price>
                    <Quantity>2</Quantity>
                </Item>
              </Order>
            </Orders>
        """

        val file = writeXmlFile("missingType.xml", xml)
        assertThrows<SAXException> { XMLParser.loadToOrder(file) }
    }

    @Test
    fun `missing item fields throws exception`() {
        val xml = """
            <Orders>
              <Order>
                <OrderType>PICKUP</OrderType>
                <Item type="">
                    <Price>5.99</Price>
                    <Quantity>2</Quantity>
                </Item>
              </Order>
            </Orders>
        """

        val file = writeXmlFile("missingItemField.xml", xml)
        assertThrows<SAXException> { XMLParser.loadToOrder(file) }
    }

    @Test
    fun `parse multiple orders`() {
        val xml = """
            <Orders>
              <Order>
                <OrderType>PICKUP</OrderType>
                <Item type="Burger"><Price>5</Price><Quantity>1</Quantity></Item>
              </Order>
              <Order>
                <OrderType>DELIVERY</OrderType>
                <Item type="Fries"><Price>2</Price><Quantity>3</Quantity></Item>
              </Order>
            </Orders>
        """

        val file = writeXmlFile("multi.xml", xml)
        val result = XMLParser.loadToOrder(file)

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals("Burger", result[0].foodItems[0].name)
        Assertions.assertEquals("Fries", result[1].foodItems[0].name)
    }
}