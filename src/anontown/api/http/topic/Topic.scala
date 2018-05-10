package anontown.api.http.topic

import anontown.api.http.HttpApiBase
import anontown.api.http.TokenFlag
import anontown.json.Json
import anontown.json.Json._
import anontown.model.user.token.Token
import java.sql.Connection
import anontown.model.user.UserRepository
import anontown.model.topic.Topic
import anontown.model.topic.TopicRepository
import anontown.model.topic.history.TopicHistoryRepository
import anontown.api.http.HttpMethod
import javax.servlet.annotation.WebServlet
import anontown.model.res.ResRepository
import anontown.model.topic.history.TopicHistory

object TopicAPI {
    def toJson(t: Topic): Json = {
        return obj("id" -> t.id,
            "title" -> t.title,
            "category" -> Topic.toCategoryString(t.category),
            "text" -> t.text,
            "mdtext"->t.mdtext,
            "update" -> t.updatetime,
            "date" -> t.date);
    }
}

@WebServlet(name = "/topic/write", urlPatterns = Array( "/topic/write" ))
class Write extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List("title", "category", "text"), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val user = ur.read(token.get.user);

        val r = Topic(sp("title"), Topic.toCategoryArray(sp("category")), sp("text"), user, token.get);
        val rr=new ResRepository(con);

        val tr = new TopicRepository(con);
        val thr = new TopicHistoryRepository(con);

        val t = tr.add(r._1, r._2,token.get);
        thr.add(t._1);
        rr.add(t._2);
        ur.save(t._3)

        return TopicAPI.toJson(t._4);
    }
}

@WebServlet(name = "/topic/read", urlPatterns = Array( "/topic/read" ))
class Read extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY, List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val tr = new TopicRepository(con);
        val t = tr.read(ip("id"));
        return TopicAPI.toJson(t);
    }
}

@WebServlet(name = "/topic/search", urlPatterns = Array( "/topic/search" ))
class Search extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY, List("category", "title"), List("page", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val tr = new TopicRepository(con);
        val tl = tr.search(Topic.toCategoryArray(sp("category")), sp("title"), ip("page"), ip("limit"));
        return arr(tl.map { x => TopicAPI.toJson(x) }: _*);
    }
}

@WebServlet(name = "/topic/data", urlPatterns = Array( "/topic/data" ))
class Data extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List("title", "category", "text"), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val tr = new TopicRepository(con);
        val rr=new ResRepository(con);
        val thr=new TopicHistoryRepository(con);

        val u=ur.read(token.get.id);

        val t = tr.read(ip("id"));

        val t2 = t.withData(u, token.get, sp("title"), Topic.toCategoryArray(sp("category")), sp("text"));

        thr.add(t2._1);
        rr.add(t2._2);
        ur.save(t2._3);
        tr.save(t2._4);

        return TopicAPI.toJson(t2._4);
    }
}
