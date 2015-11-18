package com.njuguna.dailyselfie.api;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Retrofit interface to the image processing controller
 */
public interface ImageProcessingServiceApi {

    // The path where we expect the processing service to live
    public static final String PROCESSING_SVC_PATH = "/process";

    // search parameters
    public static final String TOKEN_PARAMETER = "token";
    public static final String TYPE_PARAMETER = "type";
    public static final String FILE_PARAMETER = "image";

    @GET(PROCESSING_SVC_PATH)
    public Response getProcess();

    @Multipart
    @POST(PROCESSING_SVC_PATH)
    public void processImage(@Part(TOKEN_PARAMETER) String token, @Part(TYPE_PARAMETER) Integer type, @Part(FILE_PARAMETER) TypedFile image, Callback<Response> responseCallback);

}