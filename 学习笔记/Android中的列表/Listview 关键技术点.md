## ListView 关键技术点整理

    在这个RecyclerView到处横行的年代，来搞一篇ListView的总结，怕是飘了。但是实际使用过程中，不仅仅是小公司，很多大厂的很多亿万级用户的App，仍然在使用。哪个好哪个不好的问题，没有答案，还是看开发者再怎么来玩，玩的好可以单车变摩托。我们可以通过UIAutomatior 来查看主流大厂的App，并且去深入体验一下他们的各个细节。
    
    写这篇文章的目的是为了更多的去掌握它的用法和技术埋点，同时做个笔记，方便以后用，接下来将通过一下几个部分来进行总结。


### 基本用法
- 布局文件中加入 `<ListView></ListView>` 标签
- 创建XXXAdapter 
- 在Activity/Fragment中初始化 数据、XXXAdapter、ListView
- 将XXXAdapter设置给ListView

### XXXAdapter详解
- int getCount();
    
    获取数据源总数
- Object getItem(int position)

    根据posiition获取Item对应数据
- long getItemId(int position) 

    根据position获取Item对应ID
- View getView(int position, View convertView, ViewGroup parent)

    获取Item对应视图并加载
- int getViewTypeCount()

    获取View类型总数
- int getItemViewType(int position)

    根据position获取对应View类型
- boolean hasStableIds()


### 实际操作

- 多布局类型加载

     多布局类型加载，通常在XXXAdapter中，声明 int型 的ViewType，从0开始，因为整个ViewType是按照数组下标去取的：

    ```
            //声明view类型数量
            private final int VIEW_TYPE_COUNT = 3;

            //声明具体类型
            private interface VIEW_TYPE{
                int text = 0;
                int image = 1;
                int redpacket = 2;
            }

            //返回View类型总数
            @Override
                public int getViewTypeCount() {
                    return VIEW_TYPE_COUNT;
                }
            
            //根据具体类型返回指定Type
            @Override
                public int getItemViewType(int position) {
                    return ((XXXBean)getItem(position)).getType();
                                 
                }

            //根据ViewType加载对应布局
            
            @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                
                    switch (getItemViewType(position)){
                        case VIEW_TYPE.text:
                            convertView = LayoutInflater.from(mContext).inflate(text_resid,null);
                            break;
                        case VIEW_TYPE.image:
                            convertView = LayoutInflater.from(mContext).inflate(image_resid,null);
                            break;
                        case VIEW_TYPE.redpacket:
                            convertView = LayoutInflater.from(mContext).inflate(redpacket_resid,null);
                             break;
                    }

                    return convertView;
                }
    
    ```
- 复用机制

    1. 我们注意到，在getView()方法中，有个参数叫做convertView，这个参数就是用来缓存itemView的。整个ListView的缓存流程：根据ViewType的数量来创建相应的recycleBin数组，在可视范围外的View将被回收到recycleBin中，当可视范围外的View进入可视范围内，先去recycleBin中查找有无匹配类型的缓存，如果有则返回convertView，如果没有则会新建该类型convertView。

    2. 第1步仅仅解决了itemView的缓存问题，我们知道，一个itemView有的时候会有很多的组合控件，这是需要大量的findViewById()操作，当基数很大的数据需要加载时，这里是很大的性能开销，所以我们使用ViewHodler来减少findViewById()的操作。


    ```

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler viewHodler;
            
            if (convertView == null){
                
                convertView = LayoutInflater.from(mContext).inflate(resid,null);
                
                TextView textView = convertView.findViewById(textid);
                ImageView imageView = convertView.findViewById(imageid);
                
               viewHodler = new ViewHodler(textView,imageView);
               
               convertView.setTag(viewHodler);
            }else {
                viewHodler = (ViewHodler) convertView.getTag();
            }
            
            //此处省略赋值操作

            return convertView;
        }

        class ViewHodler{
            TextView textView;
            ImageView imageView;

            public ViewHodler(TextView textView, ImageView imageView) {
                this.textView = textView;
                this.imageView = imageView;
            }

            public TextView getTextView() {
                return textView;
            }

            public ImageView getImageView() {
                return imageView;
            }
        }


    ```

- 刷新
    1. 步骤
        - 修改数据源
        - 调用 adapter.notifyDataSetChanged()
    2. 实现原理

        在ListView.setAdapter()的时候，ListView会通过AdapterDataSetObserver.registerDataSetObserve()来向Adapter的DataSetObservable注册监听，当数据改变的时候，我们调用notifyDataSetChanged，Adapter的DataSetObservable会调用notifyChanged 通知ListView的AdapterDataSetObserver，AdapterDataSetObserver调用requestLayout()方法进行界面刷新

- 局部刷新

    获取当前item的index：position - listView 第一个可见的position

    ```
        private void indexRefresh(ListView listview,int position){
            if(position >= listview.getFirstVisiblePosition()
            && position <= listview.getLastVisiblePosition()){
                int childIndex = position - listview.getFirstVisiblePosition();

                View child = listView.getChildAt(childIndex);

                if(child.getTag() instanceof ViewHolder){
                    ((ViewHolder)child.getTag()).refresh(datas.get(position).getMSG())
                }
            }

        }

        public static class ViewHolder{
            TextView textView;

            public ViewHodler(TextView textView){
                this.textView  = textView;
            }

            public void refresh(String msg){
                this.textView.setText(msg);
            }
        }

    ```

### 关键API详解

- HeaderView、FooterView、EmptyView

    1. HeaderView

        这里注意在4.3版本前，需要先添加headerView再声明Adapter

        ```

            headerView = LayoutInflater
                        .from(MainActivity.this)
                        .inflate(headresid,listview,false);
            listview.addHeaderView(headerView);

            listview.removeHeaderView(headerView);
            
        ```
    2. FooterView
        ```

            footerView = LayoutInflater
                        .from(MainActivity.this)
                        .inflate(footerresid,listview,false);
            listView.addFooterView(footView);

            listview.removeFooterView(footereView);
        ```
    3. EmptyView

        ```
            View emptyView = findViewById(R.id.empey_view);

            listView.setEmptyView(emptyView);

        ```

- OnItemClickListener

    ```
        public interface OnItemClickListener{
            void onItemClick(Adapter<?> parent View,int position ,long id);
        }

        public interface OnItemLongClickListener{
            boolean onItemLongClick(AdapterView<?> parent,View view,int position,long id);
        }

    ```

    - 取点击条目数据

            我们通常在列表条目点击的时候，会去数据源列表中拿数据，或者去修改数据。操作但条目数据的时候，是通过下标去拿的，但是当添加HeaderView 或者 FooterView 之后，listview 会自动将原来的Adapter 构建成HeaderViewListAdapter ，所以通过 datas.get(position)或者adapter.getItem(position)是不能够正确的拿到对应数据，并且可能出现数组下标越界的问题。这时，我们需要使用parent.getItemAtPostion(position)来拿数据。
    - item条目中含有Button

            当我们的item布局中添加Button控件，此时的OnItemClick()回调是不会走的。有一下几种方法来解决这个问题
            
        - 将Button的focusable属性设置为fasle
        - 在item根布局添加属性 `android:descendantFocusability="blockDescendants"`
        - 大于5.0的系统，item根布局添加`android:touchscreenBlocksFocus="true"`

    - OnScrollListener

        ```
            public interface OnscrollListener{
                public void onScrollStateChanged(AbsListView view,int scrollState);
                public void onscroll(AbsLi stView view,int firstVisibleItem, int visibleItemCount, int totalItemCount)

            }

            //scrollState 对应常量
            //停止滑动
            public static int SCROLL_STATE_IDLE = 0;
            //随手指滑动
            public static int SCROLL_STATE_TOUCH_SCROLL = 1;
            //脱离手指惯性滑动 
            public static int SCROLL_STATE_FLING = 2; 

        ```

        - 根据 SCROLL_STATE_FLING 和 SCROLL_STATE_IDLE 可以对列表进行优化

                在滑动过程中，会回调OnScrollListener方法，通过scrollState返回值，来判断滑动状态，在滑动过程中FLING的时候，我们可以来禁止异步操作，当滑动完毕的时候（IDLE ），来恢复异步操作。

        - 根据firstVisibleItem 和visibleItemCount 以及 totalItemCount 判断列表是否被滑到底部。

                当根据firstVisibleItem + 和visibleItemCount ==  totalItemCount 时，则表示列表已经被滑到底部，此时可以做加载更多的操作


    - setSelection

        - setSelecton(int position)

                指定第一个可见的item的位置

        - setSelectionFromTop(int position, int y)

                指定第一个可见的item的位置，并设置其距离上部分的像素值


         
    - ListView标签常用属性

            我们根据父类的属性将这些属性分为三类：

        - View

            - scrollbars

                    对应属性为 none：隐藏scrollbar ，vertical/hor ：在滑动过程中展示scrollbar
            - fadeScrollbars
                    
                    属性为true的时候，滑动过程中展示scrollbar，滑动结束后消失，属性为false的时候scrollbar始终展示
            - overScrollMode

                    当属性设置为“always”的时候，列表滑动到顶部或者底部的时候，还可以拉伸，“never”则不能拉，“ifCOntentScrolls”如果内容超出一屏，则可以拉伸，如果内容小于一屏，则不可一拉伸
            - requiresFadingEdge

                    水平或垂直渐变
            - fadingEdgeLength

                    渐变的高度或者宽度
        - AbsListView
            
            - cacheColorHint

                    设置渐变的颜色
            - scrollingCache

                    true整个列表在滑动过程中颜色与渐变色一样
            - listSelector

                    item点击的效果，需要自定义selector
            - drawSelectorOnTop

                    true：item点击的时候，会用颜色覆盖
            - fastScrollEnabled

                    属性为true的时候，滑动过程中，可以滑动scrollbar来快速滑动
            - fastScrollAlwaysVisible

                    属性为true的时候，快速滑动scrollbar一直展示
            - stackFromBottom

                    true:进步列表，展示最底部，并且从底部到顶部布局
            - transcriptMode

                    alwaysScroll:总会滚头起始位置
                    normal：正常滚动
                    none：不滚动
            


        - ListView

            - divider

                    分割线
            - dividerHeight

                    分割线的高度
            - footerDividersEnable

                    底部分割线是否展示
            - headerDividersEnable

                    顶部分割线是否展示
            - overScrollFooter

                    底部拉伸的背景色
            - overScrollHeader

                    顶部的拉伸背景色




    


    