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