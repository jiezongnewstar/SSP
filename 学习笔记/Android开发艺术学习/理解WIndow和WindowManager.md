### Window 和WindowManager

---
PS:这里先跟着玉刚过一遍，后面请在`源码分析`部分进行一波分析

---

Window 表示一个窗口的概念，在日常开发中直接接触Window的机会并不多，但是在某些特殊时候我们需要在桌面上展示一个类似悬浮窗的东西，那么这种效果就需要用到Window来实现。Window是一个抽象类，它的具体实现是PhoneWindow。创建一个Window是很简单的事情，只需要通过WindowManager即可完成。WindowManager是外界访问Window的入口，Window的具体实现位于WindowManagerService中，WindowManager和WindowManagerService的交互是一个IPC过程。Android中所有的视图都是通过Window来呈现的，不管是Activity、Dialog还是Toast，他们的视图实际上都是附加在Window上的，因此Window实际是View的直接管理者。

#### Window和WindowManager

为了分析Window的工作机制，我们需要先了解如何使用WindowManager添加一个Window。下面代码演示通过WindowManager添加Window的过程：

```
mFLoatingButton = new Button(this);
mFLoatingButton.settext("button");
mLayoutParams = new WindowManager.LayoutParams(LayoutParmas.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,0,0,PixelFormat.TRANSPARENT);

mLayoutParmas.flags = LayoutParmas.FLAG_NOT_TOUCH_MODAL|LayoutParmas.FLAG_NOT_FOCUSABLE|
LayoutParmas.FLAG_SHOW_WHEN_LOCKED;

mLayoutParmas.gravity = Gravity.LEFT|Gravity.TOP;

mLayoutParmas.x = 100;
mLayoutParma.y = 300;
mWindowManager.addView(mFLoatingButton,mLayoutParmas);

```
上述代码可以将一个Button添加到屏幕坐标为（100，300）的位置上。WindowManager.LayoutParmas 中的flags和type这两个参数比较重要，下面对其进行说明。

Flags参数表示Window的属性，它有很多选项，通过这些选项可以控制Window的显示特性，这里主要介绍几个比较常用的选项，剩下的请查看官方文档。

- FLAG_NOT_FOCUSABLE
表示Window不需要获取焦点，也不需要接收各种输入事件，此标记会同时启用FLAG_NOT_TOUCH_MODAL，最终事件会直接传递给下层具体有焦点的Window。

- FLAG_NOT_TOUCH_MODAL
在此模式下，系统会将当前Window区域以外的单击事件传递给底层的Window，当前Window区域以内的单击事件则自己处理。这个标记很重要，一般来说都需要开启此标记，否则其他Window将无法接收到单击事件。

- FLAG_SHOW_WHEN_LOCKED
开启此模式可以让Window显示在锁屏界面上。

Type参数表示Window的类型，Window有三种类型，分别是应用的Window、子Window和系统的Window。应用类Window对应着一个Activity。子Window不能单独存在，它需要附属在特定的父Window之中，比如常见的一些Dialog就是一个子Window。系统Window是需要声明权限在能创建的Window，比如Toast和系统状态栏这些都是系统Window。

Window是分层的，每个Window都有对应的z-ordered，层级大的会覆盖在层级小的Window上面，这和HTML中的z-index的概念完全是一致的。在三类Window中，应用Window的层级范围是1~99，子WIndow的层级范围是1000~1999，系统Window的层级范围是2000~2999，这些层级范围对应着WindowManager.LayoutParmas的type参数。如果想要Window位于所有Window的最顶层，那么采用较大的层级即可。很显然系统Window的层级是最大的，而且系统层级有很多值，一般我们可以选用TYPE_SYSTEM_OVERLAY或者TYPE_SYSTEM_ERROR,如果采用TYPE_SYSTEM_ERROR，只需要为type参数指定这个层级即可：mLayoutParams.type = LayoutParmas.TYPE_SYSTEM_ERROR;同时声明权限：<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW">。因为系统类型的Window是需要检查权限的，如果不再AndroidMainfest中使用相应的权限，那么创建Window的时候就会报错。`Caused by:android.view.WindowManager$BadTokenException:Unalbe to add window adnroid.view.ViewrootImpl@W@42882fe --permission denied for this window type` WindowManager所提供的功能很简单，常用的只有三个方法，即添加View、更新View和删除View，这三个方法定义在ViewManager中，而WindowManager继承了ViewManager。
```
public interface ViewManager{
    public void addView(View view,ViewGroup.LayoutParmas parmas);

    public void updateViewLayout(View view,ViewGroup.LayoutParmas parmas);

    public void removeView(View view);

}
```
对于开发者来说，WindowManager常用的就是这三个功能而已，但是这三个功能已经足够我们使用了，它可以创建一个Window并向其添加View，还可以更新Window中的View，另外如果想要删除一个Window，那么只需要删除它里面的View即可。由此来看，WindowManager操作Window的过程更像是在操作Window中的View。我们时常见到那种可以拖动的Window效果，其实很好实现，只需要根据手指的位置来设定LayoutParmas中的x和y的值即可改变Window的位置。首先给View设置onTouchListener: mFloatingButton.setOnTouchListener(this)。然后在onTouch方法中不断更新View的位置即可：
```
public boolean onTouch(View v,MotionEvent event){
    int rawX = (int)event.getRawX();
    int rawY = (int)event.getRawY();

    switch(event.getAction()){
        case MotionEvent.ACTION_MOVE:
        {
            mLayoutParmas.x = rawX;
            mLayoutParmas.y = rawY;
            mWindowManager.updateViewLayout(mFloatingButton,mLayoutParmas);
        }
        break;
        default:
        break;
    }

    return false;
}

```


#### Window的内部机制
Window是一个抽象的概念，每一个Window都对应着一个View和一个ViewRootImpl，Window和View通过ViewRootImpl来建立联系，因此Window并不是实际存在的，它是已View形式存在的实体。在实际使用中无法直接访问Window，对于Window的访问必须通过WindowManager。为了分析Window的内部机制，这里从Window的添加、删除、以及更新说起。

> Window的添加过程
Window的添加过程需要通过WindowManager的addView来实现，WindowManager是一个接口，它的真正实现是WindowManagerImpl类。在WindowManagerImpl中Window的三大操作如下：
```
@Override
public void addView(View view,ViewGroup.LayoutParmas parmas){
    mGLobal.addView(view,parmas,mDisplay,mParentWindow);
}

@Override
public void updateViewLayout(View view,ViewGroup.LayoutParmas parmas){
    mGlobal.updateLayout(view,parmas)
}


@Override
public void removeView(View view){
    mGlobal.removeView(view,false)
}

```

可以发现，WindowManagerImpl并没有直接实现Window的三大操作，而是全部交给WindowManagerGlobal来处理，WindowManagerGlobal以工厂的形式向外提供自己的实例，在WindowManagerGlobal中有如下一段代码:
```
private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();

```

WindowManagerImpl这种工作模式是典型的桥接模式，将所有的操作全部交托给WindowManagerGlobal来实现。WindowManagerGlobal的addView方法主要分为以下几步。
- 检查参数是否合法，如果是子Window那么还需要调整一些布局参数

```
if（view ==null）{
    throw new IllegalArgumentException("view must not be null");
}

if(display == null){
    throw new IllegalArgumentException("display must not be null");
}

if(!parmas instanceof WindowManager.LayoutParmas){
    throw new IllegalArgumentException("Parmas must be WindowManager.LayoutParmas");
}

final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;

if(parentWindow !=null){
    parentWindow.adjustLayooutParamsForSubWindow(wparmas);
}

```

- 创建ViewRootImpl并将View添加到列表中
在WindowManagerGlobal内部有如下几个列表比较重要：
```
private final ArrayList<View> mViews = new ArrayLisr<View>();

private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();

private final ArrayList<WindowManager.LayoutParams> mParmas = new ArrayList<WindowManager.LayoutParams>();

private final ArraySet<View> mDyingViews = new ArraySet<View>();

```

在上面的声明中，mViews存储的是所有Window所对应的View，mRoots存储的时所有Window所对应的ViewRootImpl，mParams存储的时所有Window对应的布局参数，而mDyingViews则存储了那些正在被删除的View对象，或者说是那些已经调用了removeView方法但是删除操作还未完成的Window对象。在addView中通过如下方式将Window的一些列对象添加到列表中：
```
root = new ViewRootImpl(view.getContext(),display);

view.setLayoutParams(wparams);
mView.add(view);
mRoots.add(root);
mParams.add(wparams);

```

- 通过ViewRootImpl来更新界面并完成Window的添加过程

这个步骤由ViewRootImpl的setView方法来完成，我们知道View的绘制流程是由ViewRootImpl来完成的，这里当然也不例外，在setView内部会通过requestLayout来完成异步刷新请求。在下面代码中，scheduleTraversal实际是View绘制的入口：
```
public void requestLayout(){
    if(!mHandlingLayoutInLayoutRequest){
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }

}

```

接着会通过WindowSession最终完成Window的添加过程，在下面代码中，mWindowSession的类型是IWindowSession，它是一个Binder对象，真正的实现类是Session，也就是Window的添加过程是一此IPC调用。

```
try{
    mOrginWindowtype = mWindowAttributes.type;
    mAttachInfo.mRecomputeGlobalAttributes = true;
    collectVIewAttributes();
    res = mWindowSession.addToDisplay(mWindow,mSeq,mWindowAttributes,getHostVisibility(),mDisplay.getDisplayId(),mAttachInfo.mContentInsets,mInputChannel);

}catch(RemoteException e){
    mAdded = false;
    mView = null;
    mAttachInfo.mRootView = null;
    mInputChannel = null;
    mFallbackEventHandler.setView(null);
    unscheduleTraversals();
    setAccesssibilityFocus(null,null);
    throw new RuntimeException("Adding window failed",e);

}


```

在Session内部会通过WindowManagerService来实现Window的添加，代码如下：
```
public int addToDisplay(IWindow window,int seq,WindowManager.LayoutParams attrs,int viewVisibility,int displayId,Rect outContentInsets,InputChannel outInputChannel){
    return mService.addWindow(this,window,seq,attrs,viewVisibility,displayId,outContentInsets,outInputChannel)
}


```

如此一来，Window的添加请求就交给WindowManagerService去处理了，在WindowManagerService内部会为每一个应用保留一个单独的Session。

> Window的删除过程
Window的删除过程和添加过程一样，都是先通过WindowManagerImpl后，再进一步通过WindowManagerGlobal来实现的。下面是WindowManagerGlobal的removeView的实现：
```
public void removeView(View view,boolean immediate){
    if(view == null){
        throw new IllegalArgumentException("view must be null");
    }

    synchronized(mLock){
        int index = findViewLocked(view,true);
        View curView = mRoots.get(index).getView();
        removeViewLocked(index,immediate);
        if(curView == view){
            return;
        }

        throw new IllegalArumentException("Calling with view"+view+"but  the ViewAncestor is attached to "+ curView);
    }

}


```
removeView的逻辑很清晰，首先通过findViewLocked来查找待删除的View的索引，这个查找过程就是建立的数组遍历，然后再调用removeViewLocked来做进一步的删除，如下所示：
```
private void removeViewLocked(int index,boolean immedliate){
    ViewRootImpl root = mRoots.get(index);
    View view = root.getView();


if(view ==null){
    InputMethodManager imm = InputMethodManager.getInsatance();

    if(imm != null){
        imm.windowDismissed(mView.get(index).getWindowToken());
    }
}

boolean deferred = root.die(immediate);
if(view !=null){
    view.assignParent(null);
    if(deferred){
        mDyingViews.add(view);
}

}

```

removeViewLocked是通过ViewRootImpl来完成删除操作的。在WindowManager中提供了两种删除接口，removeView和removeViewImmediate，它们分别表示异步删除和同步删除，其中removeViewImmediate使用起来需要特别注意，一般来说不需要使用此方法来删除Window以免发生意外的错误。这里主要说异步删除的情况，具体删除操作由ViewRootImpl的die方法来完成。在异步删除的情况下，die方法只是发送了一个请求删除的消息后就立刻返回了，这个时候View并没有完成删除操作，所以最后会将其添加到mDyingViews中，mDyingViews表示待删除的View列表。ViewRootImpl的die方法如下：
```
boolean die(boolean immediate){
    if(immediate && !mIsInTraversal){
        doDie();
        return false
    }

    if(!mIsDrawing){
        destroyHardwareRender();
    }else{
        Log.e(TAG,"Attempting to destory the window while drawing "+window+);
    }

    mHandler.sendEmptyMessage(MSG_DIE);
    return true;
}

```

在die方法内部只是做了简单的判断，如果是异步删除，那么就发送一个MSG_DIE的消息，ViewRootImpl中的Handler会处理此消息并调用doDie方法，如果是同步删除（立即删除），那么久不发消息直接调用doDie方法，这就会这两种删除方式的区别。在doDie内部会调用dispatchDetachedFromWindow方法的内部实现。dispatchDetachedFromWindow方法主要做四件事情

1. 垃圾回收相关的工作，比如清楚数据消息、移除回调
2. 通过Session的remove方法删除Window：mWindowSession.remove(mWindow),这同样是一个IPC过程，最终会调用WindowManagerService的removeWindow方法。
3. 调用View的dispatchDetachedFromWindow方法，在内部会调用View的onDetachedFromWindow以及onDetachedFromWindowInteral。对于onDetachedFromWindow大家一定不陌生，当View从Window中移除时，这个方法就会被调用，可以在这个方法内部做一些资源回收的工作，比如终止动画、停止线程等。
4. 调用WindowManagerGlobal的doRemoveView方法刷新数据，包括mRoots、mParams以及mDyingViews，需要将当前Window所关联的这三类对象从列表删除。

> Window的更新过程

到这里，Window的删除过程已经分析完毕了，下面分析Window的更新过程，还是要看WindowManagerGlobal的updateViewLayout方法，如下所示：
```
public void updateViewLayout(View view,ViewGroup.LayoutParasm params){

 if(view ==null){
     throw new IllegalArgumentException("view must not be null");
 }

 if(!params instanceof WindowManager.LayoutParams){
     throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
 }

 final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;

 view.setLayoutParams(wparams);

 synchronized(mLock){
     int index = findViewLocked(view,true);
     ViewRootImpl root = mRoots.get(index);
     mParams.remove(index);
     mParams.add(index,wparams);
     root.setLayoutParams(wparams,false);
 }

}

```
updateViewLayout方法做的事情就比较简单了，首先它需要更新View的LayoutParams并替换掉老的LayoutParams，接着再更新ViewRootImpl中的LayoutParams，这一步是通过ViewRootImpl的setLayoutParams方法来实现的。在ViewRootImpl中会通过scheduleTraversals方法对View重新布局，包括测量、布局、重绘这三个过程。除了View本身的重绘以外，ViewRootImpl还会通过WindowSession来更新Window的视图，这个过程最终是由WindowManagerService的relayoutWindow来具体实现，它同样是一个IPC过程。


#### Window的创建过程
> Activity的Window创建过程
> Dialog的Window创建过程
> Toast的Window创建过程
