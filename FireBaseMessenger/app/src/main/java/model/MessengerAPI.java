package model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Mercer on 17.03.2018.
 */

public interface MessengerAPI {

    @GET("push_notifications.php")
    Call<ResponseFromServer> pushNotifications(@Query("token") String token,
                                               @Query("title") String title,
                                               @Query("profilePhoto") String profilePhoto,
                                               @Query("value") String value);
}
