package net.konyan.ntlmauthentication;

import net.konyan.ntlmauthenticator.NtlmAuthenticator;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by User on 2/3/2017.
 */

public class Networking {

    private static Networking networking;

    public static Networking create(){
        if (networking != null){
            return networking;
        }
        networking = new Networking();
        return networking;
    }

    public Call<ResponseBody> getListService (String url, String name, String pass, String domain) {
        return execute(url, name, pass, domain)
                .create(SharePointListService.class)
                .getListService("http://schemas.microsoft.com/sharepoint/soap/GetListCollection",
                        "GetListCollection",
                        getListSoap());
    }

    private Retrofit execute(String url, String name, String pass, String domain) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(3, TimeUnit.MINUTES)
                .authenticator(NtlmAuthenticator.create(name, pass, domain))
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .build();
    }

    public interface SharePointListService {
        @Headers({
                "Content-Type: text/xml",
                "Accept-Charset: utf-8"
        })
        @POST("_vti_bin/lists.asmx")
        Call<ResponseBody> getListService(@Header("SOAPAction")String actionHeader,
                                          @Query("op")String method,
                                          @Body RequestBody bodyStr);
    }

    public static RequestBody getListSoap() {

        String soapStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <GetListCollection xmlns=\"http://schemas.microsoft.com/sharepoint/soap/\" />\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>";

        return RequestBody.create(MediaType.parse("text/xml"), soapStr);
    }
}
