package kd.master.de.todolist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
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
        editText = (EditText)findViewById(R.id.editText);
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_task:
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("New Task").
                        setMessage("Add a new task:").
                        setView(taskEditText).
                        setPositiveButton("Add", new DialogInterface.OnClickListener(){
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
                        })

                        .setNegativeButton("cancel",null).create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void deleteTask(View view){
        View parent = (View)view.getParent();
        TextView taskView = (TextView)parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskView.getText());
        delete(task);
    }



    public void editTask(View view){
        View parent = (View)view.getParent();
        TextView taskView = (TextView)parent.findViewById(R.id.task_title);

        String task = String.valueOf(taskView.getText());
        editText.setText(task);
        delete(task);
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
            mAdatper = new ArrayAdapter<>(this, R.layout.list_todo, R.id.task_title, taskList);
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
