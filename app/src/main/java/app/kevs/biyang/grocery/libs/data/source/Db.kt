package app.kevs.biyang.grocery.libs.data.source

import android.content.Context
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.models.Category
import app.kevs.biyang.grocery.libs.models.GroceryItem
import app.kevs.biyang.grocery.libs.models.ItemAlternative
import app.kevs.biyang.grocery.libs.data.DataSource
import app.kevs.biyang.grocery.libs.models.InListItem
import io.realm.Realm

class Db(context: Context) : DataSource {

    val realm : Realm

    init {
        // Initialize Realm (just once per application)
        Realm.init(context)

        // Get a Realm instance for this thread
        this.realm = Realm.getDefaultInstance()
    }

    override fun getList(
        onSuccess: (items: ArrayList<GroceryItem>) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        val items = realm.where(GroceryItem::class.java).findAll()
        val data = ArrayList<GroceryItem>()
        data.addAll(items)
        onSuccess(data)
    }

    override fun addItem(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        item._id = Helper.makeid(20)
        realm.insertOrUpdate(item)
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun flagItemAsComplete(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        item.isComplete = true
        realm.insertOrUpdate(item)
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun updateItem(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        val realmItem = realm.where(GroceryItem::class.java).equalTo("_id",item._id).findFirst()
        realmItem?.apply {
            name = item.name
            quantity = item.quantity
            quantityType = item.quantityType
            category = item.category
            remarks = item.remarks
            img = item.img
            isComplete = item.isComplete
            order = item.order
        }
        realm.insertOrUpdate(realmItem)
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun deleteItem(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        val alts = realm.where(ItemAlternative::class.java).equalTo("itemId",item._id).findAll()
        item.deleteFromRealm()
        alts.deleteAllFromRealm()
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun clearAll(
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        val rows = realm.where(GroceryItem::class.java).findAll()
        rows.deleteAllFromRealm()
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun syncList(
        list: ArrayList<GroceryItem>,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        val rows = realm.where(GroceryItem::class.java).findAll()
        rows.deleteAllFromRealm()
        realm.copyToRealm((list))
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun getAlternativeItems(
        itemId: String?,
        onResult: (result: ArrayList<ItemAlternative>?) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        val result = realm.where(ItemAlternative::class.java).equalTo("itemId", itemId).findAll()
        val data = arrayListOf<ItemAlternative>()
        data.addAll(result)
        onResult(data)
    }

    override fun addAlternativeItem(
        item: ItemAlternative,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        item._id = Helper.makeid(20)
        realm.insertOrUpdate(item)
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun clearAlternativeItems(
        itemId: String?,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        val items = realm.where(ItemAlternative::class.java).equalTo("itemId",itemId).findAll()
        items.deleteAllFromRealm()
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun getItem(
        itemId: String?,
        onSuccess: (item: GroceryItem?) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        val item = realm.where(GroceryItem::class.java).equalTo("_id",itemId).findFirst()
        onSuccess(item)
    }

    override fun voteAlternative(
        itemId: String?,
        item: ItemAlternative,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        item.thumbsUp = item.thumbsUp?.plus(1) ?: 1
        realm.insertOrUpdate(item)
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun deleteAlternative(
        item: ItemAlternative,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        item.deleteFromRealm()
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun getListNames(
        onSuccess: (listNames: ArrayList<String>) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        val listNames = realm.where(InListItem::class.java).distinct("listName").findAll()
        val result = ArrayList<String>()
        listNames.map {
            result.add(it.listName!!)
        }
        onSuccess(result)
    }

    override fun getItemsFromList(
        listName: String,
        onSuccess: (items: ArrayList<GroceryItem>) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        var realmResult = realm.where(InListItem::class.java).equalTo("listName", listName).findAll()
        var result = ArrayList<GroceryItem>()
        realmResult.map {
            result.add(GroceryItem(it._id,it.name,it.quantity,it.quantityType,it.category,it.remarks,
                it.img,it.imgUrl,it.isComplete,it.order))
        }
        onSuccess(result)
    }

    override fun assignList(
        listName: String,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        val items = realm.where(GroceryItem::class.java).findAll()
        var toAssign = ArrayList<InListItem>()
        items.map {
            val item = InListItem(it._id, it.name, it.quantity, it.quantityType, it.category, it.remarks, it.img, it.imgUrl, it.isComplete, it.order, listName)
            toAssign.add(item)
        }
        realm.beginTransaction()
        realm.copyToRealm(toAssign)
        realm.commitTransaction()
        onSuccess("ok")
    }

    override fun deleteList(
        listName: String,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        realm.beginTransaction()
        val toDelete = realm.where(InListItem::class.java).equalTo("listName",listName).findAll()
        toDelete.deleteAllFromRealm()
        realm.commitTransaction()
        onSuccess("ok")
    }

    fun getCategories(callback: (categories: ArrayList<Category>) -> Unit) {
        var categories = arrayListOf<Category>()
        categories.addAll(realm.where(Category::class.java).findAll())
        callback(categories)
    }

    fun addCategory(category: String, callback: (status: String) -> Unit) {
        realm.beginTransaction()
        val cat = Category(category)
        realm.copyToRealm(cat)
        realm.commitTransaction()
        callback(category)
    }

    fun deleteCategory(category: String, callback: (status: String) -> Unit) {
        realm.beginTransaction()
        val cat = realm.where(Category::class.java).equalTo("category", category).findFirst()
        cat?.deleteFromRealm()
        realm.commitTransaction()
        callback(category)
    }

    fun clearCategories(callback: (status: String) -> Unit) {
        realm.beginTransaction()
        val cats = realm.where(Category::class.java).findAll()
        cats.deleteAllFromRealm()
        realm.commitTransaction()
        callback("ok")
    }
}