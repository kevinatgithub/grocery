package app.kevs.biyang.grocery.libs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import app.kevs.biyang.grocery.R
import app.kevs.biyang.grocery.libs.models.GroceryItem
import co.metalab.asyncawait.async
import kotlinx.android.synthetic.main.row_grocery_item.view.*

class GroceryItemAdapter(val items: ArrayList<GroceryItem>,
                         val context: Context,
                         val onRowClick: (item : GroceryItem) -> Unit,
                         val onComplete : (item : GroceryItem) -> Unit,
                         val onAlternative : (item : GroceryItem) -> Unit,
                         val onUpdate : (item : GroceryItem) -> Unit,
                         val onDelete : (item : GroceryItem) -> Unit) : RecyclerView.Adapter<ViewHolder>(){
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_grocery_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val groceryItem = items.get(position)

        holder?.apply {
            name.setText(groceryItem.name?.capitalize())
            remarks.setText("${groceryItem.quantity} ${groceryItem.quantityType ?: ""}  ${groceryItem.category ?: ""} ${groceryItem.remarks ?: "No Remarks"}")
            options.setOnClickListener {
                makePopup(it, options, groceryItem)
            }
            container.setOnClickListener {
                onRowClick(groceryItem)
            }
            if (groceryItem.imgUrl != null) {
                val url = groceryItem.imgUrl!!
                async {
                    val bitmap = await { Helper.getBitmapFromUrl(url) }
                    if (bitmap != null)
                        img.setImageBitmap(bitmap)
                }
            }else if (!groceryItem.img.isNullOrEmpty()){
                img.setImageBitmap(Helper.decodeBase64ToBitmap(groceryItem.img!!))
            }else{
                img.setImageDrawable(context.resources.getDrawable(R.drawable.ic_info))
            }
            if (groceryItem.isComplete!!)
                isComplete.visibility = View.VISIBLE
        }
    }

    private fun makePopup(it: View?, options: TextView?, groceryItem: GroceryItem) {
        val popup = PopupMenu(context, options)
        popup.inflate(R.menu.menu_grocery_item_row)
        popup.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.grocery_menu_complete -> flagAsComplete(groceryItem)
                R.id.grocery_menu_update -> {
                    onUpdate(groceryItem)
                    true
                }
                R.id.grocery_menu_delete -> {
                    onDelete(groceryItem)
                    true
                }
                R.id.grocery_menu_alternatives -> onAlternativeClick(groceryItem)
                else -> TODO()
            }
        }
        popup.show()
    }

    private fun onAlternativeClick(item: GroceryItem): Boolean {
        onAlternative(item)
        return true
    }

    private fun flagAsComplete(groceryItem: GroceryItem): Boolean {
        onComplete(groceryItem)
        return true
    }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
    val name = view.lbl_name
    val remarks = view.lbl_remarks
    val options = view.lbl_options
    val img = view.imageView
    val isComplete = view.img_check
    val container = view.cv_grocery_item
}