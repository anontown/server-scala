package anontown.json

import java.time.LocalDate
import java.time.LocalDateTime

class Json private (val jString: String)

object Json {
    implicit def num(v: Long): Json = {
        return new Json(v.toString())
    }

    implicit def num(v: Double): Json = {
        return new Json(v.toString())
    }

    implicit def str(v: String): Json = {
        if (v == null) {
            return Json.NULL;
        }

        //http://qiita.com/sifue/items/039846cf8415efdc5c92
        val str = {
            val sb = new StringBuilder();
            for (i <- 0 until v.length()) {
                if(v.charAt(i).isLetterOrDigit){
                    sb.append(v.charAt(i))
                }else{
                    sb.append("\\u%04X".format(Character.codePointAt(v, i)));
                }
            }
            "\"" + sb.toString() + "\"";
        }

        return new Json(str)
    }

    implicit def bool(v: Boolean): Json = {
        return new Json(v.toString())
    }

    implicit def date(v: LocalDateTime): Json = {
        return Json.str(v.toString()+"+09:00");
    }

    def arr(v: Json*): Json = {
        if (v == null) {
            return Json.NULL;
        }

        val sl = v.map { x => if (x == null) "null" else x.jString };
        return new Json("[" + sl.mkString(",") + "]")
    }

    def obj(v: (String, Json)*): Json = {
        if (v == null) {
            return Json.NULL;
        }

        val sl = v.map { t => Json.str(t._1).jString + ":" + (if (t._2 == null) "null" else t._2.jString) };
        return new Json("{" + sl.mkString(",") + "}");
    }

    val NULL = new Json("null");
    val TRUE = new Json("true");
    val FALSE = new Json("false");
}
