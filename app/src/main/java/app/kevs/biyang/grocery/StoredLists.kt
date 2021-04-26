package app.kevs.biyang.grocery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.data.DataManager
import com.bakhtiyor.gradients.Gradients
import kotlinx.android.synthetic.main.activity_stored_lists.*

class StoredLists : AppCompatActivity() {

    var LIST_NAMES : ArrayList<String>? = null
    set(value) {
        field = value
        if (value != null){
            lv_listnames.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, value)
        }
    }

    var dataManager: DataManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stored_lists)

        supportActionBar?.setBackgroundDrawable(Gradients.nightFade())
        actionBar?.setBackgroundDrawable(Gradients.nightFade())
        stored_lists_container.background = Gradients.rainyAshville()

        dataManager = DataManager(this)
        getListNames()

        lv_listnames.setOnItemClickListener { parent, view, position, id ->
            val listName = LIST_NAMES?.get(position)
            if (!listName.isNullOrEmpty()){
                confirmLoadList(listName)
            }
        }

        lv_listnames.setOnItemLongClickListener(AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val listName = LIST_NAMES?.get(position)
            if (!listName.isNullOrEmpty()){
                Helper.showAlert(this, "Delete List?"){
                    deleteList(listName)
                }
            }
            true
        })
    }

    private fun deleteList(listName: String) {
        dataManager?.deleteList(listName,
            onSuccess = {
                Helper.toast(this, "List ${listName} has been deleted", true)
                getListNames()
            }){handleError(it)}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_common_back, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_back -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmLoadList(listName: String) {
        Helper.showAlert(this, "Load ${listName} ? This will delete all current active list.",
            onPositive = {
                loadList(listName)
            })
    }

    private fun loadList(listName: String) {
        Helper.toast(this, "Clearing List")
        dataManager?.clearAll(
            onSuccess = {
                Helper.toast(this, "Loading List")
                dataManager?.getItemsFromList(listName, onSuccess = {
                    dataManager?.setList(it,
                        onSuccess = {
                            Helper.toast(this, "List has been loaded",true)
                            finish()
                        }){handleError(it)}
                }){handleError(it)}
            }
        ){handleError(it)}
    }

    private fun getListNames() {
        dataManager?.getListNames(onSuccess = {
            LIST_NAMES = it
        }){
            handleError(it)
        }
    }

    private fun handleError(error: Throwable) {
        Helper.toast(this, error.message.toString(), true)
    }
}
