package net.konyan.ntlmauthenticator;

import net.konyan.ntlmauthenticator.apache_ntlm.NTLMEngineImpl;

import java.io.IOException;
import java.util.List;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by User on 2/3/2017.
 */

public class NtlmAuthenticator implements Authenticator {

    private final NTLMEngineImpl engine = new NTLMEngineImpl();

    private String userName, password, domainName, ntlmMessage;

    public static NtlmAuthenticator create(String userName, String password, String domainName){
        return new NtlmAuthenticator(userName, password, domainName);
    }

    private NtlmAuthenticator(String userName, String password, String domainName) {
        this.userName = userName;
        this.password = password;
        this.domainName = domainName;

        String localNtlmMsg1 = null;
        try {
            localNtlmMsg1 = engine.generateType1Msg(domainName, "android-device");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ntlmMessage = localNtlmMsg1;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        final List<String> WWWAuthenticate = response.headers().values("WWW-Authenticate");

        if (WWWAuthenticate.contains("NTLM")) {
            return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMessage).build();
        }

        String ntlmMsg3 = null;
        try {
            ntlmMsg3 = engine.generateType3Msg(userName, password, domainName, "android-device", WWWAuthenticate.get(0).substring(5));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMsg3).build();
    }

}
