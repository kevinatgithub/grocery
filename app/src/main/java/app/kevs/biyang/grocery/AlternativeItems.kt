package app.kevs.biyang.grocery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import app.kevs.biyang.grocery.libs.AlternativeItemAdapter
import app.kevs.biyang.grocery.libs.models.GroceryItem
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.models.ItemAlternative
import app.kevs.biyang.grocery.libs.data.DataManager
import co.metalab.asyncawait.async
import com.bakhtiyor.gradients.Gradients
import kotlinx.android.synthetic.main.activity_alternative_items.*
import kotlinx.android.synthetic.main.activity_alternative_items.fab_back
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.android.synthetic.main.activity_main.*

class AlternativeItems : AppCompatActivity() {

    var dataManager : DataManager? = null

    companion object{
        var ITEM : GroceryItem? = null
    }

    var ALTERNATIVE_ITEMS = ArrayList<ItemAlternative>()
    set(value) {
        field = value
        if (value != null){

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alternative_items)

        supportActionBar?.title = MainActivity.APP_MODE.toString()

        val itemId = intent.getStringExtra("ITEM_ID")
        if (itemId.isNullOrEmpty()){
            finish()
        }

        dataManager = DataManager(this)

        dataManager?.getItem(itemId!!,
            onSuccess = {
                ITEM = it
                if (ITEM == null){
                    finish()
                }

                prepareItem()
                refreshList()
                addUIHandlers()
            },
            onError = {handlerError(it)})

        supportActionBar?.setBackgroundDrawable(Gradients.nightFade())
        actionBar?.setBackgroundDrawable(Gradients.nightFade())
        alternatives_container.background = Gradients.rainyAshville()
    }

    private fun handlerError(error: Throwable) {
        Helper.toast(this, error.message!!, true)
    }

    private fun addUIHandlers() {
        swiperefresh_alternatives.setOnRefreshListener {
            refreshList()
            swiperefresh_alternatives.isRefreshing = false
        }

        fab_back.setOnClickListener {
            finish()
        }

        fab_add.setOnClickListener {
            val i = Intent(this, CreateAlternativeItem::class.java)
            i.putExtra("ITEM_ID", ITEM?._id.toString())
            startActivityForResult(i, 0)
        }
    }

    private fun prepareItem() {
        if (!ITEM?.imgUrl.isNullOrEmpty()){
            val url = ITEM?.imgUrl
            async {
                val bmp = await { Helper.getBitmapFromUrl(url!!) }
                img_image.setImageBitmap(bmp)
            }
        }else if (ITEM?.img.isNullOrEmpty()){
            imageView.setImageBitmap(Helper.decodeBase64ToBitmap(ITEM?.img.toString()))
        }
        lbl_name.text = ITEM?.name
        lbl_remarks.text = "${ITEM?.quantity} ${ITEM?.quantityType} | ${ITEM?.category} | ${ITEM?.remarks}"
    }

    override fun onPause() {
        super.onPause()
        dataManager?.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refreshList()
    }

    private fun refreshList() {
        dataManager?.getAlternativeItems(ITEM?._id,
            onResult = {
                val sorted = it?.sortedWith(compareBy({it.thumbsUp}, {it.thumbsUp}))
                val items = ArrayList<ItemAlternative>()
                if (sorted != null){
                    items.addAll(sorted?.reversed())
                }
                populateList(items)
            },
            onError = {handlerError(it)})
    }

    private fun populateList(items: ArrayList<ItemAlternative>) {
        lv_alternatives.adapter = AlternativeItemAdapter(this, items,
            onClick = {confirmVote(it)},
            onLongClick = {
                Helper.showAlert(this, "Delete item?"){
                    proceedDelete(it)
                }
            })
    }

    private fun proceedDelete(it: ItemAlternative) {
        Toast.makeText(this, "Deleting item",Toast.LENGTH_SHORT).show()
        dataManager?.deleteAlternative(it,
            onSuccess = {refreshList()},
            onError = {handlerError(it)})
    }

    private fun confirmVote(item: ItemAlternative) {
        Helper.showAlert(this, "Add 1 vote?"){
            vote(item)
        }
    }

    private fun vote(item: ItemAlternative) {
        Helper.toast(this, "voting..")
        dataManager?.voteAlternative(ITEM?._id, item,
            onSuccess = {refreshList()},
            onError = {handlerError(it)})
    }
}
