package fun.lula.flomo.util;

import okhttp3.*;

import java.io.IOException;

public class HttpUtil {
    public static String sendGetRequest(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        Response execute = null;
        try {
            execute = okHttpClient.newCall(request).execute();
            return execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String sendPostRequestWithJson(String url, String json) {
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application" +
                "/json;charset=UTF-8"), json);

        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response execute = null;
        try {
            execute = okHttpClient.newCall(request).execute();
            return execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
