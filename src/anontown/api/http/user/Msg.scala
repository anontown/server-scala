package anontown.api.http.user.msg

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
import anontown.model.user.msg.Msg
import anontown.model.user.msg.MsgRepository
import anontown.api.http.HttpMethod
import javax.servlet.annotation.WebServlet

object MsgAPI {
    def toJson(m: Msg): Json = {
        return obj("id" -> m.id, "public" -> (m.id == 0), "text" -> m.text,"mdtext"->m.mdtext);
    }
}

@WebServlet(name = "/user/msg/read/one", urlPatterns = Array( "/user/msg/read/one" ))
class Read extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val mr = new MsgRepository(con);

        return MsgAPI.toJson(mr.read(token.get, ip("id")));
    }
}

@WebServlet(name = "/user/msg/read/after", urlPatterns = Array( "/user/msg/read/after" ))
class ReadAfter extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED,List(), List("min", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val mr = new MsgRepository(con);
        val m = mr.readAfter(token.get, ip("min"), ip("limit"));

        return arr(m.map { x => MsgAPI.toJson(x) }: _*);
    }
}

@WebServlet(name = "/user/msg/read/before", urlPatterns = Array( "/user/msg/read/before" ))
class ReadBefore extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List("max", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val mr = new MsgRepository(con);
        val m = mr.readBefore(token.get, ip("max"), ip("limit"));

        return arr(m.map { x => MsgAPI.toJson(x) }: _*);
    }
}

@WebServlet(name = "/user/msg/read/new", urlPatterns = Array( "/user/msg/read/new" ))
class ReadNew extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List("limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val mr = new MsgRepository(con);
        val m = mr.readNew(token.get,ip("limit"));

        return arr(m.map { x => MsgAPI.toJson(x) }: _*);
    }
}