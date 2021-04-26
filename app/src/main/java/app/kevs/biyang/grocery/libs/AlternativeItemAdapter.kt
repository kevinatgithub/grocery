package app.kevs.biyang.grocery.libs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import app.kevs.biyang.grocery.R
import app.kevs.biyang.grocery.libs.models.ItemAlternative
import kotlinx.android.synthetic.main.row_alternative_item.view.*

class AlternativeItemAdapter(val context: Context,
                             val items : ArrayList<ItemAlternative>,
                             val onClick : (item : ItemAlternative) -> Unit,
                             val onLongClick : (item : ItemAlternative) -> Unit) : BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val v = inflater.inflate(R.layout.row_alternative_item, parent, false)
        val item = items[position]

        if (item.img != null){
            v.imageView.setImageBitmap(Helper.decodeBase64ToBitmap(item.img!!))
        }else{
            v.imageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_info))
        }
        v.lbl_description.text = item.description
        v.lbl_thumbsups.text = "${item.thumbsUp.toString()}"
        v.setOnClickListener {
            onClick(item)
        }
        v.setOnLongClickListener {
            onLongClick(item)
            true
        }
        return v
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}