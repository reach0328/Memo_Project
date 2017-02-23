package com.android.jh.memo_project;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.jh.memo_project.data.DBHelper;
import com.android.jh.memo_project.domain.Memo;
import com.android.jh.memo_project.interfaces.AdapterInterface;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterInterface, View.OnClickListener{

    // 카메라 요청 코드
    private final int REQ_CAMERA = 101;
    // 겔러리 요청 코드
    private final int REQ_GALLERY = 102;
    // 권한 요청 코드
    private final int REQ_PERMISSION = 100;

    private List<Memo> datas = new ArrayList<>();
    private List<Memo> search = new ArrayList<>();
    RecyclerView recyclerView;
    MemoAdapter adapter;
    AlertDialog dialog;
    Uri fileUri = null;
    FloatingActionMenu quickFloatingMenu;
    FloatingActionButton FloatBtnQuick_memo,FloatBtnQuick_photo,FloatBtnQuick_voice,FloatBtnQuick_camera;
    Toolbar toolbar;
    EditText et_Search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 권한 처리
        checkPermission();
    }

    public void init() {
        quickFloatingMenu = (FloatingActionMenu) findViewById(R.id.quick_floating_action_menu);
        FloatBtnQuick_memo = (FloatingActionButton) findViewById(R.id.quick_floating_action_menu_add);
        FloatBtnQuick_photo = (FloatingActionButton) findViewById(R.id.quick_floating_action_menu_photo);
        FloatBtnQuick_voice = (FloatingActionButton) findViewById(R.id.quick_floating_action_menu_voice);
        FloatBtnQuick_camera = (FloatingActionButton) findViewById(R.id.quick_floating_action_menu_camera);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        et_Search = (EditText) findViewById(R.id.et_search);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        try {
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        adapter = new MemoAdapter(this,datas);
        recyclerView.setAdapter(adapter);
        listener();
    }

    public void listener() {
        quickFloatingMenu.setOnClickListener(this);
        FloatBtnQuick_memo.setOnClickListener(this);
        FloatBtnQuick_photo.setOnClickListener(this);
        FloatBtnQuick_voice.setOnClickListener(this);
        FloatBtnQuick_camera.setOnClickListener(this);
        et_Search.addTextChangedListener(Search_textWatcher);
    }

    TextWatcher Search_textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.toString().equals("")) {
                try {
                    loadData();
                    refreshList();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                searchDate(editable.toString());
            }
        }
    };

    public void searchDate(String searchText) {
        search.clear();
        for(int i = 0; i<datas.size();i++) {
            if(datas.get(i).getMemo().matches(".*"+searchText+".*")) {
                search.add(datas.get(i));
            }
        }
        adapter = new MemoAdapter(this,search);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void delete(int position) throws SQLException {
        DBHelper dbHelper = OpenHelperManager.getHelper(this,DBHelper.class);
        Dao<Memo,Integer> memoDao = dbHelper.getMemoDao();
        Memo memo = datas.get(position);
        memoDao.delete(memo);
        loadData();
        refreshList();
    }

    private void checkPermission() {
        //버전 체크해서 마시멜로우(6.0)보다 낮으면 런타임 권한 체크를 하지않는다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionControl.checkPermssion(this, REQ_PERMISSION)) {
                init();
            }
        } else {
            init();
        }
    }

    //권한체크 후 콜백< 사용자가 확인후 시스템이 호출하는 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMISSION) {
            //배열에 넘긴 런타임 권한을 체크해서 승인이 됐으면
            if (PermissionControl.onCheckedResult(grantResults)) {
                init();
            } else {
                Toast.makeText(this, "권한을 사용하지 않으시면 프로그램을 실행시킬수 없습니다", Toast.LENGTH_SHORT).show();
                finish();
                // 선택 1.종료, 2. 권한체크 다시물어보기
                //PermissionControl.checkPermssion(this,REQ_PERMISSION);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.quick_floating_action_menu_add :
                quickFloatingMenu.close(true);
                quickAddAlert();
                break;
            case R.id.quick_floating_action_menu_photo:
                quickFloatingMenu.close(true);
                getPicture();
                break;
            case R.id.quick_floating_action_menu_voice :
                quickFloatingMenu.close(true);
                break;
            case R.id.quick_floating_action_menu_camera:
                quickFloatingMenu.close(true);
                getCamera();
                break;
        }
    }

    public void saveToList(Memo memo) throws SQLException {
        DBHelper dbHelper = OpenHelperManager.getHelper(this,DBHelper.class);
        Dao<Memo,Integer> memoDao = dbHelper.getMemoDao();
        memoDao.create(memo);
        loadData();
        refreshList();
    }

    public void loadData() throws SQLException{
        DBHelper dbHelper = OpenHelperManager.getHelper(this,DBHelper.class);
        Dao<Memo,Integer> memoDao = dbHelper.getMemoDao();
        datas = memoDao.queryForAll();
    }

    public void updateToLIst(Memo memo) throws SQLException {
        DBHelper dbHelper = OpenHelperManager.getHelper(this,DBHelper.class);
        Dao<Memo,Integer> memoDao = dbHelper.getMemoDao();
        memoDao.update(memo);
        loadData();
        refreshList();
        super.onBackPressed();
    }

    private void refreshList() {
        adapter = new MemoAdapter(this,datas);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void quickAddAlert() {
        // LayoutInflater를 통해 위의 custom layout을 AlertDialog에 반영. 이 외에는 거의 동일하다.
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view4 = inflater.inflate(R.layout.quick_add_alert, null);
        //멤버의 세부내역 입력 Dialog 생성 및 보이기
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
        final EditText et_quick_content = (EditText) view4.findViewById(R.id.et_quick_content);
        et_quick_content.setText("");
        Button btn_ok = (Button) view4.findViewById(R.id.btn_add_ok);
        Button btn_cancle = (Button) view4.findViewById(R.id.btn_quick_cancel);
        buider.setView(view4);
        dialog = buider.create();
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveToList(new Memo(et_quick_content.getText().toString()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();

            }
        });
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void getCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 롤리팝 이상 버전에서는 코드를 반영해야 한다.
        // --- 카메라 촬영 후 미디어 컨텐트 uri 를 생성해서 외부저장소에 저장한다 ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        // --- 여기 까지 컨텐트 uri 강제세팅 ---
        startActivityForResult(intent, REQ_CAMERA);
    }

    public void getPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");//외부 저장소에 있는 이미지만 가져오기 위한 필터리
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_GALLERY:
                if (resultCode == RESULT_OK) {
                    fileUri = data.getData();
                    try {
                        saveToList(new Memo("사진메모", fileUri.toString(), ""));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQ_CAMERA:
                if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) { // 사진 확인처리됨 RESULT_OK = -1
                    // 롤리팝 체크
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        fileUri = data.getData();
                        try {
                            saveToList(new Memo("카메라메모", fileUri.toString(), ""));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fileUri != null) {
                        try {
                            saveToList(new Memo("사진메모", fileUri.toString(), ""));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "사진파일이 없습니다", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // resultCode 가 0이고 사진이 찍혔으면 uri 가 남는데
                    // uri 가 있을 경우 삭제처리...
                }
                break;
        }
    }
}
