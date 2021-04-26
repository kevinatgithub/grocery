package app.kevs.biyang.grocery.libs.api

import app.kevs.biyang.grocery.libs.models.GroceryItem

object GroceryApiResult {
    data class Result(val data: ArrayList<GroceryItem>)
}