package com.csi5175.mobilecommerce.quicknote;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.facebook.share.model.ShareLinkContent;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
//import com.facebook.login.LoginResult;
//import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;


public class MainActivity extends AppCompatActivity {
    ImageButton add;
    public ListView listView;
    private SearchView searchView;
    private SimpleCursorAdapter listAdapter;
//    LoginButton fbLoginButton;
//    ShareButton fbShareButton;
    ShareDialog fbShareDialog;
//    CallbackManager fbCallbackManager;
    private static final String DATABASE_NAME = "Mydb";
    private static final String TABLE_NAME = "notes";
    public static final String ID = "_id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String PATH = "path";
    public static final String TIME = "time";
    private SQLiteDatabase sqlDB;
    private Cursor c;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        sqlDB = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY,null);

        c = sqlDB.query(TABLE_NAME, null, null, null, null, null, null);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(RefreshAdapter(c));
        listView.invalidateViews();

        try {
            //sqlDB.execSQL("create table " + TABLE_NAME + " (_id text,title text  ,time text, context text)");
            sqlDB.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT NOT NULL,"
                    + TIME+ " TEXT NOT NULL,"+ CONTENT + " TEXT NOT NULL,"
                    + PATH + " TEXT)");
        }catch(Exception e){
            // Toast.makeText(getApplicationContext(),"table existed",Toast.LENGTH_SHORT).show();
        }


        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){

                    // listView.setFilterText(newText);
                    //cursor = sqlDB.query(TABLE_NAME, null, null, null, null, null, null);
                    c = sqlDB.rawQuery("SELECT * FROM notes WHERE TITLE LIKE '%" + newText + "%' OR CONTENT LIKE '%" + newText + "%'", null);
                    listView = (ListView) findViewById(R.id.listview);
                    listView.setAdapter(RefreshAdapter(c));
                    listView.invalidateViews();
                }else{

                    //  listView.clearTextFilter();
                    c= sqlDB.query(TABLE_NAME, null, null, null, null, null, null);
                    listView = (ListView) findViewById(R.id.listview);
                    listView.setAdapter(RefreshAdapter(c));
                    listView.invalidateViews();
                }
                return false;
            }
        });

        //enter add note page
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getApplicationContext(),"aaaa",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), AddNote.class);
                i.putExtra(ID, c.getString(0));
                i.putExtra(TITLE, c.getString(1));
                i.putExtra(TIME, c.getString(2));
                i.putExtra(CONTENT, c.getString(3));
                i.putExtra(PATH, c.getString(4));
                startActivity(i);
            }
        });

//        // Facebook Login on QuickNote
//        fbCallbackManager = CallbackManager.Factory.create();
//        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
//        fbLoginButton.setReadPermissions("email, publish_actions");
//        fbLoginButton.registerCallback(fbCallbackManager,
//                new FacebookCallback<LoginResult>() {
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        // App code
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        // App code
//                    }
//
//                    @Override
//                    public void onError(FacebookException exception) {
//                        // App code
//                    }
//                });
//
//        // Facebook Share on QuickNote
//        fbShareButton = (ShareButton) findViewById(R.id.share_button);
//        fbShareDialog = new ShareDialog(this);
//        fbShareButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        fbShareDialog = new ShareDialog(this);

        //long click to delete
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {

                //Toast.makeText(getApplicationContext(),"Longpress",Toast.LENGTH_SHORT).show();

                final CharSequence[] optionList = {"Share on Facebook", "Delete"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Options");
                builder.setItems(optionList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(optionList[i].equals("Share on Facebook")) {
                            if(fbShareDialog.canShow(ShareLinkContent.class)) {
                                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                        .setQuote(c.getString(1) + "\n\n" + c.getString(3))
//                                        .setContentTitle(c.getString(1))
//                                        .setContentDescription(c.getString(3))
                                        .setContentUrl(Uri.parse("https://developers.facebook.com/docs/sharing/opengraph/android"))
                                        .setImageUrl(Uri.parse("https://www.cabq.gov/culturalservices/biopark/images/share-on-facebook.png"))
                                        //.setImageUrl()
                                        .build();
                                fbShareDialog.show(linkContent);
                            }
                        }
                        if(optionList[i].equals("Delete")) {
                            c.moveToPosition(position);
                            String id = c.getString(0);
                            if(!id.equals("")) {
                                sqlDB.delete(TABLE_NAME, ID + "=" + id, null);
                            }
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });

//                builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
//                    public void onClick (DialogInterface arg0,int arg1)
//                    {
//                        // Toast.makeText(getApplicationContext(),"cancel",Toast.LENGTH_SHORT).show();
//                    }
//
//                });
//
//                builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
//                    public void onClick(DialogInterface arg0,int arg1)
//                    {
//                        //Toast.makeText(getApplicationContext(),"ok",Toast.LENGTH_SHORT).show();
//
//                        c.moveToPosition(position);
//                        String id = c.getString(0);
//                        if(!id.equals("")) {
//                            sqlDB.delete(TABLE_NAME, ID + "=" + id, null);
//                        }
//                        Intent i=new Intent(getApplicationContext(),MainActivity.class);
//                        startActivity(i);
//                    }
//                });

                builder.show();

                return true;
            }
        });



        add = (ImageButton) findViewById(R.id.imageButton);
        add.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(),AddNote.class);
                startActivity(i);
            }
        });

    }

    private SimpleCursorAdapter RefreshAdapter(Cursor c){
        SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
                R.layout.listview_detail,
                c,
                new String[]{"title", "time"},
                new int[]{R.id.title1, R.id.time1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        return  listAdapter;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sqlDB.close();
    }
}

