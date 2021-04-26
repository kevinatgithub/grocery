package app.kevs.biyang.grocery.libs.models

import io.realm.RealmObject

open class ItemAlternative(var _id : String? = null, var itemId : String? = null, var description : String? = null, var img : String? = null, var thumbsUp : Int? = 0) : RealmObject()