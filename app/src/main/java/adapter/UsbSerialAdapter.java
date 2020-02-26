package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.robottime.R;
import com.example.robottime.usb_serial.driver.UsbSerialPort;

import java.util.List;

public class UsbSerialAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater mInflater;
    private List<UsbSerialPort> usbSerialPortList;

    public UsbSerialAdapter(Context context)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setList(List<UsbSerialPort> devices)
    {
        this.usbSerialPortList = devices;
    }

    @Override
    public int getCount()
    {
        return this.usbSerialPortList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return usbSerialPortList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View converView, ViewGroup parent)
    {
        return converView;
    }

    private class ViewHolder{
        TextView deviceInfo;
        TextView driverInfo;
    }

}
