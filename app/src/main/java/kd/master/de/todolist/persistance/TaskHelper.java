package kd.master.de.todolist.persistance;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskHelper extends SQLiteOpenHelper{

    public TaskHelper(Context context){
        super(context,Task.DB_NAME,null, Task.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");
        builder.append(Task.TaskEntry.TABLE).append(" ( ");
        builder.append(Task.TaskEntry._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        builder.append(Task.TaskEntry.COL_TASK_TITLE).append(" TEXT NOT NULL );");

        String sql = builder.toString();
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+ Task.TaskEntry.TABLE);
        onCreate(db);
    }
}
