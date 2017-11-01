package testvolley.dn.java.testvolley;

import com.android.volley.Response;

/**
 * Created by dayanand on 01/11/17.
 */

public abstract class ApiListeners<T> implements Response.Listener<T>, Response.ErrorListener {

    public void onRequestStarted(ApiRequest<T> req) {

    }

    /**
     * Called on volley background thread
     * @param request
     * @param resString
     * @param response
     */
    public void onDataParsed(ApiRequest<T> request, String resString, T response) {

    }

    public void onResponse(ApiRequest<T> request,  T response) {
        onResponse(response);
    }
}
