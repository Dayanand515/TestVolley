package testvolley.dn.java.testvolley;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static testvolley.dn.java.testvolley.Constants.LOG_TAG;

/**
 * Created by dayanand on 01/11/17.
 */

public class ApiRequest<T> extends Request<T> {

    public static final String NETWORK_TIME_MILLIS = "network_time_millis";
    private static final Gson GSON = new Gson();

    /**
     * Default charset for JSON request.
     */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /**
     * Content type for request.
     */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private Response.Listener<T> mListener;
    private final String mRequestBody;

    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Bundle data = new Bundle();

    private Map<String, String> formData;

    private final Object mLock = new Object();

    public ApiRequest(Class<T> clazz, int method, String url, Map<String, String> headers, String requestBody,
                      Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(clazz, method, url, headers, requestBody, null, listener, errorListener);
    }

    public ApiRequest(Class<T> clazz, int method, String url, Map<String, String> headers, String requestBody,
                      Map<String, String> formData, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.mListener = listener;
        this.mRequestBody = requestBody;
        this.formData = formData;

        Log.d(LOG_TAG, "api url : "+ getUrl());

    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String json = null;
        try {
            String encoding = response.headers.get("Content-Encoding");
            if(encoding != null && encoding.equals("gzip")) {
                json = parseGzipData(response.data);
            }

            if (TextUtils.isEmpty(json)) {
                json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            }

            T object;

            if (clazz == String.class) {
                object = (T) json;
            } else {
                object = GSON.fromJson(json, clazz);
            }

            Response.Listener<T> listener;
            synchronized (mLock) {
                listener = mListener;
            }
            if (listener instanceof ApiListeners) {
                data.putLong(NETWORK_TIME_MILLIS, response.networkTimeMs);
                ((ApiListeners<T>) listener).onDataParsed(this, json, object);
            }

            return Response.success(object, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    public static String parseGzipData(byte[] data) {
        StringBuilder builder = new StringBuilder();
        try {
            final GZIPInputStream gStream = new GZIPInputStream(new ByteArrayInputStream(data));
            final InputStreamReader reader = new InputStreamReader(gStream);
            final BufferedReader in = new BufferedReader(reader);
            String read;
            while ((read = in.readLine()) != null) {
                builder.append(read);
            }
            reader.close();
            in.close();
            gStream.close();

        } catch (IOException e) {

        }
        return builder.toString();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        Log.d(LOG_TAG, "deliver response for :"+ getUrl());
        Response.Listener<T> listener;
        synchronized (mLock) {
            listener = mListener;
        }

        if (listener != null) {
            if (listener instanceof ApiListeners) {
                ((ApiListeners<T>) listener).onResponse(this, response);
            } else {
                listener.onResponse(response);
            }
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        Log.d(LOG_TAG, "deliver error for :"+ getUrl());
        super.deliverError(error);

        if (error != null){
            if(BuildConfig.DEBUG){
                String code = error.networkResponse != null ? String.valueOf(error.networkResponse.statusCode) :"";

                String message = "";
                try {
                    if (error.networkResponse != null && error.networkResponse.data != null) {

                        String encoding = error.networkResponse.headers.get("Content-Encoding");
                        if (encoding != null && encoding.equals("gzip")) {
                            message = parseGzipData(error.networkResponse.data);
                        }
                        if (TextUtils.isEmpty(message)) {
                            message = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                        }
                    }

                    if(TextUtils.isEmpty(message)){
                        message = error.getLocalizedMessage();
                    }

                    if(TextUtils.isEmpty(message)){
                        message = error.getClass().getSimpleName();
                    }

                    Log.d(Constants.LOG_TAG, "message : "+ message);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        try {
            if (formData != null) {
                return super.getBody();
            }
            return TextUtils.isEmpty(mRequestBody) ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
        } catch (AuthFailureError e) {

        }
        return null;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return formData;
    }

    public Bundle getData() {
        return data;
    }

    @Override
    public void cancel() {
        super.cancel();
        synchronized (mLock) {
            mListener = null;
        }
    }

}
