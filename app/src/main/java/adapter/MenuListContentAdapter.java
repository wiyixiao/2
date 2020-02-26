//https://blog.csdn.net/qq_32059827/article/details/53644380

package adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robottime.R;
import com.example.robottime.menu.MenuListContentModel;

import java.util.List;

public class MenuListContentAdapter extends BaseAdapter {

    private Context context;
    private List<MenuListContentModel> list;

    public MenuListContentAdapter(Context context, List<MenuListContentModel> list) {
        super();
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (list != null) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold hold;
        if (convertView == null) {
            hold = new ViewHold();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_menu, null);
            convertView.setTag(hold);
        } else {
            hold = (ViewHold) convertView.getTag();
        }

        hold.imageView = (ImageView) convertView
                .findViewById(R.id.item_imageview);
        hold.textView = (TextView) convertView.findViewById(R.id.item_textview);

        hold.imageView.setImageResource(list.get(position).getImageView());
        hold.textView.setText(list.get(position).getText());
        return convertView;
    }

    class ViewHold {
        public ImageView imageView;
        public TextView textView;
    }

}
