### Paint详解

- 颜色相关

1. setColor(int color)参数具体的颜色值,16进制数值，0xFFFF0000

2. setARGB(int a,int r,int g,int b) 参数分别透明度，红，绿，蓝。 0 - 255数值

3. setShader(Shader shader)参数着色器对象，一般使用shader的几个子类 

    __LinearGradient: 线性渲染__

    构造方法：`LinearGradient(float x0,float y0,float x1,float y1,int color0,int color1,Shader.TileMode tile)`

    参数：

    x0 y0 x1 y1 :渐变的两个端点位置

    color0 color1 是端点的颜色

    tile: 端点范围之外的着色规则，类型是TileMode

    使用：

    ```
    mShader = new LinearGradient(0,0,500,500,new int[]{Color.RED,Color.BLUE},null,Shader.TileMode.CLAMP);
    mPaint.setShader(mShader);
    canvas.drawCircle(250,250,250,mPaint);

    ```


    
    __RadialGradient: 环形渲染__

    构造方法：`RadialGradient(float centerX,float centerY,float radius,int ceterColor,int edgeColor,TileMode tileMode)`

    参数：

    centerX,centerY:辐射中心的坐标

    radius:辐射半径

    centerColor: 辐射中心的颜色

    edgeClolo: 辐射边缘的颜色

    tileMode:辐射范围之外的着色规则，类型是TileMode

    使用：

    ```
    mShader = new RadialGradient(250,250,250,new int[]{Color.GREEN,Color.YELLOW,Color.RED},null,Shader.TileMode.CLAMP);
    mPaint.setShader(mShader);
    canvas.drawCircle(250,250,250,mPaint);

    ```

    __SweepGradient: 扫描渲染__

    构造方法`SweepGradient(float cx,float cy,int clolor0,int color1)`

    参数：

    cx cy :扫描的中心
    color0:扫描的起始颜色
    color1:扫描的终止颜色

    使用：
    ```
    mShader = new SweepGradient(250,250,Color.RED,Color.GREEN);
    mPaint.setShader(mShader);
    canvas.drawCircle(250,250,250,mPaint);

    ```

     ```
         mShader = new SweepGradient(250,250,new int[]{Color.RED,Color.GREEN},new float[]{0f,0.5f});
        mPaint.setShader(mShader);
        canvas.drawCircle(250,250,250,mPaint);

    ```

    __BitmapShader: 位图渲染__

    构造方法：`BitmapShader(Bitmap bitmap,Shader.TileMode tileX,Shader.TileMode tileY)`

    参数：

    bitmap: 用来做模板的Bitmap对象
    tileX:横向的着色规则，类型是TileMode
    tileY:纵向的着色规则，类型是TileMode

    使用：

    ```
    mShader = new BitmapShader(mBitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
    mPaint.setShader(mShader);
    canvas.drawCircle(250,250,250,mPaint);
    ```

    注意： 
    
    如果绘制区域超出bitmap 的宽高，则会以宽高最后的像素进行拉伸填充（TileMode = ClAMP）
    
    如果绘制区域超出bitmap 的宽高，则会镜像填充（TileMode = MIRROR）

    如果绘制区域超出bitmap 的宽高，则会平铺填充（TileMode =REPEAT）

    __ComposeShader: 组合渲染，例如LinearGradient + BitMapShader__

    构造方法:`ComposeShader(Shader shaderA,Shader shaderB,PorterDuff.Mode mode)`

    参数：

    shaderA,shaderB：两个相继使用的shader
    mode：两个Shader的叠加模式，即shaderA和shaderB应该怎样共同绘制。它的类型是PorterDuff.Mode

    使用：

    ```
    BitmapShader bitmapShader = new BitmapShader(mBitmap,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);

    LinearGradient linearGradient = new LinearGradient(0,0,1000,1600,new int[]{Color.GREEN,Color.BLUE},null,Shader.TileMode.CLAMP);

    mShader = new ComposeShader(bitmapShader,linearGradient,PorterDuff.Mode.MULTIPLY);
    mPaint.setShader(mShader);
    canvas.drawCircle(250,250,250,mPaint);

    ```



4. setColotFiter(ColorFilter colorFilter)设置颜色过滤。一般使用ColorFilter三个子类：

LightColorFilter:光照效果

PorterDuffColorFilter：指定一个颜色和一种PorterDuff.Mode与绘制对象进行合成

ColorMatrixColorFilter:使用一个ColorMatrix来对颜色进行处理


__PorterDuff.Mode 图层混合模式__

它将所绘制图形的像素与Canvas中对应位置的像素按照一定规则进行混合，行车定新的像素值，从而更新Canvas中最终的像素颜色值。

18种模式

||||
|:-:|:-:|:-:|
Mode.CLEAR|Mode.SRC|Mode.DST  
Mode.SRC_OVER|Mode.DST_PVER|Mode.SRC_IN
Mode.DST_IN|Mode.SRC_OUT|Mode.DST_OUT
Mode.SRC_ATOP|Mode.DST_ATOP|Mode.XOR
Mode.DARKEN|Mode.LIGHTEN|Mode.MULTIPLY
Mode.SCREEN|Mode.OVERLAY|Mode.ADD

PS: 这里的SRC代表原图像，DST表示目标图像


- 用到图形混合模式的函数

1. ComposeShader

    ```
     public ComposeShader(Shader shaderA,Shader shaderB,Xfermode mode){
        this(shaderA,shaderB,mode.porterDuffMode)
     }

     ```
2. 画笔的Paint.setXfermode()

```
public Xfermode setXfermode(Xfermode xfermode){
    int newMode = xfermode !=null ? xfermode.porterDuffMode : Xfermode.DEFAULT;

    int curMode = mXfermode !=null ? mXfermode.porterDuffMode : Xfermode.DEFAULT;

    mXfermode = xfermode;

    return xfermode;
}

```

3. PorterDuffColorFilter (颜色过滤器)

构造方法:

`PorterDuffColorFilter(int color,PorterDuff.Mode mode)`

参数：

color: 具体的颜色值，例如Color.RED

mode: 指定PorterDuff.Mode混合模式

使用:

```
PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(Color.RED,PorterDuff.Mode.DARKEN);
paint.setColorFilter(porterDuffColorFilter);
canvas.drawBitmap(mBitmap,100,0,paint);

```



4. LightingColorFilter(颜色过滤器)

构造方法:

` LightingColorFilter(int mul,int add)`

参数：

mul和add都是和颜色值格式相同的int值，其中mul用来和目标像素相乘，add用来和目标像素相加：

R' = R * mul.R / 0xff + add.R

G' = G * mul.G / 0xff + add.G

B' = B * mul.B / 0xff + add.B

使用：

```
ColorFilter lighting = new LightingColorFilter(0x00ffff,0x000000);
paint.setColorFilter(lighting);
canvas.drawBitmap(bitmap,0,0,paint);
```

5. ColorMatrixColorFilter(颜色过滤器)

构造方法：

`ColorMatrixColorFilter(float[] colotMatrix)`

参数：

ColorMatrix矩阵数组

使用：

```
float[] colorMatrix = {
1,0,0,0,0,  //red
0,1,0,0,0,  //green
0,0,1,0,0,  //blue
0,0,0,1,0,  //alpha
};
mColorMatrixColorFilter = new ColorMatrixColorFilter(colotMatrix);
mPaint.setColorFilter(mColorMatrixColorFilter);
canvas.drawBitmap(mBitmap,0,0,mPaint);

```


> 注意：在使用图形混合之前，要禁止硬件加速，因为在android 14之后，图形混合模式有部分功能不支持硬件加速，但是系统默认是开启硬件加速的，所以在使用图形混合模式之前，需要先关闭硬件加速。


禁止硬件加速方法
```
setLayerType(View.LATER_TYPE_SOFTWARE,null);
```

> 在使用图形混合模式的时候，需要使用离屏绘制(离屏缓冲)。通过使用离屏缓冲，把要绘制的内容单独绘制在缓冲层，保证Xfermode的使用不会出现错误结果。

- 直接把整个View都绘制在离屏缓冲中:`View.setlayerType()`,使用GPU来缓冲:`setLayerType(LAYER_TYPE_HARDWARE)`,使用一个Bitmap来缓冲:`setLayerType(LAYER_TYPE_SOFTWARE)`

```

//离屏绘制
int id = canvas.saveLayer(0,0,getWidth(),getHeight(),mPaint,Canvas.ALL_SAVE_FLAG);

// 目标图
canvas.drawBitmap(dstBitmap,0,0,mPaint);
//设置混合模式
mPaint.setXformde(new PorterDuff.Mode.DST_IN);
//源图
canvas.drawBitmap(srcBitmap,0,0,mPaint);
//清除混合模式
mPaint.setXfermode(null);

canvas.restoreToCount(id);

```

> 图形混合模式只作用于原图像上，及之后绘制的图像上










