### shell 语法变量定义到使用

- [Repository : 代码传送门](https://github.com/jiezongnewstar/shell "传送门")

> 介绍：【自动化编程】shell 语法运行在 linux 上，对于 android 开发来说，用来编译底层库、第三方库，而这些库并不是由 java 语言开发的，例如 ffmpeg 等，所以我们需要用通过 shell 语法来进行编译，并将他们打包到 apk 中。
> shell 是个命令集合，可以来解决一些重复性工作，广泛存在于 linux、windows、android。也多用于运维人员。

**_这里我们要注意，shell 是运行在 linux 环境中的！！！！，文件通常以.ssh /.sh 结尾。类比于 java 中 .java_**

---

> 环境搭建

1. 安装 ubantu 虚拟环境
2. 阿里云服务器

- 指令介绍

**_在编写 shell 脚本前需要注意，需要拥有在 linux 中执行的条件，我们要在顶层声明:_**

`#!/bin/bash`

- 运行

  1. sh xxx.sh
  2. ./xxx.sh （赋予运行权限 chomd 777 xxx.sh）
  3. /bin/bash xxx.sh

- 在 shell 中用，除了第一行引入 bin bash 环境之外，其他情况 #来作为注释标记

- 变量声明，A=10 注意赋值过程不能有空格， A=10 声明的是局部变量，除了声明的局部变量之外，系统内部提供了一些环境变量，可以直接调用，如 PWD,打印当前文件地址

- 常用系统命令

\$0 当前程序的名称

\$n 程序的输入参数 n=1 第一个参数 n=2 第二个参数 1..n

\$\* 所有输入参数

\$# 输入参数个数

\$? 命令执行的状态，通常返回 0 代表成功，也可以自定义返回值

seq: 系统循环 例`seq 1 15`

expr 1 + 2 :计算 1+2 的和并输出

tar czf all.tgz \* : 打包所有文件，输入结果以 all 命名

find . -name "\*.sh": 查找当前目录指定类型文件

read line :读取命令 将输入值赋给 line 变量，取值从 line 中取

cat : 读取命令

mkdir -p [指定目录]: 指定路径下创建目录

unzip :解压指定压缩文件

- shell 填写输入参数是在 执行命令后 以空格分开传入
  `./echo.sh xibei1 xibei2`

结果

```
xibei1
xibei2

```

- 取系统操作运行结果 通过反引号来实现 “ `` ”
- 循环语句 - for in

```
for 变量 in 字符串
do
    语句1
done

```

- 循环语句 - for i

```
for (( i = 0; i < 10; i++ ))
do

done

```

- 循环语句 - while

1.

```
while 条件语句（算数逻辑  > 、<、>=、<= 、==、!=）
do
    语句
done

```

2.

```
while 条件语句(命令模式 -gt、-lt、-ge、-le、-eq、-ne)
do
    语句
done

```

> 语法详解

- if 语句

1. if(表达式)
   fi

2. if(表达式);then

语句

else

语句

fi

**_注意:【语句】部分必须加[Tab]键，否则语法错误，[空格]按键不能算变写，在 shell 中代表分割_**

**_注意:【表达式】部分与 while 循环中一样，可以是(())也可以是[[]]_**

- 逻辑运算符
  -f 判断文件是否存在

-d 判断目录是否存在

- 运算符

| 算数运算符 |  说明  |                          举例 a = 20 b=10 |
| :--------- | :----: | ----------------------------------------: |
| +          |  加法  |                 `expr $a + $b`，结果为 30 |
| -          |  减法  |                 `expr $a - $b`，结果为 10 |
| \*         |  乘法  |               `expr $a \* $b`，结果为 200 |
| /          |  除法  |                  `expr $a / $b`，结果为 2 |
| %          |  取余  |                  `expr $a % $b`，结果为 0 |
| =          |  赋值  |                 `a=$b`把变量 b 的值赋给 a |
| ==         |  相等  |     相等。用于比较两个数字，相同返回 true |
| !=         | 不相等 | 不相等。用于比较两个数字，不相同返回 true |

---

| 逻辑运算符 |  说明  |                                          举例 |
| :--------- | :----: | --------------------------------------------: |
| !          | 非运算 |                                      取反操作 |
| -o         | 或运算 |                        一个为 true 则结果为真 |
| -a         | 与运算 | 一个为 false 则为 false,全部为 true 则为 true |

---

| 字符串运算符 |                   说明                   |      举例 a=aaa b=bbb |
| :----------- | :--------------------------------------: | --------------------: |
| =            |  判断两个字符串是否相等，相等返回 true   | [ $a = $b ]返回 false |
| !=           | 判断两个字符串是否相等，不相等返回 true  | [ $a != $b ]返回 true |
| -z           |  判断字符串长度是否为 0，为 0 返回 true  |   [ -z $a ]返回 false |
| -n           | 判断字符串长度是否为 0，不为 0 返回 true |    [ -n $a ]返回 true |
| \$           |   哦按段字符串是否为空,不为空返回 true   |       [ $a ]返回 true |

---

| 文件测试运算符 |                                  说明                                  |    举例 file |
| :------------- | :--------------------------------------------------------------------: | -----------: |
| -b             |                判断文件是否是设备文件,如果是则返回 true                | [ -b $file ] |
| -c             |              判断文件是否是字符设备文件,如果是则返回 true              | [ -c $file ] |
| -d             |                  判断文件是否是目录,如果是则返回 true                  | [ -d $file ] |
| -f             | 判断文件是否是普通文件(既不是没目录，也不是设备文件),如果是则返回 true | [ -f $file ] |
| -g             |              判断文件是否设置了 SGID 位,如果是则返回 true              |  [-g $file ] |
| -k             |         判断文件是否设置了粘贴位(Sticky Bit),如果是则返回 true         | [ -k $file ] |
| -p             |                 判断文件是否有名管道,如果是则返回 true                 | [ -p $file ] |
| -u             |              判断文件是否设置了 SUID 位,如果是则返回 true              | [ -u $file ] |
| -r             |                  判断文件是否是可读,如果是则返回 true                  | [ -r $file ] |
| -w             |                  判断文件是否是可写,如果是则返回 true                  | [ -w $file ] |
| -x             |                  判断文件是否可执行,如果是则返回 true                  | [ -x $file ] |
| -s             |                   判断文件是否为空,如果是则返回 true                   | [ -s $file ] |
| -e             |              判断文件(包括目录)是否存在,如果是则返回 true              | [ -s $file ] |

**_注意：条件表达式要放在方括号之间，并且要有空格，例如[$a==$b]是错误的，必须写成[ $a==$b ]。_**

- 算数运算

1. 使用\$(())
2. 使用\$[]
3. 使用 let 命令
4. 使用 expr 外部程式(脚本方式 建议用此方法)

- 重定向

在 linux 中，输出的地方分为两个，一个是显示器，另一个是文件。我们可以通过显示器输入，或者输出，也可以通过文件输出。这里说到的输出的地方我们称作 **描述符**，并通过数字的方式来定义：

1. 标准输入 standard input 0（默认设备键盘）
2. 标准输出 standard output1 (默认设备显示器)
3. 错误输出 standard output2 (默认设备显示器)

##### 输入重定向 `>`

##### 输出重定向 `<`

这里告诉大家一个好的理解方法，箭头指向的就是目标位置

- 函数及方法参数传递

1. 函数的定义

第一种

```
name()

{
    command1;
    command2;
}

```

第二种

```
[function] funName[()]

{
    action;
    [return int;]

}

```

**_注意：shell 函数执行是由上到下，所以先要定义好函数之后再进行函数调用_**

> 这里补充一下， shell 函数的参数 需要在调用的时候 用空格 + 参数的形式传入，参数取值是通过 \$1 等数字下标来取，根据参数数量来决定的[传送门-func.sh]

> 参数返回值需要通过将函数执行结果赋给变量，然后 echo 变量[传送门-func.sh]

> 读取屏幕输入参数需要 使用 read 命令来实现，详情见顶部 [传送门-func.sh]
