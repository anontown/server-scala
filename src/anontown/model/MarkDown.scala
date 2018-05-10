package anontown.model

import java.net.URL
import javax.script.ScriptEngineManager
import javax.script.Invocable
import java.io._

object MarkDown{
    private val script={
        //スクリプト文字列DL
        val url=new URL("https://raw.githubusercontent.com/chjj/marked/master/marked.min.js");
        //var url=new URL("https://raw.githubusercontent.com/chjj/marked/master/lib/marked.js");
        val br= new BufferedReader(new InputStreamReader(url.openStream()));
        val sb=new StringBuffer();
        var s = br.readLine();
        while (s != null){
            sb.append(s + "\n");
            s = br.readLine();
        }

        val str = sb.toString();

        //スクリプトエンジン作成
        val se = new ScriptEngineManager().getEngineByName("JavaScript");
        //関数登録
        se.eval(str);
        se.eval("marked.setOptions({sanitize:true});");

        se.asInstanceOf[Invocable];
    };

    def apply(str:String):String={
        this.script.synchronized{
            return this.script.invokeFunction("marked", str).asInstanceOf[String];
        }
    }
}