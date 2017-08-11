package com.github.q115.goalie_android.https;

import android.graphics.Bitmap;
import android.util.ArrayMap;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.github.q115.goalie_android.utils.ImageHelper;
import com.github.q115.goalie_android.utils.UserHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.github.q115.goalie_android.Constants.ASYNC_CONNECTION_EXTENDED_TIMEOUT;
import static com.github.q115.goalie_android.Constants.FAILED;
import static com.github.q115.goalie_android.Constants.FAILED_TO_CONNECT;
import static com.github.q115.goalie_android.Constants.FAILED_TO_Send;
import static com.github.q115.goalie_android.Constants.URL;


/**
 * Created by Qi on 3/5/2017.
 */

public class RESTGetPhoto {
    private RESTGetPhoto.Listener mList;
    private String mUsername;

    public RESTGetPhoto(String username) {
        this.mUsername = username;
    }

    public interface Listener {
        void onSuccess(Bitmap photo);

        void onFailure(String errMsg);
    }

    public void setListener(RESTGetPhoto.Listener mList) {
        this.mList = mList;
    }

    public void execute() {
        String url;
        try {
            url = URL + "/photo/" + URLEncoder.encode(mUsername, "utf-8") + ".png";
        } catch (UnsupportedEncodingException e) {
            url = URL + "/photo/" + mUsername + ".png";
        }

        ImageRequest req = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap photo) {
                        if (photo != null) {
                            ImageHelper.getInstance().saveImageToPrivateSorageSync(mUsername, photo, ImageHelper.ImageType.PNG);
                            UserHelper.getInstance().getAllContacts().get(mUsername).profileBitmapImage = photo;
                        }

                        if (mList != null)
                            mList.onSuccess(photo);
                    }
                }, 0, 0, null, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mList == null)
                            return;
                        if (error == null || error.networkResponse == null) {
                            mList.onFailure(FAILED_TO_CONNECT);
                        } else if (error.networkResponse.headers != null && error.networkResponse.headers.containsKey("response")) {
                            String msgErr = error.networkResponse.headers.get("response") == null ? FAILED
                                    : error.networkResponse.headers.get("response");
                            mList.onFailure(msgErr);
                        } else {
                            mList.onFailure(FAILED_TO_Send);
                        }
                    }
                }) {
            @Override
            public ArrayMap<String, String> getHeaders() {
                ArrayMap<String, String> mHeaders = new ArrayMap<>();
                mHeaders.put("Content-Type", "application/json");
                mHeaders.put("Username", mUsername);
                return mHeaders;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(
                ASYNC_CONNECTION_EXTENDED_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                0));
        VolleyRequestQueue.getInstance().addToRequestQueue(req);
    }
}