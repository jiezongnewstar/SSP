### Android 的线程和线程池

> 线程
- 主线程 <br>
主线程是指晋城所用有的线程，在Android中也称作UI线程，负责处理界面交互相关的逻辑。所以所有的耗时操作在主线程中，都会造成界面的卡顿。因此在主线程中不能进行耗时操作，应该将耗时操作(网络请求/IO操作等)放在主线程操作，然后回调到主线程。

- 子线程 <br>
除了主线程以外的线程都叫做子线程


> Android中的线程
- AsyncTask 
- HandlerThread  
- IntentService 

---
接下来详细介绍着三种线程

#### AsyncTask
    AsnycTask是一种轻量级的一部任务类，底层基于Thread。封装了线程池和Handler，主要为了方便开发者在子线程中更新UI。但是并不适合进行特别耗时的后台任务，对于特别耗时的任务，建议使用线程池。 

AsyncTask是一个抽象的泛型类，它提供了Params、Progress和Result这三个泛型参数，其中Params表示参数的类型，Progress表示后台任务的执行进度的类型，Result表示返回结果的类型。
    

##### AsyncTask 方法罗列
```
    public class AsyncTaskDemo extends AsyncTask {
    
    
    @Override
    protected Object doInBackground(Object[] objects) {

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onCancelled(Object o) {
        super.onCancelled(o);
    }


    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals( Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    @Override
    public String toString() {
        return super.toString();
    }


}


```

##### AsyncTask提供了4个核心方法，他们的含义如下所示：

- onPreExecute() ---在主线程中执行，在异步任务执行之前，此方法会被调用，一般用于做一些准备工作。

- doInBackGround(Params ...params)---在线程池中执行，此方法用于执行异步任务，params参数表示异步任务的输入参数。在此方法中可以通过 publishProgress方法来更新任务的进度，publishProgress方法会调用onProgressUpdate方法。另外此方法需要返回计算结果给onPostExecute方法。

- onProgressUpdate(Progress ...values)---在主线程中执行，当后台任务的执行进度发生改变时此方法会被调用。

- onPostExecute(Result result)---在主线程中执行，在异步任务执行之后，此方法会被调用，其中result参数是后台任务的返回值，即doInBackfround的返回值。

_以上几个方法，onPreExecute先执行，接着是doInBackground,最后是onPostExecute。除了这些方法，AsyncTask还提供了onCancled()方法，他同样也在主线程执行，这个时候，onPostExecute()就不会被调用了。_

> AsyncTask 典型事例
```
public class AsyncTaskDemo extends AsyncTask<URL,Integer,Long>{

    @Override
    protected Long doInBackground(URL... urls) {
        
        int count  = urls.length;
        long totalSize = 0;
        
        for (int i = 0; i < count; i++){
            
            publishProgress((int)(i/(float)count)*100);
            if (isCancelled()){
                break;
            } 
        }
        return totalSize;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.e("进度",values+"");
    }
}
```
_在上面代码中，实现了一个具体的AsyncTask类，这个类主要用于模拟下载过程，他的输入参数类型为URL，后台任务的进度参数为Integer，而后台任务返回的结果为Long类型。注意到doInBackground和onProfressUpdate方法他们的参数中均包含...的字样，在Java中...表示参数的数量不定，它是一种数组型参数，...的概念和C语言中的...是一致的。当要执行上述下载任务时，可以通过一下方式来完成_

`new AsyncTaskDemo().execute(url1,url2,url3);`

_AsyncTaskDemo中，doInBackground用来执行具体下载任务并通过publishProgress方法来更新下载进度，同时还要判断下载任务是够被外界取消了。当下载任务完成之后，doInBackground会返回结果，即下载的粽子节数。需要注意的是，doInBackground是在线程池中执行的，。onProgressUpdate用户更新界面中的下载进度，它运行在主线程，当publishProgress被调用时，此方法就会被调用。当下载任务完成之后，onPostExecute方法就会被调用，他也是运行在主线程中的，这个时候我们就可以在界面上做出一些提示_

> AsyncTask在具体的使用过程中也是有一些条件限制的，主要有一下几点:

- AsyncTask的类必须在主线程中加载，这就意味着第一次访问AsyncTask必须发生在主线程，这个过程在Android4.1及以上版本被系统自动完成。在Android5.0的源码中，可以查看ActivityThread的main方法，它会调用AsyncTask的init方法，这就满足了AsyncTask的类必须在主线程中进行加载这个条件了。

- AsyncTask的对象必须在主线程中被创建
- execute方法必须在UI线程中创建
- 不要在程序中直接调用pnPreExecute()、onPostExecute()、doInBackground()和onProfressUpdate()方法
- 一个AsyncTask对象只能执行一次，即只能执行一次execute()方法,否则会报运行时异常。
- 在Android1.6之前，AsyncTask是串行执行任务的，Android1.6的时候AsyncTask开始采用线程池处理并行任务，但是在Android3.0开始，为了避免AsyncTask所带来的并发错误，AsyncTask又采用一个线程来串行执行任务。尽管如此，在Android以后的版本中，我们仍然可以通过AsyncTask的executeOnExecutor()方法来并行执行任务

#### HandlerThread
 
HandlerThread 继承于 Thread 类，通过 getThreadHandler()方法创建handler代码如下：
    ```
       public Handler getThreadHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }
    ```

Looper 通过 实现run()方法来创建，代码如下：
```
 @Override
    public void run() {
        mTid = Process.myTid();
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(mPriority);
        onLooperPrepared();
        Looper.loop();
        mTid = -1;
    }
```

结束方法有两个 quit() 和 quitSafely(),分别如下：

quit:
```
   public boolean quit() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }
```

quitSafely:

```
 public boolean quitSafely() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quitSafely();
            return true;
        }
        return false;
    }

```

#### IntentService







