#### Cmake 语法


> 单行注释

`#我是注释我是注释`

> 多行注释

```
 #[[
 我是注释
 我是注释
 我是注释

]]

 ```
> 变量声明  ps在cmake中，所有变量类型都为string类型

set(<变量名> <变量值>)

`set(name 西贝)`

> 变量引用

${<变量名>}

`${name}`

> 移除变量

unset()

> 列表，列表也是字符串，可以把列表看成一个特殊的变量，这个变量有多个值

set(列表名 值1 值2 ...值N)

set(列表名 值1;值2;...值N)

```
set(list a b c d e)

set(list2 a;b;c;d;e)

```

> 列表引用

${列表名}

`message(${list})`

> 操作符

|类型|名称|
|---|---|
|一元|EXIST,COMMAND,DEFINED|
|二元|EQUAL,LESS,LESS_EQUAL,GREATER,GREATER_EQUAL,STREQUAL,STRLESS,STRLESS_EQUAL,STRGREATER,STRGREATER_EQUAL,VERSION_EQUAL,VERSION_LESS,VERSIIN_LESS_EQUAL,VERSION_GREATER,VERSION_GREATER_EQUAL,MATCHES|
|逻辑|NOT,AND,OR|


> 布尔常量值

|类型|名称|
|---|---|
|true|1,ON,YES,TRUE,Y,非0的值|
|false|0，OFF,NO,FALSE,N,IGNORE,NOTFOUND,空字符串,以-NOTFOUND结尾的字符串|

> 条件命令

if(表达式)

COMMAND(ARGS ...)

elseif(表达式)

COMMAND(ARGS ...)

else(达表示)

COMMAND(ARGS ...)

endif(表达式)

```
set (if_tap OFF)
set(elseif_tap ON)

if(${if_tap})
    message("if")
elseif(${elseif_tap})
    message($elseif_tap)
else(${if_tap})
    message("else")
endif(${if_tap})
```

> 循环命令

while(表达式)

COMMAND(ARGS ...)

endwhile(表达式)

break()可以跳出整个循环

continue()可以跳出当前循环

```
set(a "")

while(NOT a STREQUAL "xxx")
    set(a "${a}x")
    messag（“ a = ${a} ”)
endwhile()

```

> 循环遍历

foreach(循环变量 参数1 参数2 ...参数N)

COMMAND(ARGS ...)

endforcach(循环变量)

每次迭代设置循环变量为参数

foreach也支持break()和continue()命令跳出循环

```
foreach(item 1 2 3 )
message("item = ${item}")
endforeach(item)
```

foreach（循环变量 RANGEtotal）

COMMAND(AGRS...)

endforeach(循环变量)

循环变量范围从0到total

```
set(a -)
foreach(i RANGE 99)

    message(${a})
    set(a ${a}-)
    endforeach()

```

foreach(循环变量 RANGE start stop step)

COMMAND(ARGS ...)

endforeach()

循环范围从start到stop，循环增量为step

```
foreach(i RANGE 2 10 3)
    message(${i})
    endforeach(i)

```
foreach(循环变量 IN LISTS 列表)

COMMAND(AGRS...)

endforeach()

```
set(list a ba la la ha ha )
foreach(i IN LISTS list)
    message(${i})
    endforeach(i)

```

> 自定义函数命令

function(<name> [arg1[arg2[agr3....]])

COMMAND()

endfuncation(<name>)

调用`nanme(arg1 arg2 arg3 ....)`

```
function(xibei q a z)
    message(${q})
    message(${a})
    message(${z})
    message("argc = ${ARGC}") #参数数量
    message("argv = ${ARGV}") #参数列表
    message("arg0 = ${ARGV0}") #参数1
    message("arg1 = ${ARGV1}") #参数2
    message("arg2 = ${ARGV2}") #参数3
    endfunction(xibei)

xibei(3 2 1)

```

> 自定义宏命令

macro(<name> [arg1[arg2[arg3 ...]]])

COMMAND()

endmacro(<name>)

调用：`name(arg1 arg2 arg3)`

```
macro(xibei q a z)
    message(${q})
    message(${a})
    message(${z})
    message(" 我 是 一个 宏 命令")
    endmacro(xibei)

xibei(3 2 1)

```

> 变量作用域

- 全局层

cache变量，在整个项目范围可见，一般在set定义变量时，指定CACHE参数就能定义cache比那辆

- 目录层

在当前目录CMakeLists.txt中定义，以及在该文件包含的其他cmake源文件定义的变量

- 函数层

在命令函数中定义的变量，属于函数作用域内的变量。


**优先级：函数层 > 目录层 > 全局层**
**优先级高的修改变量值，不会影响上层变量值，如果加入scope参数，则修改的是上一层级变量值**











