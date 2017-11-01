package testvolley.dn.java.testvolley;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by dayanand on 01/11/17.
 */

public class RequestManager {

    private static RequestManager mInstance;
    private RequestQueue mRequestQueue;

    private RequestManager(Context context){
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }


    public static RequestManager getInstance(Context context) {
        if(mInstance == null){
            synchronized (RequestManager.class){
                if(mInstance == null){
                    mInstance = new RequestManager(context);
                }
            }
        }
        return mInstance;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }

    /** Always send screen name as tag */
    public void cancelRequests(String tag) {
        if(!TextUtils.isEmpty(tag)) {
            mRequestQueue.cancelAll(tag);
        }

    }
}
