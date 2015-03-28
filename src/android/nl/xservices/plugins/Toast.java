package nl.xservices.plugins;

import android.content.Intent;
import android.view.Gravity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;

import android.text.TextUtils;
import android.util.Log;
import android.os.Bundle;

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
  private static final String ACTION_PASS_VEHICLE = "passVehicleDetails";
  private static final String ACTION_ADD_ACCOUNT = "addAccount";
  private static final String ACTION_GET_AUTHTOKEN = "getAuthToken";


  private android.widget.Toast mostRecentToast;



    private AccountManager mAccountManager;

  // note that webView.isPaused() is not Xwalk compatible, so tracking it poor-man style
  private boolean isPaused;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
            mAccountManager = AccountManager.get(cordova.getActivity());
    }


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

    }else if(ACTION_PASS_VEHICLE.equals(action)){

        Intent actionIntent = new Intent("au.gov.nswpf.PERSON_SERACH_INTENT");
        cordova.getActivity().startActivity(actionIntent);
        callbackContext.success();

        return true;

    }else if(ACTION_ADD_ACCOUNT.equals(action)){

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                android.widget.Toast toast = android.widget.Toast.makeText(webView.getContext(), "ADD ACCOUNT", android.widget.Toast.LENGTH_LONG);
                toast.show();
                mostRecentToast = toast;
                addNewAccount("au.gov.nswpf", "Full access");
                callbackContext.success();
            }
        });

        //callbackContext.success();
        return true;

    }else if(ACTION_GET_AUTHTOKEN.equals(action)){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {

                /*
                android.widget.Toast toast = android.widget.Toast.makeText(webView.getContext(), "GET AUTH TOKEN", android.widget.Toast.LENGTH_LONG);
                toast.show();
                mostRecentToast = toast;
                */
                //get auth token and pass back
                Account[] accounts = getAccountsByType("au.gov.nswpf");
                if (accounts.length>0){
                    //String token = getExsitingAccountAuthToken(accounts[0],"Full access");
                    final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(accounts[0],"Full access",null,cordova.getActivity(),new AccountManagerCallback<Bundle>(){
                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {
                            Bundle bdl;
                            try {
                                bdl = future.getResult();
                                String authToken = bdl.getString(AccountManager.KEY_AUTHTOKEN);
                                showMessage(authToken);
                                callbackContext.success(authToken);
                            } catch (Exception e) {
                                e.printStackTrace();
                                showMessage("Fetch token failed");
                                callbackContext.error("failed");
                            }
                        }
                    },null);

                }else{
                    showMessage("there's no existing account, please add a account first");
                    //addNewAccount("au.gov.nswpf", "Full access");
                    callbackContext.error("there's no account.");
                }
            }
        });
        return true;

    }else{
      callbackContext.error("toast." + action + " is not a supported function. Did you mean '" + ACTION_SHOW_EVENT + "'?");
      return false;
    }
  }


    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, cordova.getActivity(), new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    showMessage("Account was created");
                    Log.d("subzero", "AddNewAccount Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }, null);
    }


    //TODO:Clean this up
    private void getExsitingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, cordova.getActivity(), new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                Bundle bdl;
                try {
                    bdl = future.getResult();
                    String authToken = bdl.getString(AccountManager.KEY_AUTHTOKEN);
                    showMessage(authToken);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Fetch token failed");
                }
            }
        }, null);
    }

    private Account[] getAccountsByType(String accountType){
        Account allAcounts[] = mAccountManager.getAccountsByType(accountType);
        return allAcounts;
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.widget.Toast.makeText(webView.getContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

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