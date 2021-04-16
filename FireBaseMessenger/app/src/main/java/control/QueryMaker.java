package control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.MessengerAPI;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mercer on 17.03.2018.
 */

public class QueryMaker {

    /**
     * @return реалізований інтерфейс детреба буде вказати тіки метод api
     */
    public MessengerAPI getApiInterface() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        /*Тут включаэємо ретрофіт,
        * фігачим урл(базовий), сам запрос реалізується ниже,
        * Ставим gson конвертер*/
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.ok.uz.ua/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        //Реалізуємо наш інтерфейс, в якому будуть нащі запроси
        MessengerAPI messengerApi = retrofit.create(MessengerAPI.class);

        return messengerApi;
    }
}
