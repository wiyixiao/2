package adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.robottime.R;

public class BlueToothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    private final LayoutInflater mInflater;
    private int mResource;

    public BlueToothDeviceAdapter(Context context, int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.device_name);
        TextView info = (TextView) convertView.findViewById(R.id.device_address);
        TextView state = (TextView) convertView.findViewById(R.id.device_state);

        BluetoothDevice device = getItem(position);

        assert device != null;
        name.setText(String.format("名字：%s", device.getName()));
        info.setText(String.format("地址：%s", device.getAddress()));

        //设备的状态（BOND_BONDED：已绑定 常量值：12, BOND_BONDING：绑定中 常量值：11, BOND_NONE：未匹配 常量值：10）
        int deviceState = device.getBondState();
        if (deviceState == 10) {
            state.setText("状态：未匹配");
        } else if (deviceState == 11) {
            state.setText("状态：绑定中");
        } else if (deviceState == 12) {
            state.setText("状态：已绑定");
        }

        return convertView;
    }
}
