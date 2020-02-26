//
// Created by xy on 2020/1/2.
//
#include "cv.h"

template <typename T>
string operator &(const T &_t, const string &s)
{
	ostringstream oss;
	oss << _t;
	return oss.str() + s;
}

template <typename T>
string operator &(const string &s, const T &_t)
{
	ostringstream oss;
	oss << _t;
	return  s + oss.str() ;
}

static void ResizeBox(RotatedRect &box, double k)
{
    box.center *= k;
    box.size.width *= k;
    box.size.height *= k;
}

static void ResizeWindow(Rect &win, double k)
{
    win.x *= k;
    win.y *= k;
    win.width *= k;
    win.height *= k;
}
/********************************************init*************************************************/

CvHelper::CvHelper()
{
    faceDetectEnable = 1;
    trackingEnable = 0;
    scale = 3.0;
    backprojMode = false;
    selectObject = false;
    trackObject = 0;
    hsvinfo.vmin = 110;
    hsvinfo.vmax = 256;
    hsvinfo.smin = 150;
    histimg = Mat::zeros(100, 220, CV_8UC3);
    hranges[0] = 0;
    hranges[1] = 180;
    phranges = hranges;

}

CvHelper::~CvHelper()
{
    ;
}

const char* CvHelper::cv_getVersion()
{
    return CV_VERSION;
}

/*******************************************process***********************************************/
void CvHelper::_faceDetect(Mat &srcImg, Mat &img)
{
	if (faceDetectEnable == 0)
	{
	    img.copyTo(frame_face);
	    cvtColor( frame_face, frame_face, COLOR_BGR2GRAY );	// 将源图像转为灰度图
	    //equalizeHist( frame_face, frame_face );	// 直方图均衡化，提高图像质量

        vector<Rect> faces;

        /* 检测目标 */
        cascade.detectMultiScale( frame_face, faces,
            1.1, 2, 0
            //|CASCADE_FIND_BIGGEST_OBJECT
            //|CASCADE_DO_ROUGH_SEARCH
            |CASCADE_SCALE_IMAGE,
            Size(30, 30) );

        if(!faces.size())
        {
            objinfo.face_num = 0;
            return;
        }

        //只检测最大面积人脸
        Rect maxFace;

        for ( size_t i = 0; i < faces.size(); i++ ) // faces.size():检测到的目标数量
        {
            Rect face = faces[i];
            maxFace = face.area() > maxFace.area() ? face : maxFace;
        }

        /* 画矩形框出目标 */
        {
            int cx, cy;

            //中心点
            cx = (maxFace.x + cvRound(maxFace.width/2.0)) * scale;
            cy = (maxFace.y + cvRound(maxFace.height/2.0)) * scale;

            objinfo.cx = cx;
            objinfo.cy = cy;
            objinfo.cd = maxFace.width * scale;
            objinfo.face_num = 1;

            rectangle(	srcImg, Point(maxFace.x, maxFace.y) * scale,
                        Point(maxFace.x + maxFace.width, maxFace.y + maxFace.height) * scale,
                        Scalar(0, 255, 255), 2, 8);
        }

    }
}

void CvHelper::_trackingColor(Mat &srcImg, Mat &img)
{
	if (trackingEnable == 0)
	{//开始跟踪
	    img.copyTo(frame_color);
		cvtColor(frame_color, hsv, CV_BGR2HSV);//将BGR转换成HSV格式，存入hsv中，hsv是3通道

		if (trackObject)
		{
			int _vmin = hsvinfo.vmin, _vmax = hsvinfo.vmax, _smin = hsvinfo.smin;

			//inRange用来检查元素的取值范围是否在另两个矩阵的元素取值之间，返回验证矩阵mask（0-1矩阵）
			//这里用于制作掩膜板，只处理像素值为H:0~180，S:smin~256, V:vmin~vmax之间的部分。mask是要求的，单通道
			inRange(hsv, Scalar(0, _smin, MIN(_vmin, _vmax)),
				Scalar(256, 256, MAX(_vmin, _vmax)), mask);

			int ch[] = { 0, 0 };
			//type包含通道信息，例如CV_8UC3，而深度信息depth不包含通道信息，例如CV_8U.
			hue.create(hsv.size(), hsv.depth());//hue是单通道
			mixChannels(&hsv, 1, &hue, 1, ch, 1);//将H分量拷贝到hue中，其他分量不拷贝。

			if (trackObject < 0)
			{
				//roi为选中区域的矩阵，maskroi为0-1矩阵
				Mat roi(hue, selection), maskroi(mask, selection);
				//绘制色调直方图hist，仅限于用户选定的目标矩形区域
				calcHist(&roi, 1, 0, maskroi, hist, 1, &hsize, &phranges);
				normalize(hist, hist, 0, 255, CV_MINMAX);//必须是单通道，hist是单通道。归一化，范围为0-255

				trackWindow = selection;
				trackObject = 1;//trackObject置1，接下来就不需要再执行这个if块了

				LOGD("&************************[%d]*****************************&", trackObject);

				histimg = Scalar::all(0);//用于显示直方图
										 //计算每个直方的宽度
				int binW = histimg.cols / hsize;//hsize为16，共显示16个
				Mat buf(1, hsize, CV_8UC3);//

				for (int i = 0; i < hsize; i++)
					//直方图每一项的颜色是根据项数变化的
					buf.at<Vec3b>(i) = Vec3b(saturate_cast<uchar>(i*180. / hsize), 255, 255);
				cvtColor(buf, buf, CV_HSV2BGR);
				//量化等级一共有16个等级，故循环16次，画16个直方块

				int maxVal;
				for (int i = 0; i < hsize; i++)
				{
					int val = saturate_cast<int>(hist.at<float>(i)*histimg.rows / 255);//获取直方图每一项的高
                                                                                       //画直方图。opencv中左上角为坐标原点
					rectangle(srcImg, Point(i*binW, srcImg.rows),
						      Point((i + 1)*binW, srcImg.rows - val),
						      Scalar(buf.at<Vec3b>(i)), -1, 8);
                    //获取最大分量对应的颜色值
                    if(val > maxVal)
                    {
                        maxVal = val;
                        trackBoxColor = buf.at<Vec3b>(i);
                    }
				}
			}
			//根据直方图hist计算整幅图像的反向投影图backproj,backproj与hue相同大小
			calcBackProject(&hue, 1, 0, hist, backproj, &phranges);
			//计算两个矩阵backproj、mask的每个元素的按位与，返回backproj
			backproj &= mask;
			//调用最核心的camshift函数
			//TermCriteria是算法完成的条件

			RotatedRect trackBox;
			//乘比例
			Point cp((trackWindow.x + cvRound(trackWindow.width / 2)) * scale,
			         (trackWindow.y + cvRound(trackWindow.height / 2)) * scale);

			int  Iteration = meanShift(backproj, trackWindow,
				TermCriteria(CV_TERMCRIT_EPS | CV_TERMCRIT_ITER, 10, 1));
			if (Iteration != 0 && trackWindow.x >= 0 && trackWindow.y >= 0) {
				trackBox = CamShift(backproj, trackWindow,
					TermCriteria(CV_TERMCRIT_EPS | CV_TERMCRIT_ITER, 10, 1));

			}
			else
			{
			    //lost
				//circle(img, cp, trackWindow.width  * scale, trackBoxColor, 2); //
			}

			//获取跟踪位置
            objinfo.cx = cp.x;
            objinfo.cy = cp.y;
            objinfo.cd = trackWindow.width * scale;
            objinfo.face_num = 0;

			if (trackWindow.area() <= 1)
			{
				int cols = backproj.cols, rows = backproj.rows, r = (MIN(cols, rows) + 5) / 6;
				trackWindow = Rect(Point(trackWindow.x - r, trackWindow.y - r) * scale,
					Point(trackWindow.x + r, trackWindow.y + r) * scale) &
					Rect(0, 0, cols, rows);
			}

			if (backprojMode)//转换显示方式，将backproj显示出来
				cvtColor(backproj, frame_color, CV_GRAY2BGR);
			//画出椭圆，第二个参数是一个矩形，画该矩形的内接圆
			if (trackBox.size.width > 0 && trackBox.size.height > 0)
			{
			    ResizeBox(trackBox, scale);
                ellipse(srcImg, trackBox, trackBoxColor, 2, CV_AA); //
			}

		}

        if (selectObject && selection.width > 0 && selection.height > 0)
        {
            Mat roi(srcImg, selection);
            bitwise_not(roi, roi);
        }

	}
	else
	{//停止跟踪
		//位置初始化
		//_pinfo->x = -1;
		//_pinfo->y = -1;
	}
}

int CvHelper::cv_revDataHandler(Mat &img)
{
    try
    {
        if(!img.data)
        {
            return -1;
        }
        else
        {
            Mat smallImg;
            double fx = 1 / scale;

            //缩放图像
        #ifdef VERSION_2_4
            resize( img, smallImg, Size(), fx, fx, INTER_LINEAR );
        #else
            resize( img, smallImg, Size(), fx, fx, INTER_LINEAR_EXACT );
        #endif

            _faceDetect(img, smallImg);
            _trackingColor(img, smallImg);
        }
    }
    catch(Exception e)
    {
#ifdef DEBUG
        LOGD("error");
#endif
    }
    return 0;
}

/*********************************************control**********************************************/
void CvHelper::_changeMode(int state)
{
    faceDetectEnable = state;
    trackingEnable = !state;
}

int CvHelper::cv_loadCascade(const char * file)
{
    if(!cascade.load(file))
    {
#ifdef DEBUG
        LOGD("cascade load failed ...");
#endif
        return -1;
    }

    return 0;
}

int CvHelper::cv_faceDetect()
{
    if(cascade.empty())
    {
        return -1;
    }
    else
    {
        _changeMode(FACE_MODE);
    }

    return 0;
}

void CvHelper::cv_trackColor()
{
    _changeMode(COLOR_MODE);
}

void CvHelper::cv_disableTrack()
{
    faceDetectEnable = 1;
    trackingEnable = 1;
}

int CvHelper::cv_setSelection(int enable, int a, int b)
{
    int x =  a / scale, y = b / scale;

    if(enable == 0)
    {
        //开始设置选区
        //a, b为矩形选区起点x, y坐标
#ifdef DEBUG
        LOGI("x: %d, y: %d", a, b); //a, b为起点坐标x, y
#endif
        selectObject = true;
        selection.x = x;
        selection.y = y;
        selection.width = 0;
        selection.height = 0;

    }
    else
    {
        //选区设置完成
        //a, b为矩形选区宽高w, h值
        int w = x - selection.x;
        int h = y - selection.y;
#ifdef DEBUG
        LOGI("w: %d, h: %d", w, h);
#endif
        //选区有效,重新计算直方图
        selectObject = false;

        if(w > 0 && h > 0)
        {
            selection.width = w;
            selection.height = h;

            hsize = 16;
            trackObject = -1;
        }
        else
        {
            return -1;
        }

    }

    return 0;
}





