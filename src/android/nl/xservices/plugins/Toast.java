package nl.xservices.plugins;

import android.content.Intent;
import android.view.Gravity;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
    // TODO nice way for the Toast plugin to offer a longer delay than the default short and long options
    // TODO also look at https://github.com/JohnPersano/Supertoasts
    new CountDownTimer(6000, 1000) {
      public void onTick(long millisUntilFinished) {toast.show();}
      public void onFinish() {toast.show();}
    }.start();

    Also, check https://github.com/JohnPersano/SuperToasts
 */
public class Toast extends CordovaPlugin {

  private static final String ACTION_SHOW_EVENT = "show";
  private static final String ACTION_LAUNCH_SEARCH = "launchSearch";
  private static final String ACTION_GET_ROUTE = "getRoute";

  private android.widget.Toast mostRecentToast;

  // note that webView.isPaused() is not Xwalk compatible, so tracking it poor-man style
  private boolean isPaused;

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (ACTION_SHOW_EVENT.equals(action)) {

      if (this.isPaused) {
        return true;
      }

      final String message = args.getString(0);
      final String duration = args.getString(1);
      final String position = args.getString(2);

      cordova.getActivity().runOnUiThread(new Runnable() {

        public void run() {
          android.widget.Toast toast = android.widget.Toast.makeText(webView.getContext(), "not from param", android.widget.Toast.LENGTH_LONG);
          toast.show();
          mostRecentToast = toast;
          callbackContext.success();
        }

      });

      return true;
        //action on lauching the data
    } else if(ACTION_LAUNCH_SEARCH.equals(action)){
        //launch the target activity
        cordova.getActivity().runOnUiThread(new Runnable() {

            public void run() {
                android.widget.Toast toast = android.widget.Toast.makeText(webView.getContext(), "launch activity", android.widget.Toast.LENGTH_LONG);
                toast.show();
                mostRecentToast = toast;
                callbackContext.success();
            }

        });

        Intent actionIntent = new Intent("au.gov.nswpf.PERSON_SERACH_INTENT");
        JSONObject arg_object = args.getJSONObject(0);

        if(arg_object!=null){
            actionIntent.putExtra("firstName",arg_object.getString("firstName"));
            actionIntent.putExtra("lastName",arg_object.getString("lastName"));
        }

        cordova.getActivity().startActivity(actionIntent);
        callbackContext.success();

        return true;

    }else if(ACTION_GET_ROUTE.equals(action)){
        Intent intent = cordova.getActivity().getIntent();
        if(intent!=null){
            String route = intent.getStringExtra("redirRoute");
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, route));
            return true;
        }else{
            callbackContext.error("no routes found");
            return false;
        }
    }else{
      callbackContext.error("toast." + action + " is not a supported function. Did you mean '" + ACTION_SHOW_EVENT + "'?");
      return false;
    }
  }

  @Override
  public void onPause(boolean multitasking) {
    if (mostRecentToast != null) {
      mostRecentToast.cancel();
    }
    this.isPaused = true;
  }

  @Override
  public void onResume(boolean multitasking) {
    this.isPaused = false;
  }
}