package com.uit.minhho.financetracker.data.remote;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("api/detect_transaction.php")
    Call<ResponseBody> sendSmsTransaction(
            @Field("sender") String sender,
            @Field("content") String content,
            @Field("amount") String amount
    );
}
