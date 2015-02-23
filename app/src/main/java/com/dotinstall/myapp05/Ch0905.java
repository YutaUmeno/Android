package com.dotinstall.myapp05;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.dotinstall.myapp05.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class Ch0905 extends Activity implements OnClickListener {
    private static final String TAG = Ch0905.class.getSimpleName();
    private static final String PREF_TOKEN_SECRET = "";
    private static final String PREF_TOKEN = "";

    private RequestToken mRequestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.ch0905_main);

        Button btnRequest = (Button) findViewById(R.id.btnRequest);
        Button btnClearAuth = (Button) findViewById(R.id.btnClearAuth);
        btnRequest.setOnClickListener(this);
        btnClearAuth.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRequest) {
            startAuth();
        } else if (id == R.id.btnClearAuth) {
            // ?F???????
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(this);
            pref.edit().remove(PREF_TOKEN).remove(PREF_TOKEN_SECRET).commit();
        }
    }

    public static final Twitter getTwitter(Context context) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(context
                .getString(R.string.twitter_consumer_key));
        builder.setOAuthConsumerSecret(context
                .getString(R.string.twitter_consumer_secret));
        builder.setOAuthAccessToken(pref.getString(PREF_TOKEN, null));
        builder.setOAuthAccessTokenSecret(pref.getString(PREF_TOKEN_SECRET,
                null));

        final Twitter twitter = new TwitterFactory(builder.build())
                .getInstance();

        return twitter;
    }

    private void startAuth() {
        final Uri callbackUrl = getCallbackUrl();
        final Twitter twitter = getTwitter(this);
        Configuration conf = twitter.getConfiguration();
        if (conf.getOAuthAccessToken() != null
                && conf.getOAuthAccessTokenSecret() != null) {
            Toast.makeText(getApplicationContext(), "?F???????B",
                    Toast.LENGTH_LONG).show();
            return;
        }

        setProgressBarIndeterminateVisibility(true);

        // ??M?J?n
        new AsyncTask<Void, Void, RequestToken>() {
            @Override
            protected RequestToken doInBackground(Void... params) {
                RequestToken requestToken = null;
                try {
                    requestToken = twitter.getOAuthRequestToken(callbackUrl
                            .toString());
                } catch (TwitterException e) {
                    Log.e(TAG, "?F????s", e);
                }
                return requestToken;
            }

            @Override
            protected void onPostExecute(RequestToken requestToken) {
                super.onPostExecute(requestToken);

                setProgressBarIndeterminateVisibility(false);

                mRequestToken = requestToken;
                if (requestToken != null) {
                    String authorizationUrl = requestToken
                            .getAuthorizationURL();
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(authorizationUrl));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Request Token???擾?????????????B", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }.execute();
    }

    private Uri getCallbackUrl() {
        final Uri callbackUrl = new Uri.Builder()
                .scheme(getString(R.string.twitter_callback_url_scheme))
                .authority(getString(R.string.twitter_callback_url_host))
                .build();
        return callbackUrl;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null
                && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            Uri uri = intent.getData();
            if (uri != null
                    && uri.toString().startsWith(getCallbackUrl().toString())) {
                String verifier = uri.getQueryParameter("oauth_verifier");

                reqAccessToken(verifier);
            }
        }
    }

    private void reqAccessToken(final String oauthVerifier) {
        final Twitter twitter = getTwitter(this);

        new AsyncTask<Void, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(Void... params) {
                try {
                    return twitter.getOAuthAccessToken(mRequestToken,
                            oauthVerifier);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                super.onPostExecute(accessToken);

                if (accessToken != null) {
                    Toast.makeText(getApplicationContext(), "?F?????",
                            Toast.LENGTH_SHORT).show();
                    // ?g?[?N??????
                    SharedPreferences pref = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());
                    pref.edit()
                            .putString(PREF_TOKEN, accessToken.getToken())
                            .putString(PREF_TOKEN_SECRET,
                                    accessToken.getTokenSecret()).commit();
                }
            }
        }.execute();
    }

}
