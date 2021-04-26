package app.kevs.biyang.grocery.libs.models

import android.graphics.Bitmap
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class GroceryItem (
                        var _id : String? = null,
                        var name: String? = null,
                        var quantity : Int = 1,
                        var quantityType : String? = null,
                        var category : String? = null,
                        var remarks: String? = null,
                        var img : String? = null,
                        var imgUrl : String? = null,
                        var isComplete : Boolean? = false,
                        var order : Int? = 0) :
    RealmObject()