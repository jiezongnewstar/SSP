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

IntentService 是一个继承与Service 的抽象类 成员变量包含Looper和ServiceHandler。并且在onCreate()方法中创建了HandlerThread。整个工作流程是这样的：<br>

在Oncreate()方法中创建HandlerThread,通过HandlerTHread的getLooper()方法拿到Looper对象，创建ServiceHandler并与Looper绑定。 onCreate()执行完毕之后，就执行onStart()方法，在onStart()里面创建Message并发送，这里注意onStart(Intent intent,int startId) 是可以携带参数的。在onHaldleIntent中接收消息，并做出处理。结束的时候调用looper的quit()方法。


IntentService可用于执行后台耗时的任务，当任务执行后，他会自动停止，同时由于IntentService是服务的原因，这样它的优先级比淡出拿到线程要高的多，所以IntentService比较适合执行一些高优先级的后台任务，因为它优先级高不容易被系统杀死。事实上，IntentService比较合适执行一些高优先级的后台任务，因为它优先级高不容易被系统杀死。


---
>Android中的线程池
线程池的优点：
- 线程重用，避免线程创建和销毁所带来的性能开销
- 有效控制线程池的最大并发数，避免大量线程之间因为互相抢占资源导致的阻塞现象
- 对线程进行简单的管理，并提供定时执行以及指定间隔循环执行等功能。

Android中的线程池的概念源于Java中的Executor，Executor是一个接口，真正的线程池的实现为ThreadPoolExecutor。ThreadPoolExecutor提供了一系列参数来配置线程池，通过不同的参数可以创建不同的线程池，从线程池的功能特性上来说，Android的线程池主要分为4类，这4类线程池可以通过Executors所提供的的工厂方法得到，因为Android中的线程池都是直接或者间接通过配置ThreadPoolExecutor来实现的，因此要先介绍ThreadPoolExecutor。

#### ThreadPoolExecutor

THreadPoolExecutor是线程池的真正实现，它的构造方法提供了一些列参数来配置线程池，下面介绍ThreadPoolExecutor的构造方法中的各个参数的含义，这些参数将会直接影响到线程池的功能特性，下面ThreadPoolExecutor的一个比较常用的构造方法。
```
 public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }
```
- corePoolSize

线程池的核心线程数，默认情况下，核心线程会在线程池中一直存活，及时它们处于闲置状态。如果将ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为true，那么闲置的核心线程在等待新任务到来时会有超时策略，这个时间间隔由keepAliveTime所指定，当等待时间超过keepAliveTime所指定的时长后，核心线程就会被终止。
- maximumPoolSize

线程池所能容纳的最大线程数，当活动线程数达到这个数值后，后续的新任务将会被阻塞
- keepAliveTime

非核心线程闲置超时时长，超过这个时长，非核心线程就会被回收。当ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为true时，keepAliveTime同样会作用于核心线程。
- unit 

用于指定keepAliveTime参数的时间单位，这是一个枚举，常用的有TiimeUnit.MILISECONFS（毫秒）/TimeUint.MINUTES（分钟）等。

- workQueue
线程池中的任务队列，通过线程池的execute方法提交的Runnable对象会存储在这个参数中
- threadFaactory

线程工厂，为线程池提创建新线程的功能。ThreadFactory是一个接口，它只有一个方法：Thread newThread(Runnable r).

除了上面的这些主要参数外，ThreadPoolExecutor还有一个不常用的参数Rejected-ExecutionHandler handler。当线程池无法执行新任务时，这可能是由于任务队列已满或者是无法成功执行任务，这个时候ThreadPoolExecutor会调用handler的rejectedExecution-Exception。 ThreadPoolExecutor为RejectdExecutionHandler 提供了几个可选值：CallerRunPolicy、AbortPolicy、DiscardPolicy、和DiscardOldestPolicy其中AbortPolicy是默认值，它会直接抛出RejectExecutionException，由于handler这个参数不常用，这里就不再具体介绍了。

ThreadPoolExecutor执行任务时大致遵循如下规则：

- 如果线程池中的线程数量未达到核心线程的数量，那么会直接启动一个核心线程来执行任务。
- 如果线程池中的线程数量已经达到或者超过核心线程的数量，那么任务会被插入到任务队列中排队等待执行。
- 如果在步骤2中无法将任务插入到队列中，这往往是由于任务队列已满，这个时候如果线程数量未达到线程池规定的最大值，那么会立刻启动一个非核心线程来执行任务。
- 如果步骤3中线程数量已经达到线程池规定的最大值，那么就拒绝执行任务，ThreadPoolExecutor会调用RejectExecutionHandler的rejectedExecution方法来通知调用者。

ThreadPoolExecutor的参数配置在AsyncTask中有看到过：

```
   private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);
```

```
    public static final Executor THREAD_POOL_EXECUTOR;

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

```

从上面的代码可以知道，AsyncTask对THREAD_POOL_EXECUTOR这个线程池进行了配置，配置后的线程池规格如下：
- 核心线程池数等于CPU核心数 +1
- 线程池的最大线程数为CPU核心数的2倍 +1 
- 核心线程无超时机制，非核心线程在闲置时的超时时间为1秒
- 任务队列的容量为128

> 线程池的分类

#### FixedThreadPool 

通过Executors 的newFixedThreadPool方法创建。它是一种线程数量固定的线程池，当线程处于空闲状态时，他们并不会被回收，除非线程池被关闭了。当所有的线程都处于活动状态时，新任务都会处于等待状态，知道有线程空闲出来。由于FixedThreadPool只有核心线程并且这些核心线程不会被回收，这意味着它能够个更加快速的响应外界的请求。newFixedThreadPool 方法实现如下，可以发现FixedThreadPool中只有核心线程并且这些核心线程没有超时机制，另外任务队列也是没有大小限制的。

#### CachedThreadPool 

通过Executors的newCachedThreadPool方法来创建。它是一种线程数量不定的线程池，他只有非核心线程，并且其中最大线程数为Integer.Max_VALUE。由于Integer.MAX_VALUE是一个很大的数，实际上就相当于最大线程数可以任意大。当线程池中的线程都处于活动状态时，线程池会创建新的线程来处理新任务，否则就会利用空闲线程来处理新任务。线程池中的空闲线程都有超时机制，
这个超时时长为60秒，超过60秒闲置线程就会被回收。和FixedThreadPool不同的是，CachedThreadPool在任务队列其实相当于一个空集合，这将导致任何任务都会立即被执行，因为在这种场景下SzynchronousQueue是无法插入任务的。SynchronousQueue是一个非常特殊的队列，在很多情况下可以把它简单理解为一个无法存储元素的队列，由于它在实际中较少使用，这里就不深入讨论它了。从CachedThread
Pool的特性来看，这类线程池比较适合执行大量的耗时较少的人物。当整个线程池都处于闲置状态时，线程池中的线程都会超时而被停止，这个时候CachedThreadPool之中实际上是没有任何线程的，它几乎是不占用任何系统资源的。

#### SecheduledThreadPool 

通过EXecutors的newSecheduledThreadPool方法来创建。它的核心线程数量是固定的，而非核心线程数是没有限制的，并且当非核心线程闲置时会被立即回收。ScheduledThreadPool 这类线程池主要用于执行定时任务和具有固定周期的重复任务。

#### SingleThreadExecutor 

通过Executors的newSingleThreadExecutor方法来创建。这类线程池内部只有一个核心线程，他确保所有的任务在同一个线程中按顺序执行。SinglethreadExecutor的意义在于统一所有的外界任务到一个线程中，这使得在这些任务之间不需要处理线程同步的问题。









