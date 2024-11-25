package com.example.notebook11;

import static com.example.notebook11.MainActivity.String_TAGLIST;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EditActivity extends AppCompatActivity {

    EditText editText;
    EditText editTitle;
    private String content;
    private String time;
    private Toolbar toolbar;

    private Intent intent = new Intent();

    private String old_content = "";
    private String old_title;
    private String old_time = "";
    private int old_Tag = 1;
    private long id = 0;
    private  int openMode = 0;//0创建,1修改，2删除，3也是修改，4也是创建，-1什么都不做
    private int tag = 1;//??? 在后面等着呢



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
        editText = findViewById(R.id.edit1);
        editTitle = findViewById(R.id.title_Edit);


        //处理从主页点击的 edit念头
        Intent intent = getIntent();
        openMode = intent.getIntExtra("mode", 0);

        if (openMode == 3) {
            id = intent.getLongExtra("id", 0);
            old_content = intent.getStringExtra("content");
            old_time = intent.getStringExtra("time");
            old_title = intent.getStringExtra("title");
            old_Tag = intent.getIntExtra("tag", 1);
            editText.setText(old_content);
            editTitle.setText(old_title);
            editText.setSelection(old_content.length());//把光标位置移到末尾
        }


        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//?toolbar代替actionbar
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    autoSetMessage();
                    finish();
                }
            });
        }
        Spinner mspinner = findViewById(R.id.spinner);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        List<String> tagList = Arrays.asList(sharedPreferences.getString(String_TAGLIST, null).split(";")); //获取tags
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, tagList.subList(1, tagList.size())){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setGravity(Gravity.CENTER); // 设置文本靠右对齐
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setGravity(Gravity.END); // 设置文本靠右对齐
                return view;
            }

        };
        myAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mspinner.setAdapter(myAdapter);
        mspinner.setSelection(old_Tag-1);

        mspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tag = i + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


//好你个gpt写的什么东西
//        // 注册 OnBackPressedCallback
//        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                // 处理返回按钮点击事件
//                finish();
//            }
//        });
//
//    }
//    @Override
//    public boolean onOptionsItemSelected(android.view.MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            autoSetMessage();
//            // 处理 Up 按钮点击事件
//            finish(); // 结束当前 Activity
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }



        public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK ) {

            autoSetMessage();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void autoSetMessage(){//没想到你真有用。
        Log.d("set","mode:"+openMode+" "+editText.getText().toString()+editText.getText().toString().equals(old_content));
        if(openMode == 4){
            if(editText.getText().toString().isEmpty()&& editTitle.getText().toString().isEmpty()){
                intent.putExtra("mode", -1); //nothing new happens.
                //return super.onKeyDown(keyCode, event);
            }
            else{
                intent.putExtra("mode", 0); // new one note;
                intent.putExtra("title",editTitle.getText().toString());
                intent.putExtra("content", editText.getText().toString());
                intent.putExtra("time", dateToStr());
                intent.putExtra("tag", tag);
            }
        }
        else {
            if (editText.getText().toString().equals(old_content)&&editTitle.getText().toString().equals(old_title)&&!tagChange()) {
                intent.putExtra("mode", -1); // edit nothing

            }else {
                intent.putExtra("mode", 1); //edit the content
                intent.putExtra("title",editTitle.getText().toString());
                intent.putExtra("content", editText.getText().toString());
                intent.putExtra("time", dateToStr());
                intent.putExtra("id", id);
                intent.putExtra("tag", tag);
                if(editText.getText().toString().isEmpty()&& editTitle.getText().toString().isEmpty())//如果手动删空也删除~
                    intent.putExtra("mode",2);
            }
        }
        setResult(RESULT_OK, intent);
    }


    public boolean tagChange(){
        return tag != old_Tag;
    }

    public String dateToStr() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //simpleDateFormat.setTimeZone(TimeZone.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return simpleDateFormat.format(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_delete1){
            new AlertDialog.Builder(this).setMessage("要删除这条笔记吗？")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(openMode == 4){
                                intent.putExtra("mode",-1);
                                setResult(RESULT_OK,intent);
                            } else if (openMode == 3) {
                                intent.putExtra("mode",2);
                                intent.putExtra("id",id);
                                setResult(RESULT_OK,intent);
                            }
                            finish();
                        }//这个长串是谁想出来的真是牛人 留一条让你对比学习一下lambda语句 应该是拿来处理接口的
                    }).setNegativeButton(android.R.string.no, (dialogInterface, i) -> dialogInterface.dismiss()).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}