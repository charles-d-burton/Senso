package burtondesign.com.senso.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import burtondesign.com.senso.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class UpdateSensoServer extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    private static final String TAG = "UpdateSensorService";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String UpdateOauth = "burtondesign.com.senso.services.action.send-oauth";
    private static final String UpdateFirebase = "burtondesign.com.senso.services.action.send-firebase";
    public static final String SERVICE_TYPE = "_tcp.";

    // TODO: Rename parameters
    private static final String KEY = "burtondesign.com.senso.services.extra.KEY";

    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager mNsdManager;

    public UpdateSensoServer() {
        super("UpdateSensoServer");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionOauth(Context context, String param1) {
        Intent intent = new Intent(context, UpdateSensoServer.class);
        intent.setAction(UpdateOauth);
        intent.putExtra(KEY, param1);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFirebase(Context context, String param1) {
        Intent intent = new Intent(context, UpdateSensoServer.class);
        intent.setAction(UpdateFirebase);
        intent.putExtra(KEY, param1);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (UpdateOauth.equals(action)) {
                final String param1 = intent.getStringExtra(KEY);
                handleActionOauth(param1);
            } else if (UpdateFirebase.equals(action)) {
                final String param1 = intent.getStringExtra(KEY);
                handleActionFirebase(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionOauth(String param1) {
        // TODO: Handle action Foo
        Log.d("OAUTH: ", param1);
        //String token = "idtoken=" + param1;
        String jsonString = updateBackend(param1);
        if (jsonString != null) {
            saveObject(getApplicationContext(), jsonString);
            Log.d(TAG, jsonString);
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFirebase(String param1) {
        // TODO: Handle action Baz
        Log.d("FIREBASE: ", param1);
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    private String updateBackend(String token) {
        Log.d(TAG, token);
        Log.d(TAG, "Starting send to backend auth");
        StringBuffer sb = new StringBuffer();
        String firebaseToken =  FirebaseInstanceId.getInstance().getToken();
        try {
            JSONObject data = new JSONObject();
            data.put("token", token);
            data.put("firebase", firebaseToken);
            URL url = new URL("https://smoker-relay.us/google-token");
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/json");
            conn.setUseCaches (false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(
                    conn.getOutputStream ());

            wr.writeBytes(data.toString());

            InputStream in = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            /*mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
            initializeDiscoveryListener();
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);*/
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void saveObject(Context context, String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);
            String token = object.getString("token");
            String email = object.getString("email");
            String uid = object.getString("user_id");
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("token", token);
            editor.putString("email", email);
            editor.putString("uid", uid);
            editor.commit();
            Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + token);

            //GcmPubSub pubSub = GcmPubSub.getInstance(context);
            //pubSub.subscribe(token, "/topics/" + token, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else {
                    Log.d(TAG, "Found Service Type: " + service.getServiceType());
                    /*else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("NsdChat")){
                    mNsdManager.resolveService(service, mResolveListener);
                }*/
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }
}
