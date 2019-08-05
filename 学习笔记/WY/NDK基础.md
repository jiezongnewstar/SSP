### NDK 基础

    Android Native Development Kit(NDK)是一系列的开发工具，允许程序开发人员在Android应用程序中嵌入C/C++语言编写代码。你可以使用它去构建自己的源码。或者利用现有的库。


#### 思考为什么要使用C/C++代码？

    1. 历史遗留代码，这部分代码是用C或者C++写的
    2. 性能，有一些在java上性能达不到要求，则需要用C/C++
    3. 跨平台
    4. 特殊能力 （语音延迟）

#### NDK环境

    Android NDK 编译环境支持Windows、macOS、Linux等平台。

    各平台大同小异，主要需要以下组件：

    - Java JDK
    - Android SDK
    - Android Studio
    - Android NDK 

#### NDK下载

    Google 官网
    Android Studio


#### 三种NDK项目构建方式

    1. AndroidMK
        Android.mk

            LOCAL_PATH :=$(call -my-dir)  #获取Android.mk文件当前路径
            include $(CLEAR_VARS)         #清除“LOCAL开头的配置，LOCAL_PATH除外”
            LOCAL_MODULE := hello-jni     #指定源码编译后的文件名
            LOCAL_SRC_FILES := src/main/cpp/hello-jni.cpp   #需要编译的文件
            include $(BUILD_SHARED_LIBRARY)     #编译动态库



        Application.mk
        将cpp文件、Android.mk 和Application.mk文件放在同一目录，通过命令行ndk-build来编译
    -------------------------------------------------------------
    2. Cmake

        CmakeLists.txt

        bulid.gradle:
            android{
                
                defaultConfig{
                    camke{
                        cppFLags ""
                    }
                }

                externalNativeBuild{
                    cmake{
                        path "CmakeLists.txt"
                    }
                }

            }

     -------------------------------------------------------------
    3. NDKBuild

        Android.mk 
        -------------------------------------------------------------
        Application.mk

        build.gradle:
            android{
                defaultConfig{
                    externalNativeBuild{
                        ndkBuild{
                            arguments "NDK_APPLICATION_MK:=Application.mk"
                        }

                    }
                }

                externalNativeBuild{
                    ndkBuild{
                        path 'Android.mk'
                    }

                }

            }

        -------------------------------------------------------------
        localproperties.gradle

#### Android.mk 构建共享库（动态库）

    构建多个共享库

    LOCAL_PATH :=$(call -my-dir)
    include $(CLEAR_VARS)
    LOCAL_MODULE :=lib1
    LOCAL_SRC_FILES :=lib1.c
    include $(BUILD_SHARED_LIBRARY)

    include $(CLEAR_VARS)
    LOCAL_NODULE := lib2
    LOCAL_SRC_FILES := lib2.c
    include $(BUILD_SHARED_LIBRARY)


#### Android.mk 构建静态库

    LOCAL_PATH :=$(call my-dir)
    #
    #第三方JSON库
    #

    include $(CLEAR_VARS)
    LOCAL_MOUDLE := json
    LOCAL_SRC_FILES := json.c
    include $(BUILD_STATIC_LIBRARY)

    #
    # 动态库
    #

    include $(CLEAR_VARS)
    LOCAL_MOIDLE := module
    LOCAL_SRC_FILES :=module.c
    LOCAL_STATIC_LIBRARIES :=json
    includ $(BUILD_SHARED_LIBRARY)

#### Android.mk 用共享库共享通用模块

    LOCAL_PATH :=$(call my-dir)

    #
    # 第三方 JSON 库
    #
    include $(CLEAR_VARS)
    LOCAL_MOUDLE := json
    LOCAL_SRC_FILES := json.c
    includ $(BUILD_SHARED_LIBRARY)

    #
    # 动态库1
    #
    include $(CLEAR_VARS)
    LOCAL_MOIDLE := module1
    LOCAL_SRC_FILES :=module1.c
    LOCAL_SHARED_LIBRARIES :=json
    includ $(BUILD_SHARED_LIBRARY)

    #
    # 动态库2
    #
    include $(CLEAR_VARS)
    LOCAL_MOIDLE := module2
    LOCAL_SRC_FILES :=module2.c
    LOCAL_SHARED_LIBRARIES :=json
    includ $(BUILD_SHARED_LIBRARY)

#### Android.mk 使用Prebuilt库

    使用预编译库来加速构建过程
    不发布源码将模块共享给他人

    LOCAL_PATH := $(call my-dir)
    #
    # 第三方预构建 Json 库
    #
    include $(CLEAR_VARS)
    LOCAL_MODULE := json
    LOCAL_SRC_FILES := libjson.so /libjson.a
    include $(PREBUILT_SHARED_LIBRARY) / include $(PREBUILT_SHARED_LIBRARY)

    #
    #  动态库
    #
    include $(CLEAR_VARS)
    LOCAL_MODLUE :=module
    LOCAL_SRC_FILES := module.c
    LOCAL_SHARED_LIBRARIES := json
    include $(BUILD_SHARED_LIBRARY)


#### Android.md 系统构建

    其他构建系统变量

    - TARGET_ARCH:目标 CPU 体系结构的名称，例如arm
    - TARGET_PLATFORM: 目标Android 平台的名称，例如：android-v22
    - TARGET_ARCH_ABI: 目标CPU体系结构和ABI的名称，例如armeabi-v7a
    - TARGET_ABI: 目标平台ABI的串联，例如：android-22-armeabi-v7a
    - LOCAL_MODULE_FILENAME: 可选变量，用来重新定义生成的输出文件名
    - LOCAL_CPP_EXTENSION: C++源文件的默认扩展名是.cpp.这个变量可以用来为C++代码指定一个或多个文件扩展名。
    - LOCAL_CPP_FEATURES: 可选变量，用来指明模块所依赖的具体 C++特性，如RTTI、exceptions等。
    - LOCAL_C_INCLUDES: 可选目录列表，NDK安装目录的相对路径，用来搜索头文件
    - LOCAL_CFLAGS: 一组可选的编译器标志，在编译C和C++源文件的时候会被传给编译器
    - LOCAL_CPP_FLAGS: 一组可选的编译标志，在只编译C++源文件时被传递给编译器
    - LOCAL_SHOLE_STATIC_LIBRARIES:LOCAL_STATIC_LIBRARIES的变体，用来指明应该被包含在生成共享库宏的所有静态内容。
    - LOCAL_ARM_MODE:可选参数，ARM机器体系结构特有变量，用于指定要生成的ARM二进制类型。

#### Android.mk 定义新变量

    开发人员可以定义其他变量来简化他们的构建文件。以LOCAL_和NDK_前缀开头的名称预留给Android NDK构建系统使用。

    MY_SRC_FILES := src/main/cpp/hello-jin.cpp
    LOCAL_SRC_FILES := $(MY_SRC_FILES)

#### Application.mk

    Application.mk是Android NDK 构建系统使用的一个可选构建文件。
    




