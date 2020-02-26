package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.robottime.R;

import java.util.List;

public class UsbDeviceAdapter extends BaseAdapter {
    private Context context;
    private ViewHolder holder;
    private LayoutInflater mInflater;
    private List<UsbDevice> usbDeviceList;

    public UsbDeviceAdapter(Context context)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setList(List<UsbDevice> devices)
    {
        this.usbDeviceList = devices;
    }

    @Override
    public int getCount()
    {
        return this.usbDeviceList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return usbDeviceList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View converView, ViewGroup parent)
    {
        if(converView == null)
        {
            holder = new ViewHolder();
            converView = mInflater.inflate(R.layout.item_otgdevice_list, null);
            final TextView deviceInfo = (TextView) converView.findViewById(R.id.otg_info);
            final TextView driverInfo = (TextView)converView.findViewById(R.id.otg_driver);

            final UsbDevice device = usbDeviceList.get(position);

            deviceInfo.setText(String.format("%s - VPID(%4X:%4X)",
                    device.getProductName() ,
                    device.getVendorId(),
                    device.getProductId()));
            driverInfo.setVisibility(View.GONE);

            converView.setTag(holder);
        }else {
            holder = (ViewHolder) converView.getTag();
        }

        return converView;
    }

    private class ViewHolder{
        TextView deviceInfo;
        TextView driverInfo;
    }
}
