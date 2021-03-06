package org.androidtown.helpmom;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    EditText name;
    EditText id;
    EditText pw;
    EditText pwConfirm;
    Button registerBtn;
    TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        name=findViewById(R.id.input_name);
        id=findViewById(R.id.input_id);
        pw=findViewById(R.id.input_pw);
        registerBtn=findViewById(R.id.registerBtn);
        loginLink=findViewById(R.id.link_login);
        pwConfirm=findViewById(R.id.input_pwConfirm);

        registerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                register();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }

    public void register(){
        Log.d(TAG,"Register");

        if (!validate()) {
            onRegisterFailed();
            return;
        }

        registerBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("회원가입 중...");
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onRegisterSuccess or onRegisterFailed
                        // depending on success
                        onRegister();
                        // onRegisterFailed();
                        progressDialog.dismiss();
                    }
                }, 2000);
    }

    public void onRegister(){

        String NAME = name.getText().toString();
        String ID = id.getText().toString();
        String PW = pw.getText().toString();

        // TODO: Implement your own register logic here.

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-54-180-79-126.ap-northeast-2.compute.amazonaws.com:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<RegisterResult> call = service.getRegister(ID,PW,NAME);

        call.enqueue(new Callback<RegisterResult>() {
            @Override
            public void onResponse(Call<RegisterResult> call, Response<RegisterResult> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "code " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                RegisterResult r = response.body();

                if(r.getRes().equals("already")) { //아이디가 이미 있음
                    Toast.makeText(getApplicationContext(), "아이디가 이미 있음", Toast.LENGTH_SHORT).show();
                    onRegisterFailed();

                }else if(r.getRes().equals("fail")){ //등록 실패

                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                    onRegisterFailed();
                }else{ //등록 성공

                    Toast.makeText(getApplicationContext(), "등록 성공" , Toast.LENGTH_SHORT).show();
                    onRegisterSuccess();
                }
            }
            @Override
            public void onFailure(Call<RegisterResult> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                onRegisterFailed();
            }
        });
    }


    public void onRegisterSuccess() {
        registerBtn.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        //Toast.makeText(getBaseContext(), "가입 성공", Toast.LENGTH_LONG).show();
        startActivity(registerIntent);
        finish();
    }

    public void onRegisterFailed() {
       // Toast.makeText(getBaseContext(), "가입 실패", Toast.LENGTH_LONG).show();
        registerBtn.setEnabled(true);
    }
//

    public boolean validate() {
        boolean valid = true;

        String NAME = name.getText().toString();
        String ID = id.getText().toString();
        String PW = pw.getText().toString();
        String PW_CONFIRM = pwConfirm.getText().toString();

        if (NAME.isEmpty() || name.length() < 3) {
            name.setError("3자 이상 입력하세요");
            valid = false;
        } else {
            name.setError(null);
        }

        if (ID.isEmpty()) {
            id.setError("아이디를 입력하세요");
            valid = false;
        } else {
            id.setError(null);
        }

        if (PW.isEmpty() || PW.length() < 4 || PW.length() > 10) {
            pw.setError("4자리 이상 입력하세요");
            valid = false;
        } else {
            pw.setError(null);
        }
        if(!PW.equals(PW_CONFIRM))
        {
            pw.setError("비밀번호가 일치하지 않습니다");
            pwConfirm.setError("비밀번호가 일치하지 않습니다");
            valid=false;
        }

        return valid;
    }//DB check required
}
