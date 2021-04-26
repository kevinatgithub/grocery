package app.kevs.biyang.grocery.libs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kevs.biyang.grocery.R
import app.kevs.biyang.grocery.libs.models.GroceryItem
import kotlinx.android.synthetic.main.row_sort_item.view.*

class SortItemAdapter(val ctx : Context,
                      val list : ArrayList<GroceryItem>,
                      val onClick : (item : GroceryItem, position : Int) -> Unit) : RecyclerView.Adapter<SortViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortViewHolder {
        return SortViewHolder(LayoutInflater.from(ctx).inflate(R.layout.row_sort_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SortViewHolder, position: Int) {
        val item = list.get(position)
        holder?.apply {
            sort_number?.text = item.order.toString()
            name?.text = item.name.toString()
        }

        holder?.container.setOnClickListener {
            holder?.sort_number?.text = (item?.order?.plus(1)).toString()
            item?.order?.plus(1)
            onClick(item, position)
        }
    }
}

class SortViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val sort_number = view.txt_sort_number
    val name = view.txt_name
    val container = view.card_sort_row
}