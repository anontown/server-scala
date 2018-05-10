package anontown.api.http.topic.history

import anontown.model.topic.history.TopicHistory
import anontown.json.Json
import anontown.json.Json._
import anontown.api.http.HttpApiBase
import anontown.api.http.TokenFlag
import anontown.model.user.token.Token
import java.sql.Connection
import anontown.model.topic.history.TopicHistoryRepository
import anontown.api.http.HttpMethod
import javax.servlet.annotation.WebServlet

object TopicHistoryAPI {
    def toJson(t: TopicHistory): Json = {
        return obj("id" -> t.id,
            "title" -> t.title,
            "category" -> t.category,
            "text" -> t.text,
            "mdtext"->t.mdtext,
            "date" -> t.date,
            "hash" -> t.hash,
            "topic" -> t.topic);
    }
}

@WebServlet(name = "/topic/history/read/one", urlPatterns = Array( "/topic/history/read/one" ))
class Read extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY, List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val th = new TopicHistoryRepository(con).read(ip("id"));
        return TopicHistoryAPI.toJson(th);
    }
}

@WebServlet(name = "/topic/history/read/all", urlPatterns = Array( "/topic/history/read/all" ))
class ReadAll extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY, List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val th = new TopicHistoryRepository(con).readAll(ip("id"));
        return arr(th.map { x => TopicHistoryAPI.toJson(x) }: _*);
    }
}