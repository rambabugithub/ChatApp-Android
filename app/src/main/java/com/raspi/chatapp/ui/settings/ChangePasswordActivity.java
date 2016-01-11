package com.raspi.chatapp.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.raspi.chatapp.R;
import com.raspi.chatapp.ui.password.PasswordActivity;

import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class ChangePasswordActivity extends AppCompatActivity{

  public static final String CHANGE_PWD = "com.raspi.chatapp.ui.settings" +
          ".ChangePasswordActivity.CHANGE_PWD";

  private boolean changed = false;
  private boolean active = false;


  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    Intent callingIntent = getIntent();
    if (callingIntent != null){
      if (CHANGE_PWD.equals(callingIntent.getAction())){
        //access was granted -> change pwd
        setContentView(R.layout.content_change_pwd_pin);
        getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        active = true;
      }else{
        Intent intent = new Intent(this, PasswordActivity.class);
        intent.setAction(CHANGE_PWD);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
      }
    }else{
      Intent intent = new Intent(this, PasswordActivity.class);
      intent.setAction(CHANGE_PWD);
      intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
      startActivity(intent);
    }
  }

  @Override
  protected void onResume(){
    super.onResume();
    ui();
  }

  private void ui(){
    final EditText np = (EditText) findViewById(R.id.new_pin);
    final EditText cp = (EditText) findViewById(R.id.confirm_pin);
    np.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after){
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count){
      }

      @Override
      public void afterTextChanged(Editable s){
        if (s.length() == 4){
          cp.requestFocus();
          s = null;
        }
      }
    });

    cp.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after){
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count){
      }

      @Override
      public void afterTextChanged(Editable s){
        s.length();
        if (s.length() == 4){
          if (checkEqual(s, np.getText())){
            changePwd(s.toString().toCharArray());
            s = null;
            changed = true;
            finish();
          }else{
            s.clear();
            np.getText().clear();
            np.requestFocus();
            findViewById(R.id.pwd_no_match).setVisibility(View.VISIBLE);
          }
        }
      }
    });
  }

  private boolean checkEqual(Editable c, Editable n){
    char[] newPwd = new char[n.length()];
    n.getChars(0, n.length(), newPwd, 0);

    char[] conPwd = new char[c.length()];
    c.getChars(0, c.length(), conPwd, 0);
    boolean ret = Arrays.equals(newPwd, conPwd);
    conPwd = null;
    newPwd = null;
    n = null;
    c = null;
    return ret;
  }

  private void changePwd(char[] pwd){
    try{
      byte[] salt = new byte[32];
      Random random = new Random();
      random.nextBytes(salt);
      KeySpec spec = new PBEKeySpec(pwd, salt, PasswordActivity.ITERATIONS, 32);
      pwd = null;
      SecretKeyFactory factory = PasswordActivity.getSecretKeyFactory();
      byte[] hash = factory.generateSecret(spec).getEncoded();

      getSharedPreferences(PasswordActivity.PREFERENCES, 0).edit()
              .putString(PasswordActivity.SALT, Base64.encodeToString(salt,
                      Base64.DEFAULT))
              .putString(PasswordActivity.HASH, Base64.encodeToString(hash,
                      Base64.DEFAULT))
              .commit();
    }catch (Exception e){
    }
  }

  @Override
  protected void onDestroy(){
    super.onDestroy();
    if (active){
      Toast toast = Toast.makeText(getApplicationContext(), changed ? R.string
              .pwd_changed : R.string.pwd_not_changed, Toast.LENGTH_LONG);
      toast.show();
    }
  }
}
