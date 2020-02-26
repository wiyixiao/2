#include "common.h"
#include <iostream>
#include "cv.h"

CvHelper Cv;

extern "C" {

    //获取版本号
    JNIEXPORT jstring JNICALL Java_android_1opencv_1api_CvHelper_getVersion(
            JNIEnv* env,
            jobject ) {
        std::string ver = Cv.cv_getVersion();
        return env->NewStringUTF(ver.c_str());
    }

    //初始化分类器
    JNIEXPORT jint JNICALL Java_android_1opencv_1api_CvHelper_loadCascade(
            JNIEnv  *jenv,
            jobject /* this */,
            jstring cascadeFileName
    ){
        const char* cascade_file_name = jenv->GetStringUTFChars(cascadeFileName, NULL);
        return Cv.cv_loadCascade(cascade_file_name);
    }

    //图像处理
    JNIEXPORT int JNICALL Java_android_1opencv_1api_CvHelper_revDataHandler(
        JNIEnv  /**env*/,
        jobject /* this */,
        jlong addrInputRgbaImage
        /*jobject classobj*/
        ){
        //处理图片
        return Cv.cv_revDataHandler(*(Mat*)addrInputRgbaImage);

        //获取数据
        /*
        jclass objectClass = env->FindClass("com/example/robottime/utils/CvHelper/ObjInfo");
        jfieldID cx = env->GetFieldID(objectClass, "cx", "I");
        jfieldID cy = env->GetFieldID(objectClass, "cy", "I");
        jfieldID face_num = env->GetFieldID(objectClass, "face_num", "I");

        env->SetIntField(classobj, cx, Cv.objinfo.cx);
        env->SetIntField(classobj, cy, Cv.objinfo.cy);
        env->SetIntField(classobj, face_num, Cv.objinfo.face_num);
        */

    }

    //获取信息
    JNIEXPORT jint JNICALL Java_android_1opencv_1api_CvHelper_getCx(
        JNIEnv  /* *env */,
        jobject /* this */
        ){
        return Cv.objinfo.cx;
    }
    JNIEXPORT jint JNICALL Java_android_1opencv_1api_CvHelper_getCy(
        JNIEnv  /* *env */,
        jobject /* this */
        ){
        return Cv.objinfo.cy;
    }
    JNIEXPORT jint JNICALL Java_android_1opencv_1api_CvHelper_getCd(
        JNIEnv  /* *env */,
        jobject /* this */
        ){
        return Cv.objinfo.cd;
    }
    JNIEXPORT jint JNICALL Java_android_1opencv_1api_CvHelper_getFaces(
        JNIEnv  /* *env */,
        jobject /* this */
        ){
        return Cv.objinfo.cy;
    }

    //设置hsv
    JNIEXPORT void JNICALL Java_android_1opencv_1api_CvHelper_setHsv(
        JNIEnv  /**env*/,
        jobject /* this */,
        jint _vmin,
        jint _vmax,
        jint _smin
        ){

#ifdef DEBUG
        LOGI("vmin: %d, vmax: %d, smin: %d", _vmin, _vmax, _smin);
#endif
        Cv.hsvinfo.vmin = _vmin;
        Cv.hsvinfo.vmax = _vmax;
        Cv.hsvinfo.smin = _smin;
    }

    //设置scale
    JNIEXPORT void JNICALL Java_android_1opencv_1api_CvHelper_setScale(
        JNIEnv  /**env*/,
        jobject /* this */,
        jdouble _scale
        ){
        Cv.scale = _scale >= 1 ? _scale : 1;
#ifdef DEBUG
        LOGI("scale: %f", Cv.scale);
#endif
    }

    //设置section选区
    JNIEXPORT jint JNICALL Java_android_1opencv_1api_CvHelper_setSelection(
        JNIEnv  /**env*/,
        jobject /* this */,
        jint _enable,
        jint _a,
        jint _b
        ){

        return Cv.cv_setSelection(_enable, _a, _b);
    }

    //人脸跟踪模式
    JNIEXPORT jint JNICALL Java_android_1opencv_1api_CvHelper_faceDetect(
        JNIEnv  /* *env */,
        jobject /* this */
        ){
#ifdef DEBUG
        LOGI("&*********************FACE-MODE********************&");
#endif
        return Cv.cv_faceDetect();
    }

    //颜色跟踪模式
    JNIEXPORT void JNICALL Java_android_1opencv_1api_CvHelper_trackColor(
        JNIEnv  /* *env */,
        jobject /* this */
        ){
#ifdef DEBUG
        LOGI("&*********************COLOR-MODE********************&");
#endif
        Cv.cv_trackColor();
    }

    //停止跟踪
    JNIEXPORT void JNICALL Java_android_1opencv_1api_CvHelper_disableTrack(
        JNIEnv  /* *env */,
        jobject /* this */
        ){
        Cv.cv_disableTrack();
    }

}
