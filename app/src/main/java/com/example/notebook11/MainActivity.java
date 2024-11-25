package com.example.notebook11;

import static android.text.TextUtils.split;
import static android.view.View.GONE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.animation.Animator;
import android.animation.AnimatorInflater;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private NoteDatabase dbHelper;//也不知道和dbHandler有什么区别
    NoteAdapter adapter,tmpAdapter;
    private  Context thisContext = this;

    final  String TAG = "tag";
    public static final String String_TAGLIST = "tagList";
    FloatingActionButton addBtn;
    private Toolbar toolbar,toolbarSET;

    //弹出菜单
    private PopupWindow popupWindow;
    private PopupWindow popupCover;
    private ViewGroup customView;
    private ViewGroup coverView;
    private LayoutInflater layoutInfater;
    private RelativeLayout main;
    private WindowManager wm;
    private DisplayMetrics metrics;


    private ListView lv;
    private List<Note> noteList = new ArrayList<>();
    List<Note> temp = new ArrayList<>();

    private boolean listOrderByLatest = false;
    private Button settingButton1,settingButton2;
    private ListView lv_tag;
    private TextView add_tag;
    List<String> tagList;
    private TagAdapter tagAdapter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private int tag = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //数据库和表是一起建的，已有数据库只建表得单独来 是测试时候建了之后就一直用着了 如果要改某个字段就只能重来咯

        // 获取 SharedPreferences 对象
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initPres();
        addBtn =  findViewById(R.id.AddfloatingActionButton);

        lv = findViewById(R.id.listView1);
        adapter = new NoteAdapter(thisContext, noteList);
        tmpAdapter = new NoteAdapter(thisContext, temp);
        lv.setAdapter(adapter);
        refreshListView();
        lv.setOnItemClickListener(this);//也可以断开
        lv.setOnItemLongClickListener(this);




        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//?toolbar代替actionbar
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);//其实是返回上一级的
        toolbar.setNavigationIcon(R.drawable.menu_icon);
        initPopUpView();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopUpView();
            }
        });

        addBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,EditActivity.class);
            intent.putExtra("mode",4);//这是创建笔记
            //startActivityForResult(intent,0);
            intentActivityResultLauncher.launch(intent);

        });


    }

    //Activity Result API refers to https://blog.csdn.net/weixin_51906150/article/details/126589820
    ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //此处是跳转的result回调方法
        //如果在SecondActivity中设置的result不为空并且resultCode为ok，则对数据进行处理
        Intent data = result.getData();
        if (data != null && result.getResultCode() == Activity.RESULT_OK) {

            SearchView searchView = findViewById(R.id.search);
            searchView.setQuery("",false);
            searchView.clearFocus();


            String content = data.getStringExtra("content");
            String title = data.getStringExtra("title");
            String time = data.getStringExtra("time");

            int returnMode = data.getIntExtra("mode",-1);
            int tag = data.getIntExtra("tag",1);
            long id = data.getLongExtra("id",0);//getLongExtra 要对应
            Note note = new Note(id,title,content,time,tag);
            Log.d("back","content:"+content+" mode:"+returnMode);
            CRUD op = new CRUD(thisContext);
            op.open();
            switch (returnMode) {
                case 0:
                    op.addNote(note);
                    break;
                case 1:
                    op.updateNode(note);
                    break;
                case 2:
                    op.deleteNote(note);
                    break;
                default:break;
            }
            op.close();

            refreshListView();
            refreshTagListView();

        } else {
            if(data == null) //其实是result不ok
                Toast.makeText(thisContext, "DATA IS NULL", Toast.LENGTH_LONG).show();
        }
    });

//discard deprecate
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        String edit = data.getStringExtra("return");
//        Log.d(TAG,edit);
//    }
    public void initPres(){
        if(!sharedPreferences.contains(String_TAGLIST))
            editor.putString(String_TAGLIST,"全部;无标签");
        editor.commit();
    }

    public void refreshListView(){//发生delete或update或修改排序后调用
        CRUD op = new CRUD(thisContext);
        op.open();

        if(!noteList.isEmpty()) noteList.clear();
        listOrderByLatest = sharedPreferences.getBoolean("listOrderByLatest",false);
        noteList.addAll(op.getAllNotes(listOrderByLatest));

        op.close();
        adapter.notifyDataSetChanged();
    }
    private void refreshTagList() {

        tagList = Arrays.asList(sharedPreferences.getString(String_TAGLIST, null).split(";")); //获取tags
        tagAdapter = new TagAdapter(thisContext, tagList, numOfTagNotes(tagList));
        lv_tag.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();
    }
    private void refreshTagListView(){
        listOrderByLatest = sharedPreferences.getBoolean("listOrderByLatest",false);
        CRUD op = new CRUD(thisContext);
        op.open();
        if(!temp.isEmpty()) temp.clear();
        temp.addAll(op.getTagNotes(tag,listOrderByLatest));
        Log.d("tag1",temp.toString());
        op.close();

        tmpAdapter.notifyDataSetChanged();
    }

    public List<Integer> numOfTagNotes(List<String> noteStringList){
        Integer[] numbers = new Integer[noteStringList.size()];
        Arrays.fill(numbers,0);
        numbers[0] = noteList.size();
        for(int i = 0; i < noteList.size(); i++){//wokao 遍历所有note来找吗？好像只能这样
            numbers[noteList.get(i).getTag() ] ++;//默认无标签是0
        }
        return Arrays.asList(numbers);
    }

    //menu
    public void initPopUpView(){

        layoutInfater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView =(ViewGroup) layoutInfater.inflate(R.layout.setting_layout,null);
        coverView = (ViewGroup) layoutInfater.inflate(R.layout.setting_cover,null);//一层灰的东西
        main = findViewById(R.id.main);
        wm = getWindowManager();
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        toolbarSET = customView.findViewById(R.id.toolbar_setting);
        toolbarSET.setNavigationIcon(R.drawable.baseline_settings_24);
    }

    public void showPopUpView(){
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;



        popupCover = new PopupWindow(coverView,width,height,false);
        popupWindow = new PopupWindow(customView,(int)(width * 0.7),height,true);//弹出窗口占三分之二
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.rgb(224,222,209)));//就是那个背景色
        popupWindow.setAnimationStyle(R.animator.slide_in);//no work

        settingButton1 = customView.findViewById(R.id.buttonOrder1);
        settingButton2 = customView.findViewById(R.id.buttonOrder2);

        lv_tag = customView.findViewById(R.id.lv_tag);//展示已有标签
        add_tag = customView.findViewById(R.id.add_tag);//添加标签

        refreshTagList();

        add_tag.setOnClickListener(view -> {
            if (sharedPreferences.getString(String_TAGLIST,"").split(";").length < 10) {
                final EditText et = new EditText(thisContext);
                new AlertDialog.Builder(this)
                        .setMessage("输入标签名称")
                        .setView(et)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                List<String> tagList = Arrays.asList(sharedPreferences.getString(String_TAGLIST, "null").split(";"));//get tags
                                String name = et.getText().toString();
                                if (!tagList.contains(name)) {
                                    String oldTagString = sharedPreferences.getString(String_TAGLIST, null);
                                    String newTagString = oldTagString + ";" + name;
                                    editor = sharedPreferences.edit();
                                    editor.putString(String_TAGLIST, newTagString);
                                    editor.apply();
                                    refreshTagList();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm != null && getCurrentFocus() != null) {
                                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                    }
                                } else
                                    Toast.makeText(thisContext, "标签已存在！", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();

            }else Toast.makeText(thisContext,"最多支持8个自定义标签~",Toast.LENGTH_SHORT).show();
        });

        lv_tag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tagList = Arrays.asList(sharedPreferences.getString(String_TAGLIST,null).split(";"));
                tag = i;
                if(tag>0) {

                    refreshTagListView();
                    lv.setAdapter(tmpAdapter);
                    tmpAdapter.notifyDataSetChanged();
                    toolbar.setTitle(tagList.get(i));
                    findViewById(R.id.main).post(new Runnable() {
                        @Override
                        public void run() {
                            popupWindow.dismiss();
                        }
                    });
                    Log.d(TAG, i + " tag click");
                }else{
                    lv.setAdapter(adapter);
                    refreshListView();
                    toolbar.setTitle("Notebook11");
                    findViewById(R.id.main).post(new Runnable() {
                        @Override
                        public void run() {
                            popupWindow.dismiss();
                        }
                    });
                }

            }
        });

        lv_tag.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(position>1) {
                    resetTagsX(adapterView);
                    float length = getResources().getDimensionPixelSize(R.dimen.distance);
                    TextView blank = view.findViewById(R.id.num_tag);
                    blank.animate().translationX(length).setDuration(300).start();
                    TextView text = view.findViewById(R.id.text_tag);
                    text.animate().translationX(length).setDuration(300).start();
                    ImageView del = view.findViewById(R.id.delete_tag);
                    del.setVisibility(View.VISIBLE);
                    del.animate().translationX(length).setDuration(300).start();

                    del.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("该标签下的所有笔记将会移至默认分组！")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            int tag = position ;
                                            for (Note tmp : noteList) {
                                                if (tmp.getTag() == tag) {
                                                    tmp.setTag(1);
                                                    CRUD op = new CRUD(thisContext);
                                                    op.open();
                                                    op.updateNode(tmp);
                                                    op.close();
                                                }
                                            }
                                            tagList = new ArrayList<>(Arrays.asList(sharedPreferences.getString(String_TAGLIST, null).split(";")));//asList不可修改
                                            for (int j = tag + 1; j < tagList.size(); j++) {
                                                //大于被删除tag的所有tag减一
                                                for (Note tmp : noteList) {
                                                    if (tmp.getTag() == j) {
                                                        tmp.setTag(j - 1);
                                                        CRUD op = new CRUD(thisContext);
                                                        op.open();
                                                        op.updateNode(tmp);
                                                        op.close();
                                                    }
                                                }
                                            }
                                            tagList.remove(position);
                                            String newTagListString = TextUtils.join(";", tagList);
                                            editor.putString(String_TAGLIST, newTagListString);
                                            editor.apply();

                                            refreshTagList();

                                            lv.setAdapter(adapter);
                                            refreshListView();

                                            toolbar.setTitle("NoteBook11");


                                        }
                                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).create().show();
                        }
                    });

                }return true;//?
            }

        });

        settingButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean("listOrderByLatest",true);
                editor.commit();

                refreshListView();
                refreshTagListView();
                Log.d(TAG,"oder by time !!!" +sharedPreferences.getBoolean("listOrderByLatest",false)+temp.toString());
                //lv.setAdapter(tmpAdapter);
                tmpAdapter.notifyDataSetChanged();
                findViewById(R.id.main).post(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                    }
                });
            }
        });
        settingButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean("listOrderByLatest",false);
                editor.commit();
                refreshListView();
                refreshTagListView();
                Log.d(TAG,"oder by time2 !!! "+ sharedPreferences.getBoolean("listOrderByLatest",false));
                findViewById(R.id.main).post(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                    }
                });
            }
        });

        //在主界面加载成功后显示弹出
        findViewById(R.id.main).post(new Runnable() {
            @Override
            public void run() {//我了个线程啊

                popupCover.showAtLocation(main, Gravity.NO_GRAVITY,0,0);//first
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY,0,0);//从左上角弹出

                coverView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });
            }
        });
    }


    //处理ListView中的点击事件,跳转到编辑界面
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //parent指lv本身，view是其中的view，position是位置索引从0开始，id默认和position一样

        Note curNote = (Note) parent.getItemAtPosition(position);
        if(curNote == null){
            Log.e(TAG, "Note object is null at position: " + position);
            return;
        }
        CRUD op = new CRUD(thisContext);
        op.open();
        long tmpId = op.getNoteId(curNote);
        op.close();
        Intent intent = new Intent(MainActivity.this,EditActivity.class);
        intent.putExtra("content",curNote.getContent());
        intent.putExtra("title",curNote.getTitle());
        intent.putExtra("id",tmpId);
        intent.putExtra("mode",3); //mode of click to edit
        intent.putExtra("tag",curNote.getTag());
        Log.d(TAG,"itemclick at"+position+" id:"+curNote.getId());
        intentActivityResultLauncher.launch(intent);
    }

    //longclick item in listView
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        NoteAdapter.ViewHolder vh = (NoteAdapter.ViewHolder)view.getTag();
        final Note note  = new Note(vh._id,vh.tv_title.getText().toString(),vh.tv_content.getText().toString(),vh.tv_time.getText().toString(),vh.tag);//其实也不重要
        Log.d(TAG,note.toString());
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Do you want to delete this note ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CRUD op = new CRUD(thisContext);
                        op.open();
                        op.deleteNote(note);
                        op.close();

                        refreshTagListView();
                        refreshListView();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //search setting
        MenuItem mSearch = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mSearch.getActionView();
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {//没有submit键忽略
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);//magic! 在myFilter中实现
                if(tmpAdapter!=null)
                    tmpAdapter.getFilter().filter(s);// 会bug吗?

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_delete_all){
            new AlertDialog.Builder(this).setMessage("要删除全部笔记吗？")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NoteAdapter ad = (NoteAdapter)lv.getAdapter();
                            Log.d(TAG,ad.toString());
                            if(ad == adapter) {
                                dbHelper = new NoteDatabase(thisContext);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();//== db.open 不用包装好的因为没有写这个删除全部 一个一个找也太慢了
                                db.delete("note_table", null, null);
                                db.execSQL("update sqlite_sequence set seq=0 where name='note_table'");
                                db.close();
                                refreshListView();
                            } else if (ad == tmpAdapter) {
                                dbHelper = new NoteDatabase(thisContext);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.execSQL("delete from note_table where TYPE = "+tag);
                                db.close();
                                refreshListView();
                                refreshTagListView();

                            }
                        }
                    }).setNegativeButton(android.R.string.no, (dialogInterface, i) -> dialogInterface.dismiss()).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetTagsX(AdapterView<?> parent) {
        for (int i = 2; i < parent.getCount(); i++) {
            View view = parent.getChildAt(i);
            if (view.findViewById(R.id.delete_tag).getVisibility() == View.VISIBLE) {
                float length = 0;
                TextView blank = view.findViewById(R.id.num_tag);
                blank.animate().translationX(length).setDuration(300).start();
                TextView text = view.findViewById(R.id.text_tag);
                text.animate().translationX(length).setDuration(300).start();
                ImageView del = view.findViewById(R.id.delete_tag);
                del.setVisibility(GONE);
                del.animate().translationX(length).setDuration(300).start();
            }
        }
    }

}


//有一些地方写context 一些用this或者Activity.class 搞不懂