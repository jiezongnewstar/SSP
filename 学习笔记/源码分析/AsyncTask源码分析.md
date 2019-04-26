### AsyncTask 源码分析
_现在一步步对这个类的源码进行分析，经过站在巨人的肩膀上的学习与借鉴，向他们学习那种阅读源码的思路与方式，同时结合自己实际情况，总结出：读源码的时候，从一个关键入口去切入，这样会更好的掌握执行流程，在入口之前，先要学会如何引用与实现，还有就是去读注释。以下的源码阅读过程中，将不再赘述注释部分。好了下面我们开始。_

> 从使用入手

我们都知道，创建AsyncTask的方式是这样子的：<br> 
`new AsyncTask(Params,Progress,Result).execute(Params ... params)`

那我们的切入定就从这里开始execute()方法入手,在源码里查找了一下，竟然发现重写了不只一个方法，我就是那个头铁的人，都弄明白，因为，毕竟代码不像Activity那个类的代码行数吓人，不啰嗦，直接来看：<br>

第一个：
``` 
@MainThread
    public final AsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(sDefaultExecutor, params);
    }

```
第二个：
```
@MainThread
    public static void execute(Runnable runnable) {
        sDefaultExecutor.execute(runnable);
    }

```
哈哈哈，好吧，原来就俩。细心的同学肯定可以发现 `@MainThread`这东西，言简意赅，这个方法的调用必须在主线程，对比一下，这两个方法的返回参数和传入参数都有区别。第一个返回AsyncTask，而第二个无返回参数，至此我们知道，第二个并不是真正我们想要的execute(),切入正题：<br>

execute()方法返回执行的是executeOnExecutor(sDefaultExecutor, params)这个函数，点进去看看：

```
 @MainThread
    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
            Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;

        onPreExecute();

        mWorker.mParams = params;
        exec.execute(mFuture);

        return this;
    }
```
好吧，同样是一个带有`@MainThread` 的函数，返回类型是AsyncTask，传入了一个Executor，和不定长的参数。再回去看一下，传入的 Executor 是 sDefaultExecutor。没办法，那就继续去看一下这是啥东西。<br>

是个Executor：<br>
`    private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR; 
 ` <br>
 是这个SerialExecutor(),注释中说是一个in serial order (有序执行即串行的)<br>
 `    public static final Executor SERIAL_EXECUTOR = new SerialExecutor();
`<br>
接着我们来看一下SerialExecutor；<br>
```
  private static class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        Runnable mActive;

        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }
```

这段代码我们发现，在这个Executor中创建了一个mTasks的队列（先进先出依次执行）。可以发现，无论如何，都会执行scheduleNext()方法。这个方法里又通过` THREAD_POOL_EXECUTOR.execute(mActive);`搞了事情，好吧，撵着屁股走，再点进`THREAD_POOL_EXECUTOR`看一下。
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
THREAD_POOL_EXECUTOR 是在静态代码块里创建的。对应的配置参数:
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

关于Executor（线程池）的创建及其他，我会在线程池专题来讲，这里就理解为创建了一个线程池即可。很难受，看到这里，我们把executeOnExecutor的传入参数Executor搞清楚了。接着又得返回去看这个方法。我们看一下他的执行过程，先做了一个判断：

```
 @MainThread
    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
            Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;

        onPreExecute();

        mWorker.mParams = params;
        exec.execute(mFuture);

        return this;
    }
```

介绍一下Status：

- PENDING : 任务尚未执行
- RUNNING : 任务正在执行
- FINISHED :任务已经结束

很清楚了，说明了一点，一个AsyncTask的execute()方法只能执行一次，要不然就会抛出异常。接着往下走。 通过判断也就意味着exectue()执行成功，那么也就变成RUNNING状态，同时调用了onPreExecute()方法，这里注意，onPreExecute()方法是在主线程被调用的同时也需要我们在自己去重写实现自己的准备工作。接着又执行了这么一句话  `mWorker.mParams = params;
`行吧，又得去看mWorker是什么： <br>

```
private final WorkerRunnable<Params, Result> mWorker;

public AsyncTask(@Nullable Looper callbackLooper) {
        mHandler = callbackLooper == null || callbackLooper == Looper.getMainLooper()
            ? getMainHandler()
            : new Handler(callbackLooper);

        mWorker = new WorkerRunnable<Params, Result>() {
            public Result call() throws Exception {
                mTaskInvoked.set(true);
                Result result = null;
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    //noinspection unchecked
                    result = doInBackground(mParams);
                    Binder.flushPendingCommands();
                } catch (Throwable tr) {
                    mCancelled.set(true);
                    throw tr;
                } finally {
                    postResult(result);
                }
                return result;
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    android.util.Log.w(LOG_TAG, e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occurred while executing doInBackground()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }

     private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }

```
嗯，是一个抽象类，用来存Parmas的，在构造方法里进行了实例化，`mTaskInvoked.set(true);`标记当前任务已经执行。 通过 `    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
` 设置成后台线程 同时还做了一个操作，那就是` result = doInBackground(mParams);`正儿八经开始做事情了，自己实现具体的后台操作，如果有异常的话，那么就把mCancelled设置为被取消状态，最终 postResult(result)把结果返回给创建的handler，这里注意，Handler必须创建在主线程，才能结果回调到主线程：<br>
```
 private static class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }
        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult<?> result = (AsyncTaskResult<?>) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    // There is only one result
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
            }
        }
    }
```
再回头看executeOnExecutor()方法：
```
 @MainThread
    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
            Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;

        onPreExecute();

        mWorker.mParams = params;
        exec.execute(mFuture);

        return this;
    }
```
要执行`exec.execute(mFuture)` 及 `sDefaultExecutor.execute(mFuture)` 我们再看SerialExecutor：

``` 
private static class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        Runnable mActive;

        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }
```

最终执行的` THREAD_POOL_EXECUTOR.execute(mActive)`

对于get() 、cancle() 、 还有onProgressUpdate（Progress... values）、publishProgress（Progress... values）、finish（Result result）等就不在这里说了

至此，AsyncTask的整个工作流程已经理完了，说句实话，我顺着源码走了一遍流程，到现在还是有写地方没有记住，还好，我写笔记了，就是这么原始。









