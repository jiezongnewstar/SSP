### IPC机制

IPC是Inter-Process Communication的缩写，含义为进程间通信或者跨进程通信，是指两个进程之间进行数据交换过程。说起进程间通信，我们首先要理解什么是进程，什么是线程，进程和线程是截然不同的概念。按照操作系统中的描述，线程是CPU调度的最小单元，同时线程是一种有限的系统资源。而进程一般指一个执行单元，在PC和移动设备上指一个程序或者一个应用。一个进程可以包含多个线程，因此进程和线程是包含与被包含的关系。最简单的情况，一个进程中可以有一个线程，即主线程，在Android里面，主线程叫做UI线程，在UI线程里才能操作界面元素。很多时候，一个进程中需要执行大量耗时的任务，如果这些任务放在主线程中去执行就会造成界面无法响应，严重影响用户体验，这种情况在PC系统和移动系统中都存在， 在Android中有一个特殊的名字叫做ANR
Application Not Responding ，即应用无响应，解决这个问题就需要用到线程，把一些耗时任务放在线程中。

> Android IPC机制

IPC不是Android中所独有的，任何一个操作系统都需要有相应的IPC机制，比如Windows上则可以通过剪贴板、管道和油槽等来进行进程间通信；Linux上可以通过命名管道、共享内存、信号量等来进行进程间通信。可以看到不同的操作系统平台有着不同的进程间通信方式，对于Android来说，它是一种基于Linux内核的移动操作系统，它的进程间通信方式并不能完全继承自Linux，相反，它有自己的进程间通信方式。在Android中最有特色的进程间通信方式就是Binder了，通过Binder可以轻松实现进程间通信。除了Binder，Android还支持Socket，通过Socket也可以实现任意两个终端之间通信，当然同一个设备上的两个进程通过Socket通信自然也是可以的。

说到IPC的使用场景就必须提到多进程，只有面对多进程这种场景下，才需要考虑进程间通信。这个是很好理解的，如果只有一个进程在运行，又谈何多进程呢？多进程的情况分两种。第一种情况是一个应用因为某些原因自身需要运行在单独的进程中，又或者为了加大一个应用可使用的内存所以需要通过多进程来获取多份内存空间。Android对单个应用所使用的最大内存做了限制，早起一些版本可能是16M，不同的设备有不同的大小。另一种情况是当前应用需要向其他应用获取数据，由于是两个应用，所以必须采取夸进程的范式来获取所需的数据，甚至我们通过系统提供的ContentProvider去查询数据的时候，其实也是一种进程间通信，只不过通信细节被系统内部屏蔽了，我们无法感知而已。


> Android中的多进程模式

在正式介绍进程间通信之前，我们必须要先理解Android中的多进程模式。通过给四大组件指定android:process属性，我们可以轻易地开启多进程模式，这看起来很简单，但是实际使用过程中取暗藏杀机，多进程远远没有我们想的那么简单，有时候我们通过多进程得到的好处甚至都不足以弥补使用多进程所带来的代码层面的负面影响。

#### 开启多进程模式

在Android中使用多进程只有一种方法，那就是给四大组件(Acitvity、Service、Reciver、ContentProvider)在androidManifest中指定其裕兴时所在的进程。其实还有另一种非常规的多进程方法，那就是通过JNI在native层去fork 一个新的进程，但是这种方法属于特殊情况，也不是常用的创建多进程的方式，因此我们暂时不考虑这种方式。下面一个示例，描述了如何在Android 中创建多进程：
```
<acitivty
    android:name="com.xibei.MainActivity"
    android:configChanges="orientation|screenSize"
    android:label="@string/app_name"
    android:launchMode="standard">

    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER/>
        </intent-fliter>
</activity>

<activity
    android:name ="com.xibei.FirstActivity"
    android:configChanges = "screenLayout"
    android:label= "@string/app_name"
    android:process=":remote"/>

    <activity
        android:name="com.xibei.SecondActivity"
        android:configChanges="screenLayout"
        android:label="@string/app_name"
        android"process="com.xibei.remote2"/>

```
上面的示例分别为FirstActivity 和 SecondActivity指定了process属性，并且他们的属性值不同，这意味着当前应用又增加了两个新进程。假设当前应用的包名为”com.xibei“,当FirstActivity启动时，系统会为它创建一个单独的进程，进程名为”com.xibei.remote“。当SecondActivity启动是时，系统也会为它创建一个单独的进程，进程名"com.xibei.remote2" 。同时入口MainActivity，没有为它指定process属性，那么它运行在默认进程中，默认进程进程名是包名。

我们可以通过adb shell ps 来查看进程信息

对于进程的的命名：
-  简写命名 ———— :remote , ”:“的含义是指要在当前的进程名前面附加上当前的报名，这是一种简写的方法，对于FirstActivity来说，它完整的进程名为com.xibei:remote。 以":"开头的进程，属于当前应用的私有进程，其他应用的组件不可以和它跑在同一个进程中、
-  完整命名 ———— 观察SecondActiity的进程名为 com.xibei.remote2,当SecondActivity 启动时，系统也会为它创建一个单独的进程，进程名为”com.xibei.remote2“，为全局进程，其他应用通过ShareUID 方式可以和它跑在同一个进程中。

- MainActivity 并没有指定process 属性，它的进程名默认为包名

我们知道Android系统会为每一个应用分配一个唯一的UID，具有相同UID的应用才能共享数据。这里要说明的是，两个应用通过ShareUID跑在同一个进程中是有要求的，需要这两个应用有相同的ShareUID并且签名相同才可以。在这种情况下，他们可以互相访问对方的私有数据，比如data目录、组件信息等，不管他们是否跑在同一个进程中。当然如果他们跑在一个进程中，那么除了能共享data目录、组件信息，还可以共享内存数据，或者说他们看起来就像是一个应用的两个部分。

> 多进程模式的运行机制

如果用一句话来形容多进程，那就是，当应用开启了多进程之后，各种奇怪的现象都出现了。大部分人认为开启多进程是很简单的事情，只需要给四大组件指定android:process 属性即可。比如说在实际的产品开发中，可能会有多进程的需求，需要把某些组件放在单独的多进程中去运行，很多人都会觉得这不很简单吗？然后迅速地给那些组件指定android：process 属性，然后编译运行，发现”正常的运行起来了“。
真正的运行起来了吗？现在先不置可否，下面举个例子。然后引入本节的话题。还是本章高开始说的那个例子，其中FirstActivity通过指定的android:process 属性从而使其运行在一个独立的进程中，这里做了一些改动，我们创建了一个类，叫做UserManager，这个类中有一个public的静态成员变量，如下所示：

```
public class UserManager{
    public static int sUserId = 1;
}
```
然后在MainActivity的onCreate()中我们把这个sUserId 重新赋值为2，打印出这个静态变量的值后，再启动FirstActivity ，在FirstActivity中我们再打印一下sUserId的值。按照正常的逻辑，静态变量是可以在所有地方共享的，并且一处有修改处处都会同步，但是在打印日志的结果中发现，和预期的结果不一致。正常情况下FirstActivity中打印的sUserId的结果为2才对，但是从日志上看它竟然还是1，可是我们的确已经在MainActivity中把sUserId的值重新赋值为2.看到这里，大家应该明白了这就是对进程所带来的问题，多进程绝非仅仅指定一个android：process这么简单。

上述问题出现的原因是FirstActivity运行在一个单独的进程中，我们知道Android为每一个应用分配了独立的虚拟机，或者说为每个进程都分配一个独立的虚拟机，不同的虚拟机在内存分配上有不同的地址空间，这就导致在不同的虚拟机中访问同一个类的对象会产生多分副本。拿我们这个例子来说，在两个进程中，各自有一个UserManager ，并且这两个类是互不干扰的，在一个进程中修改sUserId的值只会影响当前进程，对其他进程不会造成影响，这样我们就可以理解为什么在MainActivity中修改了sUserId 的值，但在FirstActivity中的sUserId的值却没有发生改变这个现象。

所有运行在不用进程中的四大组件，只要他们之间需要通过内存来共享数据，都会共享失败，这也是多进程所带来的主要影响，正常情况下，四大组件中间不可能不通过一些中间层来共享数据，那么通过简单的指定进程名来开启多进程都会无法正确运行。当然，特殊情况下，某些组件之间不需要共享数据，这个时候可以直接指定android：process 属性来开启多进程，但是这种场景是不常见的，几乎所有情况都要共享数据：
 一般来说，使用多进程会造成如下几方面的问题：
- 静态成员和单例模式完全失效
- 线程同步机制完全失效
- SharedPreferences的可靠性下降
- Application 会多次创建

第一个问题在上面已经进行了分析，第二个问题的本质上和第一个问题是类似的，既然都不是一块内存了，那么不管是锁对象还是锁全局类都无法保证线程同步，因为不同进程锁的不是同一个对象。第三个问题是因为SharePreferences不支持两个进程同时去执行写操作，否则会导致一定几率数据丢失，这是因为SharePreferences底层是通过读写XML文件进来实现的，并发写显然是可能出问题的，甚至并发读写都有可能出问题。第四个问题也是显而易见的，当一个组件跑在一个新的进程中的时候，由于系统要在创建新的进程同时分配独立的虚拟机，所以这个过程其实就是启动一个应用的过程。因此，相当于系统又把这个应用重新启动了一遍，既然重新启动了，那么自然会创建新的Application。这个问题其实可以这么理解，运行在同一个进程中的组件是属于同一个虚拟机和同一个Application的。同理，运行在不同进程中的组件是属于两个不同的虚拟机和Application的。为了更清晰的展示这一点，下面我们来做一个测试，首先在Application的onCreate方法中打印当前进程的名字，onCreate应该执行三次并打印三次进程的名称不同的log

```
public class MyApplication extends Application{

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate(){
        super.onCreate();
        String processName = MyUtil。getProcessName（getApplicationContext(),Process.myPid()）;
        Log.e(TAG,""+processName);
    }
    
    }
```

运行后看一下log，通过log可以看出，Application执行了三次onCreate，并且每次的进程名称和进程id都不一样，他们的进程名和我们为Activity指定的android:process属性一致。这也就证实了在多进程模式中，不同进程的组件的确会拥有独立的虚拟机、Application以及内存空间，这回给实际的开发带来很多困扰，是尤其需要注意的。或者我们也可以这么理解同一个应用间的多进程：它就相当于两个不同的应用采用了SharedUID的模式，这样就能够更加直接理解多进程模式的本质

补充：分析了多进程所带来的问题，但是我们不能因为多进程有很多问题就不去正视他。为了解决这个问题，系统系统提供了很多跨进程通信方法，虽然说不能直接共享内存，但是通过跨进程通信我们还是可以实现数据交互。实现跨进程通信的方式很多，比如通过Intent来传递数据，共享文件和SharedPreFerences基于Binder的Messager和AIDL以及Socket等，但是为了更好地理解各个IPC方式，我们需要先熟悉一些基础概念，比如序列化相关的Serializable和Parceable接口，以及Binder的概念，熟悉完这些基础概念以后，再去理解各种IPC方式就比较简单了。

#### IPC基础概念介绍
主要介绍IPC中的一些基础概念，主要包括含三方面的内容：Serializable接口、Parcelable接口以及Binder，只有熟悉这三方面的内容后，我们才能更好的理解跨进程通信的各种方式。Seriaizable和Parcelable接口可以完成对象序列化过程，当我们需要通过Intent和Binder传输数据时就需要使用Parcelable和Serializable。还有的时候我们也需要把对象持久化到存储设备上或者通过网络传输给其他客户端，这个时候也需要使用Serializable来完成对象持久化，下面介绍如何使用Serializable完成对象的序列化。

> Serializable接口

Serizlizable是java所提供的一个序列化接口，它是一个空接口，为对象提供标准的序列化和反序列化操作。使用Serializable来实现序列化相当简单，只需要在类的声明中指定一个类似下面的标识即可自动实现默认的序列化过程。

`private static final long serialVersionUID = 8711368828010083044L`

在Android中也提供了新的序列化方式，那就是Parcelable接口，使用Parcelable来实现对象的序列号，其实过程稍微复杂一些，本节先介绍Serializable接口。上面提到，想让一个对象实现序列化，只需要这个类实现Serializable接口并声明一个SerialVersionUID即可，实际上，甚至这个serialVersionUID也不是必需的，我们不声明这个serialVersionUID同样也可以实现序列化，但是这将会对反序列过程产生影响，具体什么影响后面再介绍。User类就是一个实现了Serializable接口的类，它是可以被序列化和反序列化的：
```
public class User implements Serializable{

    private static fin long serialVersionUID = 519067123721295773L;
    public int userId;
    public String userName;
    public boolean isMale;
    ...
}

````

通过Serializable 方式来实现对象的序列化，实现起来非常简单，几乎所有工作都被系统自动完成了。如何进行对象的序列化和反序列化也非常简单，只需要采用ObjectOutputStream和ObjectInputStream即可轻松实现。下面举个简单的例子：
```
//序列化过程
User user = new User(0,"jake",true);
ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("cache.txt"));

out.writeObject(user);
out.close();

//反序列化过程
ObjectInputStream in = new ObjectInputStream(new FileInputStream("cache.txt"));
User newUser = (User)in.readObject();
in.close();
```

上述代码演示了采用Serializable方式序列化对象的典型过程，很简单，只需要把实现了Serializable接口的User对象写到文件中就可以快速恢复了，恢复后的对象newUser和user内容完全一样，但是两者并不是同一个对象。

刚开始提到，即使不指定serialVersionUID也可以实现序列化，那到底要不要指定呢？
如果指定的话，serialVersionUID后面那一长串数字又是什么含义呢？我们要明白，系统既然提供了这个serialVersionUID，那么它必须是有用的，这个serialVersionUID是用来辅助序列化和反序列化过程的，原则上序列化后的数据中的serialVersionUID只有和当前类的serialVersionUID相同才能够正常被序列化。serialVersionUID的详细工作机制是这样的：序列化的时候，系统会把当前类的serialVersionUID写入序列化的文件中（也可能是其他的中介），当反序列化的时候系统会去检测文件中的serialVersionUID，看它是否和当前类的serialVersionUID一致，如果一致就说明序列化的类的版本和当前类的版本是相同的，这个时候可以成功反序列化，否则就说明当前类和序列化的类相比发生了某些变换，比如成员变量的数量、类型可能发生了改变，这个时候是无法正常反序列化的，因此会报如下错误：

```
    java.io.InvalidClassExecption:Main;local class incompatible;stream classdesc serialVersionUID = 87113688010083044,local class serial-VersionUID = 8711368828010083043
```

一般来说，我们应该手动指定serialVersionUID的值，比如1L，也可以让编辑器根据当前类的结构自动去生成它的hash值，这样序列化和反序列化时两者的serialVersionUID是相同的，因此可以正常进行反序列化。如果不手动指定serialVersionUID的值，反序列化时当类有所改变，比如增加或者删除了某些成员变量，那么系统就会重新计算当前类的hash值并把它赋值给serialVersionUID，这个时候当前类的serialVersionUID就和序列化的数据中的serialVersionUID不一致，于是反序列化失败，程序就会crash。所以，我们可以明显感受到serialVersionUID的作用，当我们手动指定了它以后，就可以在最大限度地恢复数据，相反，如果不指定serialVersionUID的话，程序则会挂掉。当然我们还要考虑另外一种情况，如果类结构发成了非常规性改变，比如修改了类名，修改了成员变量的类名，这个时候尽管
serialVersionUID验证通过了，但是反序列化过程还是会失败，因为类结构有了毁灭性的改变，根本无法从老版本的数据中还原出一个新的类结构对象。

根据上面的分析，我们可以知道，给serialVersionUID指定1L或者采用编辑器根据当前类去生成的hash值，这两者并没有本质区别，效果完全一样。以下两点需要特别提一下，首先静态成员变量不属于对象，所以不会参与序列化过程；其次用transient关键字标记的成员变量不参与序列化过程。

另外，系统的默认序列化过程也是可以改变的，通过实现如下两个方法即可重写系统默认的序列化和反序列化过程：

```
private void writeObject(java.io.ObjectOutputStream out) throws IOException{

}

private void readObject(java.io.ObjectInputStream in) throws IOException,ClassNotFundException{
}
```

> Parcelable接口

Parcelable 也是一个接口，只要实现这个接口，一个类的对象就可以实现序列化并可以通过Intent和Binder传递。下面的示例是一个典型的用法。

```
package com.xibei.binderdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Xibei on 2019/4/24.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration:
 */
public class Animal implements Parcelable {

    private String name;

    public Animal(String name, int legsNumber) {
        this.name = name;
        this.legsNumber = legsNumber;
    }

    private int legsNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLegsNumber() {
        return legsNumber;
    }

    public void setLegsNumber(int legsNumber) {
        this.legsNumber = legsNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.legsNumber);
    }

    protected Animal(Parcel in) {
        this.name = in.readString();
        this.legsNumber = in.readInt();
    }

    public static final Parcelable.Creator<Animal> CREATOR = new Parcelable.Creator<Animal>() {
        @Override
        public Animal createFromParcel(Parcel source) {
            return new Animal(source);
        }

        @Override
        public Animal[] newArray(int size) {
            return new Animal[size];
        }
    };
}


```

这里先说一下Parcel ，Parcel 内部包装了可序列化的数据，可以在Binder中自由传输。从上述代码中可以看出，在序列化过程中需要实现的功能有序列化、反序列化和内容没描述。序列化功能由writeToParcel方法完成，最终通过Parcel中的一系列wirte方法完成。反序列化由CREATOR来完成，其内部标明了如何创建序列化对象和数值，并通过Parcel的一些列read方法来完成反序列化过程；内容描述功能由describeContents方法来完成，几乎在所有情况下这个方法都返回0，仅当当前对象中存在文件描述符时，此方法返回1。需要注意的是，在Animal(Parcel in)方法中，由于dog是另一个可序列化对象，所以说它的反序列化过程需要传递当前线程上下文类加载器，否则会报无法找到类的错误，下面展示详细的方法说明：


方法 | 功能| 标记位
---|---|---
createFromParcel(Parcle in)| 从序列化后的对象中创建原始数据| 
newArray(int size) | 创建指定长度的原始数据 | 
User(Parcel in) | 从序列化的对象中创建原始数据 | 
writeToParcel(Parcel out,int flags) | 将当前对象写入序列化结构中，其中flags标识有两种值:0或1（参见右侧标记位）。为1时标识当前对象需要作为返回值返回，不能立即释放资源，几乎所有情况都为0| PARCELABLE_WRITE_RETURN_VALUE
describeContents | 返回当前对象的内容描述。如果含有文件描述符，返回1（参见右侧标记位），否则返回0，几乎所有情况都返回0 | CONTENTS_FILE_DESCRIPTOR


系统已经为我们提供了实现Parcelable接口的类，他们都可以直接序列化，比如Intent、Bundle、Bitmap等，同时List 和Map也可以序列化，前提是他们里面的每个元素都是可序列化的。

既然Parcelable 和Serializable都能实现序列化并且都可用于Intent间数据传递，那么二者该如何选取呢？Serializable是Java中的序列化接口，其实用起来简单但是开销很大，序列化和反序列化过程需要大量的I/O操作。而Parcelable是Android中的序列化方式，因此更适合在Android平台上，它的缺点就是使用起来稍微麻烦点，但是它的效率很高，这个Android推荐的序列化方式，因此我们要首选Parcelable。Parcelable主要用在内存序列化上，通过Parcelable将对象序列化存储设备中，或者将对象序列化后通过网络传输也都是可以的，但是这个过程会稍显复杂，因此在这两种情况下建议大家使用Serializable。

>Binder

Binder 是Android中的一个类，它实现了IBinder接口。从IPC角度来说，Binder是Android中的一种跨进程通信方式，Binder还可以理解为一种虚拟物理设备，它的驱动设备是/dev/binder,该通信方式在Linux中没有；从Android Framwork角度来说，Binder是ServiceManager连接各种Manager（ActivityManager、WindowManager等等）和相应ManagerService的桥梁；从Android应用层来说，Binder是客户端和服务端进行通信的媒介，当bindService的时候，服务端会返回一个包含了服务端业务调用的Binder对象，同个这个Binder对象，客户端就可以获取服务端提供的服务或数据，这里的服务包括普通服务和基于AIDL的服务。

Android开发中，Binder主要用在Service中，包括AIDL和Messager，其中普通Service中的Binder不涉及进程间通信，所以较为简单，无法触及Binder的核心，而是Messager的底层其实是AIDL，所以这里选择用AIDL来分析Biner工作机制。为了分析Binder的工作机制，我们需要新建一个AIDL实例，SDK会自动为我们生产AIDL多对应的Binder类，然后我们就可以分析Binder工作过程。还是采用之前的例子

- Animal 类

```
package com.xibei.binderdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Xibei on 2019/5/7.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration:
 */
public class Dog implements Parcelable {

    public String name;

    public int legs;

    protected Dog(Parcel in) {
        name = in.readString();
        legs = in.readInt();
    }

    public static final Creator<Dog> CREATOR = new Creator<Dog>() {
        @Override
        public Dog createFromParcel(Parcel in) {
            return new Dog(in);
        }

        @Override
        public Dog[] newArray(int size) {
            return new Dog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(legs);
    }
}

```

- Animal类

```
package com.xibei.binderdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Xibei on 2019/4/24.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration:
 */
public class Animal implements Parcelable {

    private String name;

    private Dog dog;



    private int legsNumber;


    protected Animal(Parcel in) {
        name = in.readString();
        dog = in.readParcelable(Dog.class.getClassLoader());
        legsNumber = in.readInt();
    }

    public static final Creator<Animal> CREATOR = new Creator<Animal>() {
        @Override
        public Animal createFromParcel(Parcel in) {
            return new Animal(in);
        }

        @Override
        public Animal[] newArray(int size) {
            return new Animal[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(dog, flags);
        dest.writeInt(legsNumber);
        dest.writeParcelable(dog,0);
    }
}


```

- Animal.aidl

```
// Animal.aidl
package com.xibei.binderdemo;
parcelable Animal;

```

- AnimalManager类

```

// AnimalManager.aidl
package com.xibei.binderdemo;

import com.xibei.binderdemo.Animal;

interface AnimalManager{

    List<Animal> getAnimals();

    Animal getAnimal();

    int getAnimalCount();

    int getFirstAnimalLegs();

    void setAnimalName(String name);

    void setAnimalLegs(int legs);

}


```

上面三个文件中，Animal.java表示一个动物类，它实现了Parcelable接口。Animal.aidl 是Animal类在AIDL中的声明。AnimalManager 是我们定义的一个接口，里面的方法如上面代码所示。 我们可以看到，这三个类位于相同的包名下，但是在AnimalManager类中仍然要导入Animal类，这就是AIDL的特殊之处，下面我们看一下系统为AnimalManager生产的Binder类:

```

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/jiajie/Downloads/demo/android/BinderDemo/app/src/main/aidl/com/xibei/binderdemo/AnimalManager.aidl
 */
package com.xibei.binderdemo;

public interface AnimalManager extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements com.xibei.binderdemo.AnimalManager {
        private static final java.lang.String DESCRIPTOR = "com.xibei.binderdemo.AnimalManager";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.xibei.binderdemo.AnimalManager interface,
         * generating a proxy if needed.
         */
        public static com.xibei.binderdemo.AnimalManager asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof com.xibei.binderdemo.AnimalManager))) {
                return ((com.xibei.binderdemo.AnimalManager) iin);
            }
            return new com.xibei.binderdemo.AnimalManager.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            java.lang.String descriptor = DESCRIPTOR;
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(descriptor);
                    return true;
                }
                case TRANSACTION_getAnimals: {
                    data.enforceInterface(descriptor);
                    java.util.List<com.xibei.binderdemo.Animal> _result = this.getAnimals();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                }
                case TRANSACTION_getAnimal: {
                    data.enforceInterface(descriptor);
                    com.xibei.binderdemo.Animal _result = this.getAnimal();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getAnimalCount: {
                    data.enforceInterface(descriptor);
                    int _result = this.getAnimalCount();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_getFirstAnimalLegs: {
                    data.enforceInterface(descriptor);
                    int _result = this.getFirstAnimalLegs();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_setAnimalName: {
                    data.enforceInterface(descriptor);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.setAnimalName(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_setAnimalLegs: {
                    data.enforceInterface(descriptor);
                    int _arg0;
                    _arg0 = data.readInt();
                    this.setAnimalLegs(_arg0);
                    reply.writeNoException();
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

        private static class Proxy implements com.xibei.binderdemo.AnimalManager {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public java.util.List<com.xibei.binderdemo.Animal> getAnimals() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.util.List<com.xibei.binderdemo.Animal> _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getAnimals, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createTypedArrayList(com.xibei.binderdemo.Animal.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public com.xibei.binderdemo.Animal getAnimal() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                com.xibei.binderdemo.Animal _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getAnimal, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = com.xibei.binderdemo.Animal.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int getAnimalCount() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getAnimalCount, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int getFirstAnimalLegs() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getFirstAnimalLegs, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void setAnimalName(java.lang.String name) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(name);
                    mRemote.transact(Stub.TRANSACTION_setAnimalName, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void setAnimalLegs(int legs) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(legs);
                    mRemote.transact(Stub.TRANSACTION_setAnimalLegs, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_getAnimals = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_getAnimal = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_getAnimalCount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_getFirstAnimalLegs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_setAnimalName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_setAnimalLegs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    }

    public java.util.List<com.xibei.binderdemo.Animal> getAnimals() throws android.os.RemoteException;

    public com.xibei.binderdemo.Animal getAnimal() throws android.os.RemoteException;

    public int getAnimalCount() throws android.os.RemoteException;

    public int getFirstAnimalLegs() throws android.os.RemoteException;

    public void setAnimalName(java.lang.String name) throws android.os.RemoteException;

    public void setAnimalLegs(int legs) throws android.os.RemoteException;
}



```

上述代码是系统生成的，我们可以在两个地方看到它，一个是/gen，另一个是/app/generated/aidl_source_output_dir中。我们可以看到系统给我们生成的AnimalManager类继承了IInterface这个接口，同时它自己也还是个接口，所有可以在Binder中传输的接口需要继承IInterface接口。这个类刚刚开始看起来逻辑混乱，但是实际上还是很清晰的，通过它我们可以清楚的了解到Binder的工作机制。在最下面，声明了我们在AnimalManager.aidl中声明的方法，同时声明了对应的整型id,这里我们可以看到是递增的。这里id 的用处是标识在transact过程中客户端所请求的到底是哪个方法。接着声明了一个内部类Stub，这个Stub就是一个Binder类，当客户端和服务端都位于同一个进程时，方法调用不会走跨进程的transact过程，而当两者位于不同进程时，方法调用需要走transact过程，这个逻辑有Stup的内部代理类Proxy来完成。这么来看，AnimalManager这个接口确实很简单，但是我们也应该认识到，这个接口的核心就是它的背部类Stub和Stub的内部代理类Proxy，下面详细介绍针对这两个类的每个方法的含义。

- DESCRIPTOR
  Binder 的唯一标识，一般用当前Binder的类名表示，比如本例中的 "com.xibei.binderdemo.AnimalManager"
- asInterface(android.os.IBinder obj)
用于将服务端的Binder对象转换成客户端所需的AIDL接口类型的对象，这种转换过程是区分进程的，如果客户端和服务端位于同一进程，那么此方法返回的就是服务端的Stub对象本身，否则返回的是系统封装后的Stub.Proxy对象
- asBinder
此方法用于返回当前Binder对象
- onTransact
这个方法运行在服务端中的Binder线程池中，当客户端发起跨进程请求时，远程请求会通过系统底层封装后交由此方法来处理。该方法的原型为 public Boolean onTransact(int code,android.os.Parcel data,android.osParcel reply,int flags)。服务端通过code可以确定客户端所请求的目标方法是什么，接着从data中取出目标方法所需的参数(如果目标方法有返回值的话)，onTransact方法的执行过程就是这样，需要注意的是，如果此方法返回false，那么客户端的请求会失败，因此我们可以利用这个特性来做权限验证，毕竟我们也不希望随便一个进程都能远程调用我们的服务。

- Proxy#getAnimals
这个方法运行在客户端，当客户端远程调用此方法时，它的内部实现是这样的：首先创建该方法所需要的输入型Parcel 对象 _data,输出型Parcel 对象_reply和返回值对象List；然后把该方法的参数信息写入data(如果有参数的话)；接着调用transact方法来发起RPC（远程过程调用）请求，同时当前线程挂起；然后服务端的onTransact方法会被调用，直到RPC过程返回后，当前线程继续执行，并从_reply中取出RPC过程的返回结果；最后返回_reply中的数据。

 .......

 通过上面的分析，我们应该大致了解了Binder的工作机制，但是有两点还是需要额外说明一下：首先，当客户端发起远程请求是，由于当前线程会被挂起直至服务端进程返回数据，所以如果一个远程方法是很耗时的，那么不能在UI线程中发起此远程请求；其次，由于服务端的Binder方法运行在Binder的线程池中，所以Binder不管是否耗时都应该采用同步的方式去实现，因为它已经运行在一个线程中了。

 












