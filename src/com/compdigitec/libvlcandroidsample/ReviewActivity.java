package com.compdigitec.libvlcandroidsample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.compdigitec.libvlcandroidsample.bean.Record;
import com.compdigitec.libvlcandroidsample.bean.Word;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


class ViewHolder{

    public TextView word;
    public TextView mean_cn;
    public TextView sentence;
}

class ListAdapter  extends BaseAdapter
{
    private List<Record> rs = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context ctx = null;

    public ListAdapter(Context ctx,int offset,int pageSize)
    {
        this.ctx = ctx;
        this.mInflater = LayoutInflater.from(ctx);
        rs = Dao.getInstance(ctx.getApplicationContext()).findRecords(offset, pageSize, Dao.ORDER_DESC);
        System.out.println(rs);
    }

    @Override
    public int getCount() {
        return rs.size();
    }

    @Override
    public Object getItem(int i) {
        return rs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        ViewHolder holder = null;
        if (convertView == null) {

            holder=new ViewHolder();

            convertView = mInflater.inflate(R.layout.review_item, null);
            holder.word = (TextView) convertView.findViewById(R.id.word);
            holder.mean_cn = (TextView) convertView.findViewById(R.id.mean_cn);
            holder.sentence = (TextView) convertView.findViewById(R.id.sentence);
            holder.word.setTextColor(Color.BLACK);
            holder.sentence.setTextColor(Color.BLACK);

            convertView.setTag(holder);

        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        Record r= rs.get(i);
        Word w  = Dao.getInstance(ctx).findWordsByWord(r.getWord());

        String accent = w == null ? "" : w.getAccent();
        String mean_cn = w == null ? "" : w.getMean_cn();

        holder.word.setText(r.getWord()+"  "+ accent);
        holder.mean_cn.setText(mean_cn);

        String sub = r.getSubtitle();
        Spanned sp = Html.fromHtml(sub);
        String content = sp.toString().trim();
        holder.sentence.setText(content);


        return convertView;

    }
}



public class ReviewActivity extends Activity {

    private ListView lv = null;
    private ListAdapter la;
    private int recordCount = 0;
    private Button pre = null;
    private Button next = null;
    private TextView curPage = null;
    private int totalPage = 0;
    private int pageSize = 2;
    private int curPageIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.review);
        pre = (Button)findViewById(R.id.pre_record_page);
        next = (Button)findViewById(R.id.next_record_page);
        curPage = (TextView)findViewById(R.id.show_cur_page);

        this.recordCount = Dao.getInstance(getApplicationContext()).recordsCount();
        this.totalPage = recordCount / pageSize;
        this.curPageIndex = 1;

        lv = (ListView)findViewById(R.id.reviewList);
        la = new ListAdapter(this,0,pageSize);
        lv.setAdapter(la);

        curPage.setText(curPageIndex+"/"+this.totalPage);

        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curPageIndex > 1)
                {
                    curPageIndex = curPageIndex-1;
                    la = new ListAdapter(ReviewActivity.this,(curPageIndex-1)*pageSize,pageSize);
                    lv.setAdapter(la);
                    la.notifyDataSetChanged();

                }
                curPage.setText(curPageIndex+"/"+totalPage);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curPageIndex < totalPage)
                {
                    curPageIndex = curPageIndex+1;
                    la = new ListAdapter(ReviewActivity.this,(curPageIndex-1)*pageSize,pageSize);
                    la.notifyDataSetChanged();
                    lv.setAdapter(la);
                }

                curPage.setText(curPageIndex+"/"+totalPage);
            }
        });
    }
}