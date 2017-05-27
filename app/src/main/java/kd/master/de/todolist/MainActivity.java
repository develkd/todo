package kd.master.de.todolist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kd.master.de.todolist.persistance.Task;
import kd.master.de.todolist.persistance.TaskHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity";
    private TaskHelper mHelper;
    private ListView mTaskListView;
    private EditText editText;
    private ArrayAdapter<String> mAdatper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LayoutInflater inflater = this.getLayoutInflater();
        mHelper = new TaskHelper(this);
        mTaskListView = (ListView)findViewById(R.id.list_todo);

        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                editSelectedDialog((ViewGroup)view);
                //  editSelectedDialog((TextView)findViewById(R.id.list_item));
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.item_edit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
               //         .setAction("Action", null).show();
                addNewTaskDialog();
            }
        });

        //  editText = (EditText)findViewById(R.id.editText);
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_task:
                addNewTaskDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void addNewTaskDialog(){

        final EditText taskEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Eintrag").
                setMessage("Hinzufügen:").
                setView(taskEditText).
                setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        SQLiteDatabase db = mHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(Task.TaskEntry.COL_TASK_TITLE, task);
                        db.insertWithOnConflict(Task.TaskEntry.TABLE, null,values,SQLiteDatabase.CONFLICT_REPLACE);
                        db.close();
                        updateUI();
                    }
                }).setNegativeButton("Abbrechen",null).create();
        dialog.show();

    }


    public void editSelectedDialog(ViewGroup layout ){

        final EditText taskEditText = new EditText(this);
        final   String updateable = String.valueOf(((TextView)layout.findViewById(R.id.list_item)).getText());

        taskEditText.setText(updateable);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Eintrag").
                setMessage("Bearbeiten:").
                setView(taskEditText).
                setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        SQLiteDatabase db = mHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(Task.TaskEntry.COL_TASK_TITLE, task);
                        db.updateWithOnConflict(Task.TaskEntry.TABLE, values,Task.TaskEntry.COL_TASK_TITLE+ " = ?", new String[]{updateable},SQLiteDatabase.CONFLICT_REPLACE);
                        db.close();
                        updateUI();
                    }
                }).setNegativeButton("Abbrechen",null).create();
        dialog.show();

    }



    public void deleteTask(View view){
        View parent = (View)view.getParent();
        TextView taskView = (TextView)parent.findViewById(R.id.list_item);
        String task = String.valueOf(taskView.getText());
        delete(task);
    }



    public void editTask(View view){
        View parent = (View)view.getParent();
        TextView taskView = (TextView)parent.findViewById(R.id.list_item);

        String task = String.valueOf(taskView.getText());
        editText.setText(task);
        delete(task);
    }


    public void addTask(View view){
        String task = String.valueOf(editText.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Task.TaskEntry.COL_TASK_TITLE, task);
        db.insertWithOnConflict(Task.TaskEntry.TABLE, null,values,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        editText.setText(null);
        updateUI();

    }
    private void delete(String task){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(Task.TaskEntry.TABLE, Task.TaskEntry.COL_TASK_TITLE+ " = ?", new String[]{task});
        db.close();
        updateUI();
    }


    private void updateUI(){
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(Task.TaskEntry.TABLE,
                new String[]{Task.TaskEntry._ID,
                        Task.TaskEntry.COL_TASK_TITLE},null,null,null,null,null);

        while (cursor.moveToNext()){
            int index = cursor.getColumnIndex(Task.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(index));
        }

        if(null == mAdatper){
            mAdatper = new ArrayAdapter<>(this, R.layout.list_todo, R.id.list_item, taskList);
            mTaskListView.setAdapter(mAdatper);
        }else{
            mAdatper.clear();
            mAdatper.addAll(taskList);
            mAdatper.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }
}
