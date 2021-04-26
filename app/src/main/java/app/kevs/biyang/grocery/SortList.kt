package app.kevs.biyang.grocery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.SortItemAdapter
import app.kevs.biyang.grocery.libs.data.DataManager
import app.kevs.biyang.grocery.libs.models.GroceryItem
import com.bakhtiyor.gradients.Gradients
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sort_list.*

class SortList : AppCompatActivity() {

    var dataManager : DataManager? = null

    var ITEMS = ArrayList<GroceryItem>()
    set(value) {
        field = value
        if (value != null){
            populateList()
        }
    }

    private fun populateList() {
        val sorted = ITEMS?.sortedWith(compareBy({it.order}, {it.order}))
        val items = ArrayList<GroceryItem>()
        if (sorted != null){
            items.addAll(sorted?.reversed())
        }

        lv_sort.adapter = SortItemAdapter(this, items,onClick = {
            item, position ->
            ITEMS?.find { it._id == item?._id }?.order = item?.order
            populateList()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sort_list)

        dataManager = DataManager(this)
        lv_sort.layoutManager = LinearLayoutManager(this)
        fetchList()

        supportActionBar?.setBackgroundDrawable(Gradients.nightFade())
        actionBar?.setBackgroundDrawable(Gradients.nightFade())
        sort_container.background = Gradients.rainyAshville()
    }

    private fun fetchList() {
        dataManager?.getList(onSuccess = {
            ITEMS = it
        }){handleError(it)}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sort_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_sort_save -> saveChanges()
            R.id.menu_sort_cancel -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        dataManager?.dispose()
    }

    private fun saveChanges() {
        dataManager?.setList(ITEMS,
            onSuccess = {
                finish()
            }){handleError(it)}
    }

    private fun handleError(error: Throwable) {
        Helper.toast(this, error.message.toString(), true)
    }
}
