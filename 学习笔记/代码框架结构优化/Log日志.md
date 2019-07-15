### log日志

```
import android.util.Log;

/**
 * Created by Xibei on 2019/7/15.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration: 调试日志类
 */
public class XBLogger{

    private static final boolean DEBUG = true;       //全局标记位
    private static XBLogger instance;
    private LOG_TYPE type;                           //日志级别
    private String tag;                              //日志筛选
    private String msg;                              //日志内容

    private XBLogger(){

    }

    public void showLog(){

        if (!DEBUG){
            return;
        }

        if (instance == null){
            instance = new XBLogger();
        }

        if (tag == null){
            tag = "xibei - 没有初始化，兄弟";
        }

        if (type == null){
            type = LOG_TYPE.E;
        }

        if (msg == null){
            msg = "xibei - the log class did not be declare!";
        }

        switch (type){
            case E:
                Log.e(tag,msg);
              break;
            case D:
                Log.d(tag,msg);
                break;
            case I:
                Log.i(tag,msg);
                break;
            case V:
                Log.v(tag,msg);
                break;
            case W:
                Log.w(tag,msg);
                break;
        }
    }

    public static XBLogger getInstance(){
        if (instance == null){
            instance = new XBLogger();
        }
        return instance;
    }

    public XBLogger logType(LOG_TYPE log_type){
        type = log_type;
        if (instance == null){
            instance = new XBLogger();
        }
        return instance;
    }


    public XBLogger tag(String tag){
        this.tag = tag;
        if (instance == null){
            instance = new XBLogger();
        }
        return instance;
    }

    public XBLogger msg(String msg){
        this.msg = msg;
        if (instance == null){
            instance = new XBLogger();
        }
        return instance;
    }


    public enum LOG_TYPE{
        E,      //Erro
        D,      //Debug
        I,      //Info
        V,      //verbose
        W,      //Warn
    }

}


```