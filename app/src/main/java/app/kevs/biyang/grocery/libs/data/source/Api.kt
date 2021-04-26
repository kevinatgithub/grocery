package app.kevs.biyang.grocery.libs.data.source

import app.kevs.biyang.grocery.libs.models.GroceryItem
import app.kevs.biyang.grocery.libs.models.ItemAlternative
import app.kevs.biyang.grocery.libs.api.ApiManager
import app.kevs.biyang.grocery.libs.api.SearchImageResult
import app.kevs.biyang.grocery.libs.data.DataSource
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Api : DataSource {

    val apiService by lazy {
        ApiManager.create()
    }

    var disposable: Disposable? = null

    override fun getList(function: (items : ArrayList<GroceryItem>) -> Unit, onError : (error : Throwable) -> Unit) {
        disposable = apiService.getList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> function(result.data) },
                { error -> onError(error)}
            )
    }

    override fun addItem(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.addItem(item.name.toString(), item.quantity.toString(), item.quantityType, item.category, item.remarks, item.img, item.isComplete!!, item.order)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun flagItemAsComplete(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        item.isComplete = true
        updateItem(item, onSuccess, onError)
    }

    override fun updateItem(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.updateItem(item._id, item.name.toString(), item.quantity.toString(), item.quantityType.toString(), item.category.toString(), item.remarks.toString(), item.img.toString(), item.imgUrl, item.isComplete!!, item.order)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun deleteItem(
        item: GroceryItem,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.deleteItem(item._id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun clearAll(
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.clearItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun syncList(
        list: ArrayList<GroceryItem>,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        val gson = Gson()
        val listJson = gson.toJson(list)
        disposable = apiService.setList(listJson)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun getAlternativeItems(
        itemId: String?,
        onResult: (result: ArrayList<ItemAlternative>?) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.getAlternativeItems(itemId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onResult(result.items) },
                { error -> onError(error)}
            )
    }

    override fun addAlternativeItem(
        item: ItemAlternative,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.addAlternativeItem(item.itemId, item.description, item.img, item.thumbsUp)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun clearAlternativeItems(
        itemId: String?,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.clearAlternativeItems(itemId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun getItem(
        itemId: String?,
        onSuccess: (item: GroceryItem?) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.getItem(itemId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onSuccess(result.item) },
                { error -> onError(error) }
            )
    }

    override fun voteAlternative(
        itemId: String?,
        item: ItemAlternative,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        item.thumbsUp = item.thumbsUp?.plus(1) ?: 1
        disposable = apiService.updateAlternativeItems(itemId, item._id, item.description, item.img, item.thumbsUp)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun deleteAlternative(
        item: ItemAlternative,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.deleteAlternativeItem(item._id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun getListNames(
        onSuccess: (listNames: ArrayList<String>) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.getListNames()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onSuccess(result.listNames) },
                { error -> onError(error)}
            )
    }

    override fun getItemsFromList(
        listName: String,
        onSuccess: (items: ArrayList<GroceryItem>) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.getItemsFromList(listName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onSuccess(result.data)},
                { error -> onError(error)}
            )
    }

    override fun assignList(
        listName: String,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.assignList(listName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    override fun deleteList(
        listName: String,
        onSuccess: (status: String) -> Unit,
        onError: (message: Throwable) -> Unit
    ) {
        disposable = apiService.deleteList(listName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSuccess("ok") },
                { error -> onError(error)}
            )
    }

    fun dispose(){
        disposable?.dispose();
    }
}