//2019/1/12
//https://blog.csdn.net/xyzz609/article/details/51916222
//手机端作为从机--Host
//HC-05作为主机--Slave
package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robottime.R;
import com.example.robottime.msg.Host;
import com.example.robottime.msg.Slave;

import java.util.ArrayList;

public class MsgAdapter extends BaseAdapter {

    //定义两个类别标志
    private static final int TYPE_HOST = 0;
    private static final int TYPE_SLAVE = 1;
    private Context mContext;
    private ArrayList<Object> mData = null;


    public MsgAdapter(Context mContext,ArrayList<Object> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //多布局的核心，通过这个判断类别
    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof Slave) {
            return TYPE_HOST;
        } else if (mData.get(position) instanceof Host) {
            return TYPE_SLAVE;
        } else {
            return super.getItemViewType(position);
        }
    }

    //类别数目
    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        ViewHolder1 holder1 = null;
        ViewHolder2 holder2 = null;
        if(convertView == null){
            switch (type){
                case TYPE_SLAVE:
                    holder1 = new ViewHolder1();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_host_msg, parent, false);
                    holder1.img_icon = (ImageView) convertView.findViewById(R.id.img_icon);
                    holder1.txt_aname = (TextView) convertView.findViewById(R.id.txt_aname);
                    convertView.setTag(R.id.Tag_SLAVE,holder1);
                    break;
                case TYPE_HOST:
                    holder2 = new ViewHolder2();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_slave_msg, parent, false);
                    holder2.txt_bname = (TextView) convertView.findViewById(R.id.txt_bname);
                    holder2.img_bicon = (ImageView) convertView.findViewById(R.id.img_bicon);
                    convertView.setTag(R.id.Tag_HOST,holder2);
                    break;
            }
        }else{
            switch (type){
                case TYPE_SLAVE:
                    holder1 = (ViewHolder1) convertView.getTag(R.id.Tag_SLAVE);
                    break;
                case TYPE_HOST:
                    holder2 = (ViewHolder2) convertView.getTag(R.id.Tag_HOST);
                    break;
            }
        }

        Object obj = mData.get(position);
        //设置下控件的值
        switch (type){
            case TYPE_SLAVE:
                Host slave = (Host) obj;
                if(slave != null){
                    holder1.img_icon.setImageResource(slave.getaIcon());
                    holder1.txt_aname.setText(slave.getaName());
                }
                break;
            case TYPE_HOST:
                Slave book = (Slave) obj;
                if(book != null){
                    holder2.txt_bname.setText(book.getbName());
                    holder2.img_bicon.setImageResource(book.getbIcon());
                }
                break;
        }
        return convertView;
    }


    //两个不同的ViewHolder
    private static class ViewHolder1{
        ImageView img_icon;
        TextView txt_aname;
    }

    private static class ViewHolder2{
        TextView txt_bname;
        ImageView img_bicon;
    }
}