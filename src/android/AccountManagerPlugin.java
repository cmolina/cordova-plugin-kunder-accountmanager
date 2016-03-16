package cl.kunder.accountmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Build;
import android.os.Bundle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class AccountManagerPlugin extends CordovaPlugin {

    private static final String TAG = "AccountManagerPlugin";

    public AccountManagerPlugin(){

    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        AccountManager accountManager = AccountManager.get(cordova.getActivity().getApplicationContext());

        if (action.equals("addAccount")) {
            /*Esta acción necesita los siguientes parámetros: userAccount, password, authToken, accountType. Estos son Strings*/

            String userAccount = args.getString(0);
            String password = args.getString(1);
            // String authToken = args.getString(2);
            //Account type debe ser único e idéntico al valor definido en el string authLabel
            String accountType = args.getString(2);
            Bundle userData = new Bundle();

            
            try {
                JSONObject jsonObject = args.getJSONObject(4);
                Iterator<?> iterator = jsonObject.keys();
                while (iterator.hasNext()){
                    String key = (String)iterator.next();
                    String value = jsonObject.get(key).toString();
                    userData.putString(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            

            Account [] accounts = accountManager.getAccountsByType(accountType);

            if(accounts.length == 0){
                //No hay cuentas, entonces es posible añadir una
                
                Account account = new Account(userAccount, accountType);
                if(accountManager.addAccountExplicitly(account, password, userData)){
                    // Toast.makeText(getApplicationContext(), "Registro de cuenta exitoso", Toast.LENGTH_LONG).show();
                    JSONObject r = new JSONObject();
                    r.put("responseCode", "ok");
                    callbackContext.success(r);
                }
                else{
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                    // Toast.makeText(getApplicationContext(), "Error al registrar una cuenta!!!", Toast.LENGTH_LONG).show();
                }

            }
            else {
                //Caso contrario, no se debe realizar el proceso
                // Toast.makeText(getApplicationContext(), "Ya existe una cuenta!!!", Toast.LENGTH_LONG).show();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            }

            
        }
        else if(action.equals("removeAccount")){
            
            String accountType = args.getString(0);
            Account [] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length == 0){
                //No hay cuentas, retorna success
                JSONObject r = new JSONObject();
                r.put("responseCode", "ok");
                callbackContext.success(r);

            }
            else {    
                if(Build.VERSION.SDK_INT >= 22){
                    if(accountManager.removeAccountExplicitly(accounts[0])){
                        JSONObject r = new JSONObject();
                        r.put("responseCode", "ok");
                        callbackContext.success(r);
                    }
                    else {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                    }
                }
                else{
                    //Deprecated on API 22
                    accountManager.removeAccount(accounts[0], new AccountManagerCallback<Boolean>() {
                        @Override
                        public void run(AccountManagerFuture<Boolean> future) {
                            try {
                                if (future.getResult()) {
                                    JSONObject r = new JSONObject();
                                    r.put("responseCode", "ok");
                                    callbackContext.success(r);
                                }
                            } catch (AuthenticatorException e) {
                                e.printStackTrace();
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                            } catch (OperationCanceledException e) {
                                e.printStackTrace();
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                            } catch (IOException e) {
                                e.printStackTrace();
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                            }
                        }
                    }, null);

                }
            }
            
                
        }

        else if(action.equals("getUserAccount")){
            String accountType = args.getString(0);
            String responseKey = args.getString(2);

            Account [] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length == 0){
                //No hay cuentas, retorna error
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            }
            else{
                String userAccount = accounts[0].name;
                JSONObject r = new JSONObject();
                r.put(responseKey, userAccount);
                callbackContext.success(r);
            }

        }

        else if(action.equals("getPassword")){
            
            String accountType = args.getString(0);
            String key = args.getString(2);
            
            Account [] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length == 0){
                //No hay cuentas, retorna error
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            }
            else { 
                String password = accountManager.getPassword(accounts[0]);
                JSONObject r = new JSONObject();
                r.put("password", password);
                callbackContext.success(r);
            }
               
        }

        else if(action.equals("getDataFromKey")){

            String accountType = args.getString(0);
            String keyData = args.getString(2);
            Account [] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length == 0){
                //No se pueden obtener los datos de la cuenta
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            }
            else{
                String data = accountManager.getUserData(accounts[0], keyData);
                if(data != null){
                    JSONObject r = new JSONObject();
                    r.put(keyData, data);
                    callbackContext.success(r);
                }
                else{
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                }
            }
        }

        else if(action.equals("setUserData")){

            String accountType = args.getString(0);
            JSONObject userData = args.getJSONObject(2);
            Account [] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length == 0){
                //No se pueden obtener los datos de la cuenta
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            }
            else{
                try {
                    Iterator<?> iterator = userData.keys();
                    while (iterator.hasNext()){
                        String key = (String)iterator.next();
                        String value = userData.get(key).toString();
                        accountManager.setUserData(accounts[0],key,value);
                    }
                    JSONObject r = new JSONObject();
                    r.put("responseCode", "ok");
                    callbackContext.success(r);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                }
            }   
        }

        else if(action.equals("changePassword")){

            String accountType = args.getString(0);
            String newPassword = args.getString(2);
            Account [] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length == 0){
                //No se pueden obtener los datos de la cuenta
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            }
            else{
                accountManager.setPassword(accounts[0], newPassword);  
                JSONObject r = new JSONObject();
                r.put("responseCode", "ok");
                callbackContext.success(r); 
            }
        }
        return true;
    }

}