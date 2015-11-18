package com.njuguna.dailyselfie.util;

import com.njuguna.dailyselfie.common.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;

public class TimedOutUrlConnectionClient extends UrlConnectionClient {
    @Override
    protected HttpURLConnection openConnection(Request request) throws IOException {
        HttpURLConnection connection = super.openConnection(request);
        connection.setConnectTimeout(Constants.HTTP_CONNECTION_TIMEOUT_MILLIS);
        connection.setReadTimeout(Constants.HTTP_CONNECTION_TIMEOUT_MILLIS);
        return connection;
    }
}
