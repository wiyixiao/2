package opencv_src.sdk.java.src.org.opencv.core;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;

import java.util.Arrays;
import java.util.List;


public class MatOfRect extends org.opencv.core.Mat {
    // 32SC4
    private static final int _depth = org.opencv.core.CvType.CV_32S;
    private static final int _channels = 4;

    public MatOfRect() {
        super();
    }

    protected MatOfRect(long addr) {
        super(addr);
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public static MatOfRect fromNativeAddr(long addr) {
        return new MatOfRect(addr);
    }

    public MatOfRect(Mat m) {
        super(m, Range.all());
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public MatOfRect(org.opencv.core.Rect...a) {
        super();
        fromArray(a);
    }

    public void alloc(int elemNumber) {
        if(elemNumber>0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(org.opencv.core.Rect...a) {
        if(a==null || a.length==0)
            return;
        int num = a.length;
        alloc(num);
        int buff[] = new int[num * _channels];
        for(int i=0; i<num; i++) {
            org.opencv.core.Rect r = a[i];
            buff[_channels*i+0] = (int) r.x;
            buff[_channels*i+1] = (int) r.y;
            buff[_channels*i+2] = (int) r.width;
            buff[_channels*i+3] = (int) r.height;
        }
        put(0, 0, buff); //TODO: check ret val!
    }


    public org.opencv.core.Rect[] toArray() {
        int num = (int) total();
        org.opencv.core.Rect[] a = new org.opencv.core.Rect[num];
        if(num == 0)
            return a;
        int buff[] = new int[num * _channels];
        get(0, 0, buff); //TODO: check ret val!
        for(int i=0; i<num; i++)
            a[i] = new org.opencv.core.Rect(buff[i*_channels], buff[i*_channels+1], buff[i*_channels+2], buff[i*_channels+3]);
        return a;
    }
    public void fromList(List<org.opencv.core.Rect> lr) {
        org.opencv.core.Rect ap[] = lr.toArray(new org.opencv.core.Rect[0]);
        fromArray(ap);
    }

    public List<org.opencv.core.Rect> toList() {
        Rect[] ar = toArray();
        return Arrays.asList(ar);
    }
}
