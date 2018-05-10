package anontown.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date
import java.sql.Timestamp
import com.sun.org.glassfish.external.statistics.annotations.Reset
import java.util.Calendar

object SQL {
    Class.forName("com.mysql.jdbc.Driver");

    def getConnection(): Connection = {
        val con = DriverManager.getConnection(
            Config.DB_URL+"?useUnicode=true&characterEncoding=utf8",
            Config.DB_USER,
            Config.DB_PASS);
        con.setAutoCommit(false);
        return con;
    }


    //自動的に生成された列(AUTO_INCREMENT)の値を取得
    def getAutoIncrement(s: Statement): Int = {
        val rs = s.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    def toSQLDate_(localDateTime: LocalDateTime): Timestamp = {
        //LocalDateTime→util.Date
        val date =
            {
                val zone = ZoneId.systemDefault();
                val zonedDateTime = ZonedDateTime.of(localDateTime, zone);

                val instant = zonedDateTime.toInstant();
                Date.from(instant);
            }

        //util.date→sql.date
        return new Timestamp(date.getTime());
    }

    def fromSQLDate_(sdate: Timestamp): LocalDateTime = {
        //sdate→udate
        val date=new Date(sdate.getTime());

        val instant = date.toInstant();
        val zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    //#でエスケープ
    def likeEsc(x: String): String = {
        return x.replaceAllLiterally("#", "##").
            replaceAllLiterally("%", "#%").
            replaceAllLiterally("_", "#_");
    }

    //ResultSetをObjectListに変換します
    def toObjList[T](rs: ResultSet, func: (ResultSet) => T): List[T] = {
        return Iterator.continually { rs }.takeWhile { _.next() }.map { func }.toList;
    }
}
