package app.kevs.biyang.grocery.libs

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import app.kevs.biyang.grocery.R
import app.kevs.biyang.grocery.libs.models.GroceryItem
import co.metalab.asyncawait.async
import com.bakhtiyor.gradients.Gradients


class PreviewDialog {
    companion object{
        fun show(ctx : Context,
                 item: GroceryItem,
                 onUpdate : (item : GroceryItem) -> Unit,
                 onComplete : (item : GroceryItem) -> Unit){
            val dialog = Dialog(ctx)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_preview)
            dialog.setCancelable(true)

            val img = dialog.findViewById(R.id.img_preview) as ImageView?
            val name = dialog.findViewById(R.id.txt_name) as TextView?
            val desc = dialog.findViewById(R.id.txt_description) as TextView?
            val btn_complete = dialog.findViewById(R.id.btn_complete) as Button?
            val btn_update = dialog.findViewById(R.id.btn_update) as Button?
            val btn_close = dialog.findViewById(R.id.btn_close) as Button?

            img?.background = Gradients.premiumDark()
            img?.setImageDrawable(ctx.resources.getDrawable(R.drawable.ic_info))
            name?.text = ""
            desc?.text = ""

            if (!item.imgUrl.isNullOrEmpty()){
                val url = item.imgUrl
                async {
                    val bmp = await { Helper.getBitmapFromUrl(url!!) }
                    img?.setImageBitmap(bmp)
                }
            }else if(!item.img.isNullOrEmpty()){
                img?.setImageBitmap(Helper.decodeBase64ToBitmap(item.img!!))
            }else{
                img?.setImageDrawable(ctx.resources.getDrawable(R.drawable.ic_info))
            }
            name?.text = item.name
            desc?.text = "${item.quantity} ${item.quantityType ?: ""} ${item.remarks ?: ""}"

            btn_complete?.setOnClickListener {
                onComplete(item)
                dialog.dismiss()
            }

            btn_update?.setOnClickListener {
                onUpdate(item)
                dialog.dismiss()
            }

            btn_close?.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}