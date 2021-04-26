package app.kevs.biyang.grocery

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import app.kevs.biyang.grocery.libs.models.GroceryItem
import app.kevs.biyang.grocery.libs.GroceryItemAdapter
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.PreviewDialog
import app.kevs.biyang.grocery.libs.data.DataManager
import co.metalab.asyncawait.async
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(){

    companion object{
        var APP_MODE = MODE.OFFLINE
    }

    var dataManager : DataManager? = null

    var txt_name_value : String? = null

    var GROCERYLISTS = ArrayList<GroceryItem>()
    set(value) {
        val filtered = ArrayList<GroceryItem>()
        filtered.addAll(value.filter {
            !it.isComplete!!
        })
        field = if (COMPLETED_VISIBLE){
            value
        }else{
            filtered
        }

        if (value != null){
            populateList()
        } else {
            setState(State.HAS_NO_RESULT)
            Helper.toast(this, "No items yet")
        }
    }

    var myOptionsMenu : Menu? = null

    var COMPLETED_VISIBLE = true
    set(value){
        field = value
        if (value){
            myOptionsMenu?.getItem(1)?.setIcon(resources.getDrawable(R.drawable.ic_visible))
        }else{
            myOptionsMenu?.getItem(1)?.setIcon(resources.getDrawable(R.drawable.ic_visible))

        }
    }

    var ADD_FORM_VISIBLE = false
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    set(value){
        field = value
        TransitionManager.beginDelayedTransition(main_container)
        if (value){
            ll_add_item.visibility = View.VISIBLE
        }else{
            ll_add_item.visibility = View.GONE
        }
    }

    var BACKUP_FILE : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshTitle()

        dataManager = DataManager(this)
        BACKUP_FILE = File("backup.json")

        refreshList()

        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.colorPrimary))
        itemsswipetorefresh.setColorSchemeColors(Color.WHITE)

        itemsswipetorefresh.setOnRefreshListener {
            refreshList()
            itemsswipetorefresh.isRefreshing = false
        }

        btn_add.setOnClickListener {
            if (!txt_name.text.isNullOrEmpty()){
                createNewItem()
            }
        }

        txt_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt_name_value = s.toString()
            }

        })

        supportActionBar?.setBackgroundDrawable(AppConstants.actionBarBackgrounDrawable)
        actionBar?.setBackgroundDrawable(AppConstants.actionBarBackgrounDrawable)
        main_container.background = AppConstants.containerBackgroundDrawable
    }

    private fun createNewItem() {
        Helper.toast(this, "Saving..")
        async {
            val imgResult = await { Helper.getRelatedImagesUrlFromWeb(txt_name.text.toString(), 1) }
            var imgUrl : String? = null
            if (imgResult.size > 0)
                imgUrl = imgResult.get(0)
            dataManager?.addItem(
                GroceryItem(
                null,
                txt_name.text.toString().trim(),
                    1,null,null,null,null,imgUrl, false,0
                ),
                onSuccess = {
                    refreshList()
                    txt_name.setText("")
                    ADD_FORM_VISIBLE = false
                },
                onError = {handleError(it)}
            )
        }
    }

    private fun refreshTitle() {
        when(APP_MODE){
            MODE.ONLINE -> supportActionBar!!.title = "App Online"
            MODE.OFFLINE -> supportActionBar!!.title= "App Offline"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        myOptionsMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.main_menu_add-> toggleAddForm()
            R.id.main_menu_1-> copyDataFromOnline()
            R.id.main_menu_2-> switchMode()
            R.id.main_menu_sort -> startActivity(Intent(this, SortList::class.java))
            R.id.main_menu_save_list -> initSaveToList()
            R.id.main_menu_load_list -> startActivity(Intent(this, StoredLists::class.java))
            R.id.main_menu_toggle_completed -> toggleListType()
            R.id.main_menu_clear-> confirmClearList()
            R.id.main_menu_categories -> startActivity(Intent(this, Categories::class.java))
            R.id.main_menu_backup -> TODO()
            R.id.main_menu_restore -> TODO()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initSaveToList() {
        Helper.prompt(this, "Enter List Name", "IMPORTANT: This will delete all previous content of the list"){
            saveToList(it)
        }
    }

    private fun saveToList(listName: String) {
        dataManager?.deleteList(listName,
            onSuccess = {
                dataManager?.assignList(listName,
                    onSuccess = {
                        Helper.toast(this, "List created", true)
                    },
                    onError = {handleError(it)})
            },
            onError = {handleError(it)})
    }

    private fun toggleAddForm() {
        ADD_FORM_VISIBLE = !ADD_FORM_VISIBLE
    }

    private fun toggleListType() {
        COMPLETED_VISIBLE = !COMPLETED_VISIBLE
        refreshList()
    }

    private fun confirmClearList() {
        Helper.showAlert(this,
            "Are you sure you wan't to clear all items in the list?"){
            clearList()
        }
    }

    private fun clearList() {
        Helper.toast(this, "Clearing list..")
        dataManager?.clearAll(
            onSuccess = {refreshList()},
            onError = {handleError(it)}
        )
        refreshList()
    }

    private fun switchMode() {
        Helper.toast(this, "Switching Mode")
        APP_MODE = when (APP_MODE){
            MODE.ONLINE -> MODE.OFFLINE
            MODE.OFFLINE -> MODE.ONLINE
            else -> MODE.ONLINE
        }
        refreshTitle()
        refreshList()
    }

    private fun copyDataFromOnline() {
        Helper.toast(this, "Copying..")
        val list = GROCERYLISTS
        if (list != null){
            dataManager?.syncList(list,
                onSuccess = {
                    refreshList()
                    val copylocation = if (APP_MODE == MODE.ONLINE) {
                        MODE.OFFLINE
                    }else{
                        MODE.ONLINE
                    }
                    Toast.makeText(this, "Copied to $copylocation", Toast.LENGTH_SHORT).show()
                },
                onError = {handleError(it)})
        }
    }

    override fun onPause() {
        super.onPause()
        dataManager?.dispose()
    }

    override fun onPostResume() {
        super.onPostResume()
        refreshList()
    }

    private fun populateList() {
        if (GROCERYLISTS.size > 0){
            setState(State.HAS_RESULT)
            val sorted = GROCERYLISTS?.sortedWith(compareBy({it.category}, {it.category}))
            val items = ArrayList<GroceryItem>()
            if (sorted != null){
                items.addAll(sorted)
            }

            val adapter = GroceryItemAdapter(items, this,
                onRowClick = {
                    PreviewDialog.show(this, it,
                        onUpdate = {
                            val i = Intent(this, CreateGroceryItem::class.java)
                            i.putExtra("ITEM_ID",it._id)
                            startActivity(i)
                        }){
                        dataManager?.flagItemAsComplete(it,
                            onSuccess = {refreshList()},
                            onError = {handleError(it)})
                    }
                },
                onComplete = {
                    dataManager?.flagItemAsComplete(it,
                        onSuccess = {},
                        onError = {handleError(it)})
                },
                onAlternative = {
                    val i = Intent(this, AlternativeItems::class.java)
                    i.putExtra("ITEM_ID", it._id)
                    startActivity(i)
                },
                onUpdate = {
                    val i = Intent(this, CreateGroceryItem::class.java)
                    i.putExtra("ITEM_ID",it._id)
                    startActivity(i)
                },
                onDelete = {
                    val b = AlertDialog.Builder(this)
                    b.setMessage("Delete item from list?")
                    b.setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, which -> dataManager?.deleteItem(it,
                        onSuccess = {refreshList()},
                        onError = {handleError(it)}) })
                    b.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    val a = b.create()
                    a.setOnShowListener {
                        a.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                        a.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                    }
                    a.show()
                })
            rv_grocery_items.layoutManager = LinearLayoutManager(this)
            rv_grocery_items.adapter = adapter
        }else{
            setState(State.HAS_NO_RESULT)
        }
    }

    private fun refreshList(){
        setState(State.LOADING)

        dataManager?.getList(
            onSuccess = {
                GROCERYLISTS = it
            },
            onError = {
                handleError(it)
            }
        )

    }

    private fun setState(state: State){
        when(state){
            State.LOADING ->{
                rv_grocery_items.visibility = View.GONE
                img_info.visibility = View.VISIBLE
                lbl_info.apply{
                    visibility = View.VISIBLE
                    text = "Please wait, loading.."
                }
            }
            State.HAS_RESULT ->{
                rv_grocery_items.visibility = View.VISIBLE
                lbl_info.visibility = View.GONE
                img_info.visibility = View.GONE
            }
            State.HAS_NO_RESULT ->{
                rv_grocery_items.visibility = View.GONE
                lbl_info.apply{
                    visibility = View.VISIBLE
                    text = "No items to display, click + to create"
                }
            }
        }
    }

    private enum class State{
        LOADING, HAS_RESULT, HAS_NO_RESULT
    }

    private fun handleError(error : Throwable) {
        Toast.makeText(this,"error : ${error.message}",Toast.LENGTH_LONG).show()
    }
}

enum class MODE {
    ONLINE, OFFLINE
}

