package app.kevs.biyang.grocery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import app.kevs.biyang.grocery.libs.Helper
import app.kevs.biyang.grocery.libs.ImageResultAdapter
import app.kevs.biyang.grocery.libs.api.SearchImageResult
import app.kevs.biyang.grocery.libs.data.DataManager
import app.kevs.biyang.grocery.libs.models.AppPreference
import co.metalab.asyncawait.async
import com.bakhtiyor.gradients.Gradients
import com.chibatching.kotpref.Kotpref
import kotlinx.android.synthetic.main.activity_search_image.*


class SearchImage : AppCompatActivity() {

    var dataManager : DataManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_image)
        Kotpref.init(this)
        supportActionBar?.setBackgroundDrawable(Gradients.nightFade())
        actionBar?.setBackgroundDrawable(Gradients.nightFade())
        search_image_container.background = Gradients.rainyAshville()

        val initKeyword = intent.getStringExtra("KEYWORD")
        if (!initKeyword.isNullOrEmpty()){
            txt_keyword.setText(initKeyword)
            performSearch()
        }

        dataManager = DataManager(this)
        rv_image_results.layoutManager = GridLayoutManager(this, 2)

        btn_search_img.setOnClickListener {
            if (!txt_keyword.text.isNullOrEmpty()){
                performSearch()
            }
        }


    }

    private fun performSearch() {

        if (txt_keyword.text.isNullOrEmpty()){
            return
        }

        async {
            val imageSearchResult = ArrayList<SearchImageResult.ImageResult>()
            val imgsUrl = await { Helper.getRelatedImagesUrlFromWeb(txt_keyword.text.toString().trim(), 10) }
            imgsUrl.map {
                val bmp = await { Helper.getBitmapFromUrl(it) }
                if (bmp != null){
                    imageSearchResult.add(SearchImageResult.ImageResult(bmp, it, {Helper.encodeImageToBase64(it) }){ Helper.getResizedBitmap(it, 100) })
                }
            }
            populateList(imageSearchResult)
        }
    }

    private fun populateList(result: ArrayList<SearchImageResult.ImageResult>) {
        val sanitized = result //sanitize(result)
        rv_image_results.adapter = ImageResultAdapter(this,sanitized,
            onClick = {
                AppPreference.itemImgBase64 = it.base64!!
                val i = Intent()
                i.putExtra("IMG_URL", it.url)
                setResult(Activity.RESULT_OK, i)
                finish()
            })
    }
}
