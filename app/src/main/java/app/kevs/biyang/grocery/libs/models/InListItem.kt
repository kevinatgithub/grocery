package app.kevs.biyang.grocery.libs.models

import io.realm.RealmObject

open class InListItem(
    var _id: String? = null,
    var name: String? = null,
    var quantity: Int = 1,
    var quantityType: String? = null,
    var category: String? = null,
    var remarks: String? = null,
    var img: String? = null,
    var imgUrl: String? = null,
    var isComplete: Boolean? = false,
    var order: Int? = 0,
    var listName : String? = null
) : RealmObject()