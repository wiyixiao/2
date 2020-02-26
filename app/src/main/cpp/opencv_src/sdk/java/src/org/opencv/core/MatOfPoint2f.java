package opencv_src.sdk.java.src.org.opencv.core;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;

import java.util.Arrays;
import java.util.List;

public class MatOfPoint2f extends org.opencv.core.Mat {
    // 32FC2
    private static final int _depth = org.opencv.core.CvType.CV_32F;
    private static final int _channels = 2;

    public MatOfPoint2f() {
        super();
    }

    protected MatOfPoint2f(long addr) {
        super(addr);
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public static MatOfPoint2f fromNativeAddr(long addr) {
        return new MatOfPoint2f(addr);
    }

    public MatOfPoint2f(Mat m) {
        super(m, Range.all());
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public MatOfPoint2f(org.opencv.core.Point...a) {
        super();
        fromArray(a);
    }

    public void alloc(int elemNumber) {
        if(elemNumber>0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(org.opencv.core.Point...a) {
        if(a==null || a.length==0)
            return;
        int num = a.length;
        alloc(num);
        float buff[] = new float[num * _channels];
        for(int i=0; i<num; i++) {
            org.opencv.core.Point p = a[i];
            buff[_channels*i+0] = (float) p.x;
            buff[_channels*i+1] = (float) p.y;
        }
        put(0, 0, buff); //TODO: check ret val!
    }

    public org.opencv.core.Point[] toArray() {
        int num = (int) total();
        org.opencv.core.Point[] ap = new org.opencv.core.Point[num];
        if(num == 0)
            return ap;
        float buff[] = new float[num * _channels];
        get(0, 0, buff); //TODO: check ret val!
        for(int i=0; i<num; i++)
            ap[i] = new org.opencv.core.Point(buff[i*_channels], buff[i*_channels+1]);
        return ap;
    }

    public void fromList(List<org.opencv.core.Point> lp) {
        org.opencv.core.Point ap[] = lp.toArray(new org.opencv.core.Point[0]);
        fromArray(ap);
    }

    public List<org.opencv.core.Point> toList() {
        Point[] ap = toArray();
        return Arrays.asList(ap);
    }
}
