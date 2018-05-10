package anontown.api.http.user.profile

import anontown.model.res.Res
import anontown.model.DeleteFlag
import anontown.json.Json
import anontown.json.Json._
import anontown.api.http.TokenFlag
import anontown.api.http.HttpApiBase
import anontown.model.user.token.Token
import java.sql.Connection
import anontown.model.topic.TopicRepository
import anontown.model.user.UserRepository
import anontown.model.res.ResRepository
import anontown.model.user.profile.ProfileRepository
import anontown.model.user.vote.VoteRepository
import anontown.model.user.profile.Profile
import anontown.api.http.HttpMethod
import javax.servlet.annotation.WebServlet

object ProfileAPI {
    def toJson(p: Profile): Json = {
        return obj("id" -> p.id, "sid" -> p.sid, "name" -> (if (p.active) p.name else "削除"), "text" -> (if (p.active) p.text else "削除"),"mdtext"->(if (p.active) p.mdtext else "削除"), "active" -> p.active);
    }
}

@WebServlet(name = "/user/profile/add", urlPatterns = Array( "/user/profile/add" ))
class Add extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List("sid", "name", "text"), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val pr = new ProfileRepository(con);

        val p = Profile(token.get, sp("sid"), sp("name"), sp("text"));
        val p2 = pr.add(p);

        return ProfileAPI.toJson(p2);
    }
}

@WebServlet(name = "/user/profile/read/one", urlPatterns = Array( "/user/profile/read/one" ))
class Read extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY,List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val pr = new ProfileRepository(con);

        val p = pr.read(ip("id"));

        return ProfileAPI.toJson(p);
    }
}

@WebServlet(name = "/user/profile/read/all", urlPatterns = Array( "/user/profile/read/all" ))
class ReadAll extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val pr = new ProfileRepository(con);

        val p = pr.readAll(token.get.user);

        return arr(p.map { x => ProfileAPI.toJson(x) }: _*);
    }
}

@WebServlet(name = "/user/profile/data", urlPatterns = Array( "/user/profile/data" ))
class Data extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List("name", "text"), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val pr = new ProfileRepository(con);
        val p = pr.read(ip("id"));
        val p2 = p.withData(token.get,sp("name"), sp("text"));
        pr.save(p2);

        return ProfileAPI.toJson(p2);
    }
}

@WebServlet(name = "/user/profile/delete", urlPatterns = Array( "/user/profile/delete" ))
class Delete extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val pr = new ProfileRepository(con);
        val p = pr.read(ip("id"));
        val p2 = p.delete(token.get);
        pr.save(p2);

        return ProfileAPI.toJson(p2);
    }
}