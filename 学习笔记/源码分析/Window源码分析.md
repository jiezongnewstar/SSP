### Window 源码分析
_现在一步步对这个类的源码进行分析，经过站在巨人的肩膀上的学习与借鉴，向他们学习那种阅读源码的思路与方式，同时结合自己实际情况，总结出：读源码的时候，从一个关键入口去切入，这样会更好的掌握执行流程，在入口之前，先要学会如何引用与实现，还有就是去读注释。以下的源码阅读过程中，将不再赘述注释部分。好了下面我们开始。_

---

这次没有切入点，先走一遍注释。代码有点长，这里不全部贴了。

> Window类的介绍
顶层窗口外观和行为策略的抽象类，一个类的实例应用作添加到窗口管理器。它提供了标准的UI策略，比如背景、标题、区域、默认秘钥处理等。 目前唯一做为它的实例的时PhoneWindow，当你需要的时候可以去实例化它。

- 常量说明

常量名 | 作用 | 数值
---|---|---
FEATURE_OPTIONS_PANEL | 标志“选项面板”功能。这是默认启用的|  0
FEATURE_NO_TITLE | 标记“无标题”功能，关闭顶部的标题的屏幕 | 1
FEATURE_PROGRESS | 标记进度指示器特性。从API 21开始不再支持 | 2
FEATURE_LEFT_ICON | 标志，用于在标题栏的左侧有一个图标 | 3
FEATURE_RIGHT_ICON | 标志，用于在标题栏的右侧有一个图标 | 4
FEATURE_INDETERMINATE_PROGRESS | 标记不确定的进度。从API 21开始不再支持 | 5
FEATURE_CONTEXT_MENU | 标记上下文菜单。这是默认启用的。|6
FEATURE_CUSTOM_TITLE | 标记自定义标题。您不能将此功能与其他标题功能相结合 | 7
FEATURE_ACTION_BAR | 标记以启用操作栏。对于某些设备，这是默认启用的。操作栏替换标题栏并提供替代位置用于某些设备上的屏幕菜单按钮。|8
FEATURE_ACTION_BAR_OVERLAY | 标志，用于请求覆盖窗口内容的操作栏|9
FEATURE_ACTION_MODE_OVERLAY | 标记，用于指定在不存在操作栏时操作模式的行为。如果启用了overlay，操作模式UI将允许覆盖现有窗口内容|10
FEATURE_SWIPE_TO_DISMISS | 标志，用于请求无装饰的窗口，该窗口通过从左侧滑动而被取消。|11
FEATURE_CONTENT_TRANSITIONS | 用于请求窗口内容更改的标志应使用，如果没有设置，将使用默认的TransitionManager|12
FEATURE_ACTIVITY_TRANSITIONS| 使活动能够通过发送或接收运行活动转换|13
FEATURE_MAX | 用作特征ID的最大值|FEATURE_ACTIVITY_TRANSITIONS=13
PROGRESS_VISIBILITY_ON | 用于将进度条的可见性设置为可见，不再使用FEATURE_PROGRESS和相关方法支持从API 21开始|-1
PROGRESS_VISIBILITY_OFF| 用于将进度条的可见性设置为GONE，不再使用FEATURE_PROGRESS和相关方法支持从API 21开始| -2
PROGRESS_INDETERMINATE_ON | 用于设置进度条的不确定模式| -3
PROGRESS_INDETERMINATE_OFF| 用于关闭进度条的不确定模式|-4
PROGRESS_START | (主)进度的起始值，不再使用FEATURE_PROGRESS和相关方法支持从API 21开始|0
PROGRESS_END | (主要)进度的结束值，不再使用FEATURE_PROGRESS和相关方法支持从API 21开始|10000
PROGRESS_SECONDARY_START| 第二个进度的最低可能值。不再使用FEATURE_PROGRESS和相关方法支持从API 21开始|20000
PROGRESS_SECONDARY_END| 第二个进度的最大可能值。不再使用FEATURE_PROGRESS和相关方法支持从API 21开始|30000
STATUS_BAR_BACKGROUND_TRANSITION_NAME | 自定义状态栏的时候使用|android:status:background
NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME | 自定义导航栏的时候使用|android:navigation:background
DEFAULT_FEATURES | 启用默认特性|(1 << FEATURE_OPTIONS_PANEL)或(1 << FEATURE_CONTEXT_MENU)
ID_ANDROID_CONTENT| XML布局文件中的主布局应该具有的ID|com.android.internal.R.id.content
PROPERTY_HARDWARE_UI | 硬件属性|persist.sys.ui.hw
DECOR_CAPTION_SHADE_AUTO | 用于让主题驱动窗口标题控件的颜色|0
DECOR_CAPTION_SHADE_LIGHT | 用于窗口标题上设置浅色控件 使用setDecorCaptionShade(int)|1
DECOR_CAPTION_SHADE_DARK |用于在窗口标题上设置深色控件，使用 setDecorCaptionShade (int)|2



