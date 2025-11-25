package org.FoodHub


/**
 * Class to store the attributes of an item of food.
 */
data class FoodItem
/**
 * Creates a new FoodItem with the given attributes.
 *
 * @param name - name of the food.
 * @param quantity - quantity of the food.
 * @param price - price per-item of food.
 */(
    val name: String, val quantity: Int,
    /**
     * @return the price of a FoodItem.
     */
    val price: Double
)


