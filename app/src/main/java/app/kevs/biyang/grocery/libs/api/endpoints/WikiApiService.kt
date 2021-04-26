package app.kevs.biyang.grocery.libs.api.endpoints

import app.kevs.biyang.grocery.libs.api.Model
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface WikiApiService {

    @GET("api.php")
    fun hitCountCheck(@Query("action") action: String,
                      @Query("format") format: String,
                      @Query("list") list: String,
                      @Query("srsearch") srsearch: String):
            Observable<Model.Result>
}