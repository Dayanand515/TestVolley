package testvolley.dn.java.testvolley;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;

/**
 * Created by dayanand on 01/11/17.
 */

public class ApiActivity extends Activity {
    private static String url = "https://wwww.abc.com/api/getData";
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);
        textView = findViewById(R.id.tv2);
        fetchData();

    }

    private ApiListeners<User> userApiListeners = new ApiListeners<User>() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (isFinishing()) return;
            textView.setText("Api error occurred");
        }

        @Override
        public void onResponse(User user) {
            if (isFinishing()) return;
            Log.d(Constants.LOG_TAG, "Response received");
            textView.setText("Response received");
        }
    };

    private void fetchData() {
        ApiRequest<User> request = new ApiRequest<>(User.class,
                Request.Method.GET,
                url,
                null,
                null,
                null,
                userApiListeners,
                userApiListeners);

        request.setRetryPolicy(
                new DefaultRetryPolicy(0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        request.setTag(getRequestTag());

        RequestManager.getInstance(this).addToRequestQueue(request);
    }

    private String getRequestTag() {
        return "ApiRequestTag";
    }

    @Override
    protected void onDestroy() {
        RequestManager.getInstance(this).cancelRequests(getRequestTag());
        super.onDestroy();
    }
}

