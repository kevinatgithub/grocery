package app.kevs.biyang.grocery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import app.kevs.biyang.grocery.libs.models.Category
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.data.DataManager
import com.bakhtiyor.gradients.Gradients
import kotlinx.android.synthetic.main.activity_alternative_items.*
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.android.synthetic.main.activity_categories.fab_back

class Categories : AppCompatActivity() {

    var dataManager : DataManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        dataManager = DataManager(this)

        refreshList()

        swiperefresh_category.setOnRefreshListener {
            refreshList()
            swiperefresh_category.isRefreshing = false
        }

        btn_add_category.setOnClickListener {
            if (!txt_category.text.toString().isEmpty()){
                Helper.toast(this, "saving..")
                dataManager?.addCategory(txt_category.text.toString().trim()) {
                    Helper.toast(this, "Category $it has been added", true)
                    refreshList()
                    txt_category.setText("")
                }
            }
        }

        lv_categories.setOnItemClickListener { parent, view, position, id ->
            val cat = lv_categories.getItemAtPosition(position)

            Helper.showAlert(this, "Delete Category?"){
                dataManager?.deleteCategory(cat.toString()){
                    Helper.toast(this,"Category $it has been deleted")
                    refreshList()
                }
            }
        }

        fab_back.setOnClickListener {
            finish()
        }

        supportActionBar?.setBackgroundDrawable(Gradients.nightFade())
        actionBar?.setBackgroundDrawable(Gradients.nightFade())
        categories_container.background = Gradients.rainyAshville()
    }

    private fun refreshList() {
        dataManager?.getCategories {
            populateList(it)
        }
    }

    private fun populateList(it: ArrayList<Category>) {
        val catergories = mutableListOf<String>()
        for(cat in it){
            catergories.add(cat.category.toString())
        }
        lv_categories.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, catergories)
    }
}
