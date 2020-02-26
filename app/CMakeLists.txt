
# 参考：
# https://blog.csdn.net/wilson1068/article/details/87656518
# 请注意，修改本地的 OPENCV_SDK_PATH （OpenCV-android-sdk 路径）

cmake_minimum_required(VERSION 3.4.1)

set(OpenCV_STATIC ON)

# Sets the minimum version of CMake required to build the native library.
#set(OPENCV_SDK_PATH D:/Code/OpenCV-android-sdk)#设置OpenCv的路径变量
set(OPENCV_SDK_PATH  ${CMAKE_SOURCE_DIR}/src/main/cpp/opencv_src)#设置OpenCv的路径变量

# 输出路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

# 配置加载native依赖
include_directories(${OPENCV_SDK_PATH}/sdk/native/jni/include/
                    ${OPENCV_SDK_PATH}/sdk/native/jni/include/opencv2/
                    ${CMAKE_SOURCE_DIR}/src/main/cpp/
                    )


set(CMAKE_VERBOSE_MAKEFILE on)


set(OPENCV_DYNAMIC_LIBS "${OPENCV_SDK_PATH}/sdk/native/libs")         # 动态 opencv native 库路径
set(OPENCV_STATIC_LIBS "${OPENCV_SDK_PATH}/sdk/native/staticlibs")    # 静态 opencv native 库路径
set(OPENCV_3RD_STATIC_LIBS "${OPENCV_SDK_PATH}/sdk/native/3rdparty/libs")    # 第三方依赖库

set(OPENCV_ANDROID_ABI "${ANDROID_ABI}")


link_directories("${OPENCV_DYNAMIC_LIBS}/${OPENCV_ANDROID_ABI}/")
link_directories("${OPENCV_STATIC_LIBS}/${OPENCV_ANDROID_ABI}/")
link_directories("${OPENCV_3RD_STATIC_LIBS}/${OPENCV_ANDROID_ABI}/")

# 添加库
add_library(libopencv_java4 SHARED IMPORTED )
set_target_properties(libopencv_java4 PROPERTIES
        IMPORTED_LOCATION "${OPENCV_DYNAMIC_LIBS}/${OPENCV_ANDROID_ABI}/libopencv_java4.so")

#native-lib
file(GLOB_RECURSE CPP_SRCS "src/main/cpp/*.cpp")
add_library( native-lib SHARED ${CPP_SRCS} )

#SerialPort
#file(GLOB_RECURSE C_SRCS "src/main/cpp/*.c")
#add_library( SerialPort SHARED ${C_SRCS})


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.

                        native-lib

                        #SerialPort

                        libopencv_java4

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

ADD_SUBDIRECTORY(${PROJECT_SOURCE_DIR}/src/main/cpp/SerialPort)
