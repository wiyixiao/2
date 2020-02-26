//
// Created by xy on 2020/1/3.
//

#ifndef INC_2_TRACKING_H
#define INC_2_TRACKING_H

#include "common.h"

#define FACE_MODE 0
#define COLOR_MODE 1

using namespace std;
using namespace cv;

typedef struct
{
    int cx;
    int cy;
    int cd; //直径？？或宽度//用作距离变化的依据
    int face_num;
}ObjInfo; //识别的信息

typedef struct
{
    int hmin, hmax;
    int smin, smax;
    int vmin, vmax;
}HsvInfo;

class CvHelper{

public:
    CvHelper();
    ~CvHelper();
    const char* cv_getVersion();
    void
        cv_disableTrack(),
        cv_trackColor(int enable);
    int
        cv_revDataHandler(Mat &),
        cv_setSelection(int, int, int),
        cv_faceDetect(),
        cv_loadCascade(const char * file);

    void
        cv_trackColor();

    //返回的信息
    ObjInfo objinfo;
    HsvInfo hsvinfo;
    //缩放比
    double scale;
private:
    void
        _changeMode(int ),
        _trackingColor(Mat &, Mat &),
        _faceDetect(Mat &, Mat &);

    //模式使能
    bool
        trackingEnable,
        faceDetectEnable;

    //face
    CascadeClassifier cascade;
    //color
    Rect
        trackWindow,
        selection;//选中的区域
    bool
        backprojMode,
        selectObject;//用来判断是否选中
    int
        hsize,
        trackObject;

    Mat
        hsv,
        hue,
        mask,
        hist,
        histimg,
        backproj,
        frame_face,
        frame_color;
    const float* phranges;
    float hranges[2];
    Vec3b trackBoxColor;
};

#endif //INC_2_TRACKING_H
