package com.compdigitec.libvlcandroidsample;

import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.compdigitec.libvlcandroidsample.bean.Record;
import com.compdigitec.libvlcandroidsample.bean.Word;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ShowOnlineMovies extends Activity {

    private ListView lv = null;
    private ListAdapter la;
    private int recordCount = 0;
    private Button pre = null;
    private Button next = null;
    private TextView curPage = null;
    private int totalPage = 0;
    private int pageSize = 7;
    private int curPageIndex = 1;

    private ProgressDialog pDialog;

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.online_movie);
        View footerView = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.review_footer, null, false);

        lv = (ListView)findViewById(R.id.online_movie_list);
        new GetContacts().execute();
        Utils.statistics(getApplicationContext(),"online_movie");

    }





    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        List<TasksManagerModel> movies = new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ShowOnlineMovies.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try
            {
                final String content = Utils.connect("http://172.16.47.13:3000/player/movies.json");
                JSONObject jo = new JSONObject(content);
                JSONArray joes = jo.getJSONArray("data");
                for(int i = 0; i < joes.length(); i++)
                {
                    JSONObject jo_ = joes.getJSONObject(i);
                    TasksManagerModel tm = new TasksManagerModel();
                    tm.setUrl(jo_.getString("url"));
                    String name = jo_.getString("name");
                    tm.setPath(Utils.baseDir().getPath()+"/"+name+".mp4");
                    this.movies.add(tm);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new ListAdapter(ShowOnlineMovies.this,this.movies);
            lv.setAdapter(adapter);
        }

    }


    class ViewHolder{
        private View itemView = null;
        public ViewHolder(View itemView) {
            this.itemView = itemView;
            assignViews();
        }

        private View findViewById(final int id) {
            return itemView.findViewById(id);
        }



        /**
         * viewHolder position
         */
        private int position;
        /**
         * download id
         */
        private int id;

        public void update(final int id, final int position) {
            this.id = id;
            this.position = position;
        }


        public void updateDownloaded() {
            taskPb.setMax(1);
            taskPb.setProgress(1);

            taskStatusTv.setText(R.string.tasks_manager_demo_status_completed);
            taskActionBtn.setText(R.string.delete);
        }

        public void updateNotDownloaded(final int status, final long sofar, final long total) {
            if (sofar > 0 && total > 0) {
                final float percent = sofar
                        / (float) total;
                taskPb.setMax(100);
                taskPb.setProgress((int) (percent * 100));
            } else {
                taskPb.setMax(1);
                taskPb.setProgress(0);
            }

            switch (status) {
                case FileDownloadStatus.error:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_error);
                    break;
                case FileDownloadStatus.paused:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
                    break;
                default:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_not_downloaded);
                    break;
            }
            taskActionBtn.setText(R.string.start);
        }

        public void updateDownloading(final int status, final long sofar, final long total) {
            final float percent = sofar
                    / (float) total;
            taskPb.setMax(100);
            taskPb.setProgress((int) (percent * 100));

            switch (status) {
                case FileDownloadStatus.pending:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
                    break;
                case FileDownloadStatus.started:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
                    break;
                case FileDownloadStatus.connected:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
                    break;
                case FileDownloadStatus.progress:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_progress);
                    break;
                default:
                    taskStatusTv.setText(EnPlayerApplication.CONTEXT.getString(
                            R.string.tasks_manager_demo_status_downloading, status));
                    break;
            }

            taskActionBtn.setText(R.string.pause);
        }

        private TextView taskNameTv;
        private TextView taskStatusTv;
        private ProgressBar taskPb;
        private Button taskActionBtn;

        private void assignViews() {
            taskNameTv = (TextView) itemView.findViewById(R.id.mname);
            taskStatusTv = (TextView) itemView.findViewById(R.id.task_status_tv);
            taskPb = (ProgressBar) itemView.findViewById(R.id.task_pb);
            taskActionBtn = (Button) itemView.findViewById(R.id.mdownload);
        }

    }

    class ListAdapter  extends BaseAdapter
    {
        private LayoutInflater mInflater;
        private Context ctx = null;
        List<TasksManagerModel> movies = new ArrayList<>();

        public ListAdapter(Context ctx,List<TasksManagerModel> movies)
        {
            this.ctx = ctx;
            this.mInflater = LayoutInflater.from(ctx);
            this.movies = movies;
        }

        @Override
        public int getCount() {
            return movies == null ? 0 : movies.size();
        }

        @Override
        public TasksManagerModel getItem(int i) {
            try
            {
                return movies.get(i);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.online_movie_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
                holder.assignViews();

            }else {
                holder = (ViewHolder)convertView.getTag();
            }

            try
            {
                TasksManagerModel tmm = getItem(i);

                String name = tmm.getName();
                String url = tmm.getUrl();
                holder.taskNameTv.setText(name + ";" + url);
                holder.taskActionBtn.setTag(tmm);
                holder.taskActionBtn.setOnClickListener(new BtnClickListener(holder));

            }
            catch (Exception e)
            {
            }


            return convertView;
        }
    }

    class MyFileDownloadListener extends FileDownloadListener
    {
        private ViewHolder tag = null;

        public MyFileDownloadListener(ViewHolder holder)
        {
                this.tag = holder;
        }

        @Override
        protected void warn(BaseDownloadTask task){

        }

        @Override
        public void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            tag.updateDownloading(FileDownloadStatus.pending, soFarBytes
                    , totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes);

            tag.updateDownloading(FileDownloadStatus.connected, soFarBytes
                    , totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            tag.updateDownloading(FileDownloadStatus.progress, soFarBytes
                    , totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {

            tag.updateNotDownloaded(FileDownloadStatus.error, task.getLargeFileSoFarBytes()
                    , task.getLargeFileTotalBytes());
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            tag.updateDownloaded();
        }
    }


    class BtnClickListener implements View.OnClickListener
    {
        private ViewHolder holder;

        public BtnClickListener(ViewHolder vh)
        {
                this.holder = vh;
        }

        @Override
        public void onClick(View v) {


            CharSequence action = ((TextView) v).getText();
            if (action.equals(v.getResources().getString(R.string.pause))) {
                // to pause
                FileDownloader.getImpl().pause(holder.id);
            } else if (action.equals(v.getResources().getString(R.string.start))) {
                // to start
                // to start
                final TasksManagerModel model =  (TasksManagerModel)v.getTag();
                final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                        .setPath(model.getPath())
                        .setCallbackProgressTimes(100)
                        .setListener(new MyFileDownloadListener(holder));
                holder.updateDownloading(FileDownloadStatus.started,0,0);
                int id = task.start();
                holder.id = id;
            } else if (action.equals(v.getResources().getString(R.string.delete))) {
                // to delete
                new File("").delete();
                holder.taskActionBtn.setEnabled(true);
                holder.updateNotDownloaded(FileDownloadStatus.INVALID_STATUS, 0, 0);
            }
        }
    };
}






class TasksManagerDBController {
    public final static String TABLE_NAME = "tasksmanger";
    private final SQLiteDatabase db;

    private TasksManagerDBController() {
        TasksManagerDBOpenHelper openHelper = new TasksManagerDBOpenHelper(EnPlayerApplication.CONTEXT);

        db = openHelper.getWritableDatabase();
    }

    public List<TasksManagerModel> getAllTasks() {
        final Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        final List<TasksManagerModel> list = new ArrayList<>();
        try {
            if (!c.moveToLast()) {
                return list;
            }

            do {
                TasksManagerModel model = new TasksManagerModel();
                model.setId(c.getInt(c.getColumnIndex(TasksManagerModel.ID)));
                model.setName(c.getString(c.getColumnIndex(TasksManagerModel.NAME)));
                model.setUrl(c.getString(c.getColumnIndex(TasksManagerModel.URL)));
                model.setPath(c.getString(c.getColumnIndex(TasksManagerModel.PATH)));
                list.add(model);
            } while (c.moveToPrevious());
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return list;
    }

    public TasksManagerModel addTask(final String url, final String path) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return null;
        }

        // have to use FileDownloadUtils.generateId to associate TasksManagerModel with FileDownloader
        final int id = FileDownloadUtils.generateId(url, path);

        TasksManagerModel model = new TasksManagerModel();
        model.setId(id);
        model.setName(EnPlayerApplication.CONTEXT.getString(R.string.tasks_manager_demo_name, id));
        model.setUrl(url);
        model.setPath(path);

        final boolean succeed = db.insert(TABLE_NAME, null, model.toContentValues()) != -1;
        return succeed ? model : null;
    }


}

// ----------------------- model
class TasksManagerDBOpenHelper extends SQLiteOpenHelper {
    public final static String DATABASE_NAME = "tasksmanager.db";
    public final static int DATABASE_VERSION = 2;

    public TasksManagerDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + TasksManagerDBController.TABLE_NAME
                + String.format(
                "("
                        + "%s INTEGER PRIMARY KEY, " // id, download id
                        + "%s VARCHAR, " // name
                        + "%s VARCHAR, " // url
                        + "%s VARCHAR " // path
                        + ")"
                , TasksManagerModel.ID
                , TasksManagerModel.NAME
                , TasksManagerModel.URL
                , TasksManagerModel.PATH

        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.delete(TasksManagerDBController.TABLE_NAME, null, null);
        }
    }
}

class TasksManagerModel {
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String URL = "url";
    public final static String PATH = "path";

    private int id;
    private String name;
    private String url;
    private String path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ID, id);
        cv.put(NAME, name);
        cv.put(URL, url);
        cv.put(PATH, path);
        return cv;
    }
}
