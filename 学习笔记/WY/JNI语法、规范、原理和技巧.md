#### JNI 与 Native Code 通信
    JNI 是 Java Native Interface 的缩写，它提供了若干的API实现了Java和其他语言的通信

### Java代码调用原生方法

    ```
        @Override
        protected void onCreate(Bundle saveInstanceState){
            TextView textview = new TextView(this);
            textview.setText(stringFromJNI());
            setContentView(textView);
        }

    ```
#### 声明原生方法

    ```
        //原生方法由’hello_jni‘原生库实现，该原生库与应用程序一起打包
        public native String stringFromJNI();

    ```
#### 加载共享库

    ```
        /**
        *这段代码作用于在应用启动时加载’hello_jni'共享库
        *该库在应用程序安装时由包管理器解压到/data/data/com/xxx.xxx/hello.jin/lib/libhello_jni.so中
        */

        static{
                System.loadLibrary("hello_jin");
        }

    ```
#### 实现原生方法

    ```
        #include<jni.h>
        #include<string>
        extern "C"
        JNIEXPORT jstring JNICALL
        Java_com_xibei_xxx_hellojni_HelloJni_stringFromJNI(
            JNIEnv* env,
            jobject){
                std::string hello = "Hello from C++";
                return env->NewStringUTF(hello.c_str());
            }

    ```

    c/c++头文件生成器：javah
    在命令行方式下运行
    javah -d jni -classpath <SDK_android.jar>;<APP_classes> <class>

    在Android Studio 中生成头文件

    Tools -> External Tools -> 点击”+“添加：
    Name : javah
    Descrption: JNI Tool
    ☑️Synchronize files after execution     ☑️Open console
    ☑️Main menu     ☑️Editor menu   ☑️Project views     ☑️Search results
    
    Program： javah
    Parameters： -v -jni -d $ModuleFileDir$/src/main/jni $FileClass$
    Working directory: $SourcepathEntry$

#### 头文件解读

- JINEnv 结构指针

    原生代码通过JNIEnv接口指针提供的各种函数来使用虚拟机功能
- 实例方法与静态方法

#### JIN 语法规范

- 数据类型


    - JNI系统类型 ：JNIEnv（当前线程上下文）
    - 数据类型映射关系
    
        |java类型|Jni.h |
        | :--------- | :----: | 
        |boolean|jboolean|
        |btye|jbyte|
        |char|jchar|
        |short|jshort|
        |int|jint|
        |long|jlong|
        |float|jfloat|
        |double|jdoulbe|
        |String|jstring|
        |Object|jobject|
        |Class|jcalss|
    - 数组类型
       
        |java类型|Jni.h |
        | :--------- | :---- | 
        |boolean[]|jbooleanArray|
        |btye[]|jbyteArray|
        |char[]|jcharArray|
        |short[]|jshortArray|
        |int[]|jintArray|
        |long[]|jlongArray|
        |float[]|jfloatArray|
        |double[]|jdoulbeArray|
        |String[]|jobjectArray|
        |Object[]|jobjectArray|
        |Class[]|jcalssArray| 



    - java

    ```
        public native int listPrimitiveType(int i,float f,doulbe d,long l);

        public native String listReferenceType(String str,Object activity);

        public native String[] listArray(int[] i,String[] jstr,Object[] objects);

        public native Class[] listClassArray(Class c);


    ```

    - JNI

    ```
        JNIEXPORT jint JINCALL Java_com_xibei_xxx_JNIDemo_listPrimitiveType(JNIEnv *,jobject,jint,jfloat,jdouble,jlong);

        JNIEXPORT jstring JINCALL Java_com_xibei_xxx_JNIDemo_listReferenceType(JNIEnv *,jobject,jstring,jobject);

        JNIEXPORT jobjectArray JINCALL Java_com_xibei_xxx_JNIDemo_listArray(JNIEnv *,jobject,jintArray,jobjectArray,jobjectArray)

        JNIEXPORT jobjectArray JNICALL Java_com_xibei_xxx_JNIDemo_listClassArray(JNIEnv *,jobject,jclass)

    ```

#### JNI中基本数据类型使用
- 主要类型使用
    ```
        typedef unsigned char       jboolean;
        typedef signed char         jbyte;
        typedef unsigned short      jchar;
        typedef short               jshort;
        typedef int                 jint;
        typedef long long           jlong;
        typedef float               jfloat;
        typedef double              jdouble;   

    ```
- jstring使用
    jstring 不能直接当做字符串来使用

    `typedef jobject    jstring;`

    `typedef void* jobject;`

    从上面得知，jstring其实是jobject类型，而jobject又是一个void数组，下面请看啊jstring的使用：
    - jstring ->char[]

        ```
            const char* charStr = (*env)->GetStringUTFChars(env,str,NULL); //这里注意GetStringUTFChars 会申请一块内存，所以使用完毕要释放内存

            printf("%s",charStr);
            (*env)->ReleaseStringUTFChars(env,str,charStr); //释放内存
        ```
    - char[]->jstring 

        ```
            const char* jniChars = "i am jni chars";
            jstring str = (*env)->NewStringUTF(env,jniChars); //该参数java可以直接用
            return str;

        ```
- jobject的使用
    - JIN内部描述

        |java类型|JNI描述 |
        | :--------- | :---- | 
        |boolean|Z|
        |btye|B|
        |char|C|
        |short|S|
        |int|I|
        |long|L|
        |float|F|
        |double|D|
        |Object|L全路径名称;|
        |type[]|[Type| 
        |method|"方法名",(参数类型)返回值|

        描述实例：

            ```
                public final class Message{
                    public int what;                                【I】
                    public Object obj;                              【Ljava/lang/Object;】 
                    public static Message obtain(){                 【"obtain",()"Landroid/os/Message;"】             

                    }
                    public Message obtain(Handler h,int what){      【"obtain","(Landroid/os/Handler;I)Landroid/os/Message;"】

                    }

                    public Message(){                               【”<init>“,"()V"】

                    }

                }

            ```

    - 对象的创建
  


    - 字段的读写

        - 读写实例字段
        调用 GetObjectClass 得到 jclass(类型信息),

        调用 GetFieldID 得到 jfieldID（字段ID）,

        调用 Set/GetXXXField 
        - 读写静态的字段

        FindClass 得到 jclass（类型信息）,

        调用 GetStaticFieldID 得到 jfieldID（字段ID）,

        调用 Set/GetStaticXXXField

        - 示例：

            ```

                jclass cls = (*env)->GetObjectClass(env,msg);
                jfieldID whatField = (*env)->GetFieldID(env,cls,"what","I");
                (*env)->SetIntField(env,msg,whatField,-1);

                int iWhat = (*env)->GetIntField(env,msg,whatField);

            ```

    - 方法的调用

        - 读取实例方法

        调用 GetObjectClass 得到 jclass,

        调用GetMethodID 得到 jmethodID,

        调用 CallXXXMethod 
        

        - 读取静态方法

        通过FindClass 得到 jclass,

        调用 GetStaticMethodID 得到 jmethodID,

        调用 CallStaticXXXMethod

        - 示例 

            ```
                jclass cls = (*env)->FindClass(env,"android/os/Message");
                jmethodID obtainMethod = (*env)->GetStaticMethodID(env,cls,"obtain","()Landroid/os/Message;")
                return (*env)->CallStaticObjectMethod(env,cls,obtainMethod);

            ```