package solutis.com.br.babyberry.solutis.com.br.babyberry.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Elvis Teles on 02/07/2017.
 */

public interface ApiInterface {

    @POST("api/v1/token")
    Call<Void> enviarToken(@Body String token);
}
