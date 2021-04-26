package app.kevs.biyang.grocery.libs.api.endpoints

import app.kevs.biyang.grocery.libs.api.CommonResult
import app.kevs.biyang.grocery.libs.api.GroceryApiResult
import app.kevs.biyang.grocery.libs.models.GroceryItem
import io.reactivex.Observable
import retrofit2.http.*

interface GroceryApiService {

    @GET("lists")
    fun getList() : Observable<GroceryApiResult.Result>

    @POST("lists")
    @FormUrlEncoded
    fun addItem(@Field("name") name : String,
                @Field("quantity") quantity : String,
                @Field("quantityType") quantityType : String?,
                @Field("category") category : String?,
                @Field("remarks") remarks : String?,
                @Field("img") img : String?,
                @Field("isComplete") isComplete : Boolean,
                @Field("order") order : Int?) : Observable<CommonResult.Result>

    @PUT("item/{id}")
    @FormUrlEncoded
    fun updateItem(@Path("id") id : String?,
                   @Field("name") name : String,
                   @Field("quantity") quantity : String,
                   @Field("quantityType") quantityType : String?,
                   @Field("category") category : String?,
                   @Field("remarks") remarks : String?,
                   @Field("img") img : String?,
                   @Field("imgUrl") imgUrl : String?,
                   @Field("isComplete") isComplete : Boolean,
                   @Field("order") order : Int?) : Observable<CommonResult.Result>

    @DELETE("item/{id}")
    fun deleteItem(@Path("id") id : String?) : Observable<CommonResult.Result>

    @DELETE("lists")
    fun clearItems() : Observable<CommonResult.Result>

    @POST("/setlist")
    @FormUrlEncoded
    fun setList(@Field("groceries") groceries : String) : Observable<CommonResult.Result>

    @GET("/item/{id}")
    fun getItem(@Path("id") id : String?) : Observable<CommonResult.ItemResult>

    @GET("/alt/{itemId}")
    fun getAlternativeItems(@Path("itemId") itemId : String?) : Observable<CommonResult.AlternativeItemQueryResult>

    @PUT("/alt/{itemId}")
    @FormUrlEncoded
    fun updateAlternativeItems(@Path("itemId") itemId : String?,
                               @Field("_id") _id : String?,
                               @Field("description") description: String?,
                               @Field("img") img : String?,
                               @Field("thumbsUp") thumbsUp : Int?) : Observable<CommonResult.Result>


    @POST("/alt/{itemId}")
    @FormUrlEncoded
    fun addAlternativeItem(@Path("itemId") itemId: String?,
                           @Field("description") description: String?,
                           @Field("img") img : String?,
                           @Field("thumbsUp") thumbsUp : Int?) : Observable<CommonResult.Result>

    @DELETE("/alt/{itemId}")
    fun clearAlternativeItems(@Path("itemId") itemId: String?) : Observable<CommonResult.Result>

    @DELETE("/alt/{id}")
    fun deleteAlternativeItem(@Path("id") id: String?): Observable<CommonResult.Result>

    @GET("/itemlist")
    fun getListNames() : Observable<CommonResult.ListNames>

    @GET("/itemlist/{listName}")
    fun getItemsFromList(@Path("listName") listName: String?) : Observable<GroceryApiResult.Result>

    @POST("/itemlist/{listName}")
    fun assignList(@Path("listName") listName : String?) : Observable<CommonResult.Result>

    @DELETE("/itemlist/{listName}")
    fun deleteList(@Path("listName") listName : String?) : Observable<CommonResult.Result>
}