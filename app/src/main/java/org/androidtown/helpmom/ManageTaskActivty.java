package org.androidtown.helpmom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageTaskActivty extends AppCompatActivity {

    ListView listView_addedTask;
    EditText editText_addTask;

    Button btn_addTask;
    Button btn_delete_tasks;
    Button btn_confirm_tasks;

    // EditText의 String값 받을 용도.
    String editTextValue;

    // ListView에 전시할 데이터.
    ArrayList<Data_Format> datas;
    private static MyAdapter adapter;

    // 체크된 Task들은 이 arraylist에 저장될것임.
    ArrayList<String> list_decided_tasks;

    // ListView의 onItemClickListener 에 쓰임.
    int default_backgroundColor = Color.parseColor("#fcfdff"); //기존 배경색
    int checkedColor = Color.parseColor("#f9b37a"); //체크됬을때 배경색
    int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_task);
//        final TextView addTask = findViewById(R.id.editText_addTask);
//        final float density = getResources().getDisplayMetrics().density;
//        final Drawable drawable_addTask = getResources().getDrawable(R.drawable.input_icon);
//        final int width = Math.round(24*density);
//        final int height = Math.round(24*density);
//        drawable_addTask.setBounds(0, 0, width, height);
//        addTask.setCompoundDrawables(drawable_addTask, null, null, null);

        setTitle("TASK 관리창");
        listView_addedTask = (ListView) findViewById(R.id.listView_taskStored);
        editText_addTask = (EditText) findViewById(R.id.editText_addTask);
        btn_addTask = (Button) findViewById(R.id.btn_addTask);
        btn_delete_tasks = (Button) findViewById(R.id.btn_delete_tasks);
        listView_addedTask =  findViewById(R.id.listView_taskStored);
        editText_addTask =  findViewById(R.id.editText_addTask);

        btn_delete_tasks =  findViewById(R.id.btn_delete_tasks);
        btn_confirm_tasks=findViewById(R.id.confirm_tasks);
        // Data 리스트
        datas = new ArrayList<>();

        // 선택된 task들을 담을 arrayList. 넘겨줄 것임.
        list_decided_tasks = new ArrayList<>();

        // Adapter 정의
        adapter = new MyAdapter(datas, getApplicationContext());
        adapter.notifyDataSetChanged();

        // 리스트뷰에 데이터 연결 완료.
        listView_addedTask.setAdapter(adapter);

        btn_confirm_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //리스트가 없으면 막아야함
                if(count==0) {
                    Toast.makeText(getApplicationContext(),"TASK를 추가하세요",Toast.LENGTH_LONG).show();
                    return;
                }else if(checkCount()==0){
                    Toast.makeText(getApplicationContext(), "추가 할 TASK를 선택하세요", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    Confirm();
                }


            }
        });
        listView_addedTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // user selects item, get that item.
                Object itemObject = adapterView.getAdapter().getItem(position);

                // cast it to Data_Format format.
                Data_Format data_format_object = (Data_Format) itemObject;

                // get the checkBox and others of a row.
                CheckBox itemCheckBox =  view.findViewById(R.id.checkBox_listView_row_item);
                TextView txt_view =  view.findViewById(R.id.textView_rowItem_detail);
                ImageView imgView =  view.findViewById(R.id.imgView_rowItem_icon);

                // Reverse the state of the checkBox, change backgroundColor.
                if (data_format_object.isChecked() == true) {
                    Log.d("itemClicked:", "cur state:" + data_format_object.toString());
                    itemCheckBox.setChecked(false);
                    data_format_object.setCheckedState(false);


                } else if (data_format_object.isChecked() == false) {
                    Log.d("itemClicked:", "cur state:" + data_format_object.toString());
                    itemCheckBox.setChecked(true);
                    data_format_object.setCheckedState(true);
                }
            }
        });

        // AddTask 버튼 이벤트리스너.
        // 할일추가버튼(에딧텍스트옆에있는) 을 누르면, editText에서 입력한 값을 Data_Format에 넣고 그 Data_Format 오브젝트를
        // 위에있는 할일 list에 추가.
        btn_addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editTextValue = editText_addTask.getText().toString();

                if (editTextValue.isEmpty()) {
                    Toast.makeText(ManageTaskActivty.this, "할 일을 입력하세요", Toast.LENGTH_SHORT).show();
                } else {

                    Data_Format data_format1 = new Data_Format(editTextValue);

                    // 유저가 입력한 할일을 data_list (어레이 리스트)에 추가
                    datas.add(data_format1);

                    Log.d("taskAdd", data_format1.toString());

                    // 리스트뷰 갱신 (이래야 화면에 나타남)
                    adapter.notifyDataSetChanged();
                    // 가장 밑 row로 자동 스크롤
                    scrollMyListViewToBottom();
                    // 초기화
                    editText_addTask.setText("");
                    // ListView 높이 동적 변화 : 350dp -> 150dp

                    count++;
                }
            }
        });

        // delete 버튼누르면:
        btn_delete_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (count == 0 ) {//리스트에 올라온개수..체크한 개수 별도로생각
                    Toast.makeText(getApplicationContext(), "삭제 할 TASK가 없습니다", Toast.LENGTH_LONG).show();
                    return;
                }else if(checkCount()==0){
                    Toast.makeText(getApplicationContext(), "삭제 할 TASK를 선택하세요", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ManageTaskActivty.this).create();
                    alertDialog.setMessage("선택한 일을 삭제 하시겠습니까?");
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {

                            int size = datas.size();
                            for (int i = 0; i < size; i++) {
                                // unselect the checkedboxes.
                                Data_Format data_format = datas.get(i);

                                if (data_format.isChecked()) {
                                    datas.remove(i);
                                    i--;
                                    size = datas.size();
                                    count--;
                                }
                            }
                            // update
                            adapter.notifyDataSetChanged();
                        }
                    });

                    alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                }
            }
        });
    }


    private int checkCount(){
        int size = datas.size();
        int c = 0;
        for (int i = 0; i < size; i++) {
            // unselect the checkedboxes.
            if (datas.get(i).isChecked()) {
               c++;
            }
        }
        return c;
    }


    // 각 listView의 row에 display할 내용의 포맷.
    public class Data_Format {

        // memebers
        String task_detail;
        boolean isChecked = false; //체크박스 상태 반영

        // 생성자
        public Data_Format(String task_detail) {
            this.task_detail = task_detail;
        }

        public String toString() {
            if (isChecked() == true) {
                return "isChecked";
            } else {
                return "isNotChecked";
            }
        }

        // 메소드
        public String getTask_detail() {
            return task_detail;
        }

        public boolean isChecked() {
            return isChecked == true;
        }

        public void setCheckedState(boolean bool) {
            isChecked = bool;
        }
    }

    public class MyAdapter extends BaseAdapter {

        private ArrayList<Data_Format> dataSet = null;
        private Context mContext = null;

        public MyAdapter(ArrayList<Data_Format> data, Context context) {
            this.dataSet = data;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            int ret = 0;
            if (dataSet != null) {
                ret = dataSet.size();
            }
            return ret;
        }

        @Override
        public Object getItem(int itemIndex) {
            Object ret = null;
            if (dataSet != null) {
                ret = dataSet.get(itemIndex);
            }
            return ret;
        }

        @Override
        public long getItemId(int itemIndex) {
            return itemIndex;
        }

        private int lastPosition = -1;

        @NonNull
        @Override
        public View getView(int itemPosition, @Nullable View convertView, @NonNull ViewGroup parent) {

            mViewHolder viewHolder = null;

            if (convertView != null) {
                viewHolder = (mViewHolder) convertView.getTag();
            } else {
//                // 수정됨(from 안에)
//                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//                // row_item.xml 팽창.
//                convertView = inflater.inflate(R.layout.row_item, parent, false);
                convertView = View.inflate(mContext, R.layout.row_item, null);

                TextView item_task_detail = (TextView) convertView.findViewById(R.id.textView_rowItem_detail);
                CheckBox itemCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox_listView_row_item);

                viewHolder = new mViewHolder(convertView);
                viewHolder.setItemCheckBox(itemCheckBox);
                viewHolder.setItemTextView(item_task_detail);

                convertView.setTag(viewHolder);
            }

            Data_Format data_format = dataSet.get(itemPosition);
            viewHolder.getItemCheckBox().setChecked(data_format.isChecked());
            viewHolder.getItemTextView().setText(data_format.getTask_detail());

            int lastPosition = -1;

            // 수정: result->convertView.
            Animation animation = AnimationUtils.loadAnimation(mContext, (itemPosition > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            convertView.startAnimation(animation);
            lastPosition = itemPosition;

            // return the completed view to render on screen
            return convertView;
        }
    }

    public class mViewHolder extends RecyclerView.ViewHolder {
        private TextView itemTextView;
        private CheckBox itemCheckBox;

        public mViewHolder(View itemView) {
            super(itemView);
        }

        public CheckBox getItemCheckBox() {
            return itemCheckBox;
        }

        public void setItemCheckBox(CheckBox itemCheckBox) {
            this.itemCheckBox = itemCheckBox;
        }

        public TextView getItemTextView() {
            return itemTextView;
        }

        public void setItemTextView(TextView itemTextView) {
            this.itemTextView = itemTextView;
        }


    }



        @Override
        public void onBackPressed() {
            super.onBackPressed();
        }

        // Task가 추가될 때마다 마지막 row로 자동스크롤.
        public void scrollMyListViewToBottom() {
            listView_addedTask.post(new Runnable() {
                @Override
                public void run() {
                    // select the last row, so it will scroll into view..
                    listView_addedTask.setSelection(adapter.getCount() - 1);
                }
            });
    }
    public void Confirm(){
        Intent intent=getIntent();
        final String roomNumber=intent.getStringExtra("roomNumber");
        Log.d("Confirm", roomNumber + " "+"Confirm동작");

//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(20, TimeUnit.SECONDS)
//                .readTimeout(20, TimeUnit.SECONDS)
//                .writeTimeout(20, TimeUnit.SECONDS)
//                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-54-180-79-126.ap-northeast-2.compute.amazonaws.com:3000/")
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);

        int size = datas.size();
        for (int i = 0; i < size; i++) {
            // unselect the checkedboxes.
            if (datas.get(i).isChecked()) {
                list_decided_tasks.add(datas.get(i).getTask_detail());
            }
        }
        Log.d("list_decided_tasks", "number of added list_decided_tasks: "+list_decided_tasks.size()+"");


            Call<RegisterResult> call = service.createTask(roomNumber,list_decided_tasks);
            call.enqueue(new Callback<RegisterResult>() {
                @Override
                public void onResponse(Call<RegisterResult> call, Response<RegisterResult> response) {
                    if(!response.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "code " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.d("response_failed", "code:"+response.code());
                        return;
                    }
                    RegisterResult r = response.body();

                    ArrayList<Task> tasks = new ArrayList<Task>();

                    if(r.getRes().equals("one")){
                        Log.d("one","one");

                    }else {
                        int sz = r.getTask().length;

                        for (int i = 0; i < sz; i++) {
                            Task t = new Task(r.getTask()[i], r.getProgress()[i], r.getComment()[i], r.getPoint()[i], r.getChangedName()[i], r.getCreated()[i]);
                            tasks.add(t);
                        }
                    }

                    Log.d("taskCheck", tasks.size()+"");
                    displayTask(roomNumber, tasks);

                }
                @Override
                public void onFailure(Call<RegisterResult> call, Throwable t) {
                    Log.d("onFailure", "실패"+t.toString());
                    Toast.makeText(getApplicationContext(), "실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void displayTask(String roomNumber, ArrayList<Task> tasks){
        Intent intent=new Intent();
        intent.putExtra("roomNumber", roomNumber);
        // 6-02: 추가된코드 (Serilizable 캐스팅)
        intent.putExtra("task", (Serializable)tasks);
        setResult(1,intent);
        finish();
    }
    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.logout:
                startActivity(new Intent(ManageTaskActivty.this,LoginActivity.class));
                Toast.makeText(getApplicationContext(), "로그아웃 되었습니다", Toast.LENGTH_LONG).show();
                finish();
        }
        return true;
    }
}