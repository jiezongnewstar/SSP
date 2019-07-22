#### CMakeLists.txt 文件详解

- `cmake_minimum_required(VERSION 3.4.1)`

    指定cmake最低支持版本,该指令是可选项，但是在工程中使用了指定高版本，则必须使用该指令指定最低版本

- `aux_source_directory(. DIR_SRCS)`
    
    第一个参数.指明当前目录，第二个参数DIR_SRCS接收源文件
    查找当前目录所有源文件，并将源文件名称列表保存到DIR_SRCS变量中，该指令不能查找子目录

> 常用命令

- add_library命令

1. 添加一个库

    `add_library(<name> [STATIC|SHARED|MODULE] [EXCLUED_FROM_ALL] source1 source2 ... sourceN)`

       参数1: 添加库文件名。
       参数2: 指定库的类型。STATIC-静态库、SHARED-动态库、MODULE-在使用dyld的系统有效，若不支持dyld则等同于SHARED。
       参数3: 表示该库不会被默认构建。
       参数4: 用来指定库的源文件

2. 导入预编译库

    `add_library(<name> [SHARDE|STATIC|UNKONW] IMPORTED)`

    该命令同行与`set_target_properties(test SHARED IMPORTED)`一起使用

    示例：导入一个名为test的预编译动态库

    ```
        add_library(test SHARED IMPORTED)

        set_target_properties(
            test                                #指明目标库名
            PROPERTIES IMPORTED_LOCATION        #指明要设置的参数
            <库路径>/${ANDROID_ABI}/libtest.so   #指明导入的库的路径
        )

    ```

- set命令

    1. 设置可执行文件的输出路径(EXCUTEABLE_OUTPUT_PATH是全局变量)
    
        `set(EXCUTEABLE_OUTPUT_PATH [output_path])`
    2. 设置库文件的输出路径(LIBRARY_OUTPUT_PATH是全局变量)

        `set(LIBRARY_OUTPUT_PATH [output_path])`

    3. 设置C++编译参数(CMAKE_CXX_FLAGS是全局变量)

        `set(CMAKE_CXX_FLAGS "-Wall std=c++11")`

    4. 设置源文件集合(SOURCE_FILES是本地变量即自定义变量)
    
        `set(SOURCE_FILES main.cpp test.cpp ...)`

- include_directories命令

    用来设置头文件目录，相当于g++中的 `-I`命令

    `include_directories(./include ${MY_INCLUDE})`

    `./include 可以是相对路径或者绝对路径及自定义变量值`

- add_executable命令

    添加可执行文件

    `add_executable(<name> ${SRC_LIST})`

    参数1：文件名
    
    参数2：源文件

- target_link_livraries命令

    将若干库链接到目标库文件

    链接的顺序应当复合gcc链接顺序规则，被链接的库放在依赖它的库的后面，即如果上面的命令中，lib1依赖于lib2，lib2又依赖于lib3，则在上面命令中必须严格按照lib1 lib2 lib3的顺序排列，，否则会报错


    `target_link_libraries(<name> lib1 lib2 lib3)`

    ```
        #如果出现相互依赖的静态库，Cmake会允许依赖图中包含循环依赖，如：

        add_library(A STATIC a.c)
        add_library(B STATIC b.c)
        target_link_libraries(A B)
        target_link_libraries(B A)
        add_executable(main main.c)
        target_link_libraries(main A)

    ```

- add_definitions命令

    为当前路径以及子目录的源文件加入-D引入defin flag 等编译参数

    `add_definitions(-DF00 -DDEBUG ...)`

- add_subdirectory命令

    用于添加子目录的CMake源文件，如果当前目录下还有子目录时可以使用add_subdirectory,子目录中也需要包含CMakeLists.txt

    `add_subdirectory(sub_dir [binary_dir])`

    参数1: 指定包含CMakeLists.txt和源码文件的子目录位置
    参数2: 指定输出路径，一般可以不指定

- file命令

    文件操作命令

    1. 将message写入filename文件中，会覆盖文件原有内容

        `file(WRITE filename "message")`

    2. 将message写入filename文件中，会追加在文件末尾

        `file(APPEND filename "message")`

    3. 从filename文件中读取内容并存储到var变量中，如果指定了numBytes和offset则从offset出开始最多读numBytes个字节，另外如果指定了HEX参数，则内容会以十六进制形式存储在var变量中

        `file(READ filename var [LIMIT numberBytes] [OFFSET offset [HEX]])`

    4. 重命名文件

        `file(RENAME <oldname> <newname> )`

    5. 删除文件，等同于rm命令

        `file(REMOVE [file1 ... ])`

    6. 递归的执行删除文件命令，等于rm -r
    
        `file(REMOVE_RECURSE [file1 ...])`
    
    7. 根据指定url下载文件,timeout超时时间；下载的状态会保存到status中；下载的日志会被保存到log；sum指定所下载文件预期的MD5值，如果指定会自动进行比对，如果不一致，则返回一个错误；SHOW_PROGRESS，进度信息会以状态信息的形式被打印出来

        `file(DOWNLOAD url file [TIMEOUT timeout] [STATUS status] [LOG log] [EXPECTED_MD5 sum] [SHOW_PROGRESS])`

    8. 创建目录

        `file(MAKE_DIRECTORY [dir1 dir2 ...])`

    9. 会把path转换为以unix的开头cmake风格路径，保存在result中

        `file(TO_MAKE_PATH path result)`

    10. 会把cmake风格的路径转换为本地路径风格： windows下用”\“,而unix下用”/“

        `file(TO_MATIVE_PATH path result)`

    11. 将会为所有匹配查询表达式的文件生成一个文件list，并将该list存储进变量variable里，如果一个表达式指定了RELATIVE,返回的结果将会是响度与给定路径的相对路径，查询表达式例子 ：` *.cxx,*.vt? `NOTE 按官方文档的说法，不建议使用file的GLOB指令来收集工程的源文件  **这个是最常用的**

        `file(GLOB variable [RELATIVE path] [globbing expressions] ...)`

        [PS:注意！使用这种方式来指定源文件，如果后面项目需要增加源文件，如果直接增加新的源文件，执行会报错，因为CMakeLists.txt没有改变，所有需要在CMakeLists文件中增加空格或换行，让CMakeLists重新编译]

- set_directory_properties命令

    设置某一个路径的一种属性

    `set_directory_properties(PROPERTIES prop1 value1 prop2 value2)`

    参数1、参数2代表属性，取值为：

    INCLUDE_DIRECTORIES

    LINK_DIRECTORIES

    INCLUDE_REGULAR_EXPRESSION

    ADDITIONAL_MAKE_CLEAN_FILES

- set_property命令

    在给定的作用域内设置一个命名属性

    `set_property(<GLOBAL| DIRECTORY [dir]| TARGET [target ...] |SOURCE [src1 ...] |TEST [test1...] |CACHE [entry1 ...] |[APPEND] PROPERTY <name> [values ...])`

   PROPERTY参数是必须的

   参数1：决定了属性可以影响的作用域：

   GLOBAL：全局作用域

   DIRECTORY：默认当前路径，也可以用[dir]指定路径

   TARGET：目标作用域，可以是0个或多个已有目标

   SOURCE：源文件作用域，可以是0个或多个源文件

   TEST：测试作用域，可以是0个或多个已有的测试

   CACHE：必须指定0个或多个cache中已有的条目

   
> 多个源文件处理

        如果有多个源文件，把所有的文件一个个加入很麻烦，可以使用 aux_source_directory 命令或 file命令,会查找指定目录下的所有源文件，然后将结果存进指定变量名。

        1. 查找当前目录所有源文件并保存到DIR_SRC变量变量，不能查找子目录
            aux_source_directory(. DIE_SRCS)
        2. 可以使用file(GLOB DIR_SRCS *c *.cpp)

        最终: add_library(native-lib SHARED ${DIR_SRCS})

> 多目录多个源文件处理

    1. 主目录中的CMakeLists.txt中添加add_subdirectory(child)命令，指明本项目包含一个子项目child。并在target_link_libraries指明本项目需要链接一个名为child库。
    2. 子目录child中创建CMakeLists.txt,这里child编译为共享库。

    ```
        cmake_minimum_required(VERSION 3.4.1)

        aux_source_directory(. DIR_SRCS)

        #添加child子目录下的cmakelists
        add_subdirectory(child)

        add_library(native-lib
                    SHARED
                    ${DIR_SRCS})

        target_link_libraries(native-lib child)

        -------------------------------------

        #child目录下的CMakeLists.txt:
        cmake_minimum_directory(VERSION 3.4.1)
        aux_source_directory(. DIR_LIB_SRCS)
        add_library(child
                    SHARED
                    ${DIR_LIB_SRCS})

    ```

> 添加预编译库

- Android6.0版本以前

    - 假设我们本地项目引用了libmported-lib.so
    - 添加add_library命令，第一个参数是模块名，第二个参数SHARED表示动态库，STATIC表示静态库，第三个参数IMPORTED表示以导入的形式添加。
    - 添加set_target_prpperties命令设置导入路径属性
    - 将import-lib添加到target_link_libraries，命令参数中，表示native-lib需要链接imported-lib模块

        ```
        cmake_minimum_directory(VERSION 3.4.1)
        #使用IMPORTED标志告知CMake只希望将库导入到项目中
        #如果是静态库则将SHARED改为STATIC
        add_library(import-lib
                    SHARED
                    IMPORTED)
        #参数分别为：库、属性、导入地址、库所在地址
        set_target_properties(
                            improt-lib
                            PORPERTIES
                            IMPORTED_LOCSTION
                            <路径>/libimported-lib.so)
        aux_source_directory(. DIR_SRCS)
        add_library(
                    native-lib
                    SHARED
                    ${DIR_SRCS})
        
        target_link_libraries(native-lib imported-lib)

        ```
- Android6.0版本以后

    - 在Android6.0以及以上版本，如果使用上面的方法添加预编译动态库的话，会有问题。我们可以使用另外一种方式来配置。

        ```
            #set命令定义一个变量
            #CMAKE_C_FLAGS: c的参数，会传递给编译器
            #如果是C++文件，需要用CMAKE_CXX_FLAGS
            # -L: 库的查找路径
            set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -L[so所在的目录]")

        ```
> 添加头文件目录

- 为了确保CMake可以在编译时定位头文件，使用include_directories,相当于g++选项中的 -I 参数。 这样就可以使用#include<xx.h>,否则需要使用#include ”path/xx.h“

    ```
        cmake_minimum_required(VERSION 3.4.1)
        #设置头文件目录
        include_directories(<文件目录>)
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -L[so所在目录]")
        aux_source_directory(. DIR_SRCS)
        add_library(
                    native-lib
                    SHARED
                    ${DIR_SRCS})
        target_link_libraries(native-lib imported-lib)

    ```

> Build.gradle配置

- 还可以在gradle中使用arguments设置了一些配置

    ```
        android{
            defaultConfig{
                externalNativeBuild{
                camke{
                    //使用的编译器是clang/gcc
                    //cmake默认就是gnustl_static
                    arguments "-DANDROID_TOOLCHAIN=clang",
                    "-DANDROID_STL=gnustl_static"
                    //指定cflags和cppflags,效果和cmakelists使用一样
                    cFlags ""
                    cppFlags ""
                    //指定abiFilters "armeabi -v7a"

                }

            }
            
            }

           externalNativeBuild{
               cmake{
                   //指定CMakeLists.txt文件相对于当前Build.gradle的路径
                   path "xxx/CMakeLists.txt"
               }

           } 

        }

    ```

> Android Studio 中的CmakeLists文件

    ```
        #指定cmake最小支持版本
        cmake_minimum_required(VERSION 3.4.1)

        #添加一个库，根据native-lib.cpp源文件编译一个native-lib的动态库
        add_library(
                native-lib
                SHARED
                native-lib.cpp)

        #查找系统库，这里查找的是系统日志库，并赋值给变量log-lib
        find_library(
                log-lib
                log)


        target_link_libraries(
                native-lib
                ${log-lib})


    ```


















    
    