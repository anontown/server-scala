package anontown.api.http.res

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
import anontown.api.http.HttpMethod
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServlet
import anontown.model.Config
import java.io.File
import anontown.model.user.User
import javax.servlet.annotation.WebServlet
import anontown.model.user.msg.MsgRepository

object ResAPI {
    def toJson(r: Res, user: Int): Json = {
        return obj("id" -> r.id,
            "topic" -> r.topic,
            "date" -> r.date,
            "name" -> (if (r.delete == DeleteFlag.ALIVE) r.name else "■削除"),
            "text" -> (if (r.delete == DeleteFlag.ALIVE) r.text else if (r.delete == DeleteFlag.SELF) "投稿主により削除されました" else if (r.delete == DeleteFlag.VOTE) "投票により削除されました" else "管理人により削除されました"),
            "mdtext"->(if (r.delete == DeleteFlag.ALIVE) r.mdtext else if (r.delete == DeleteFlag.SELF) "投稿主により削除されました" else if (r.delete == DeleteFlag.VOTE) "投票により削除されました" else "管理人により削除されました"),
            "reply" -> r.reply,
            "delete" -> r.delete.id,
            "vote" -> r.vote,
            "hash" -> r.hash,
            "replyCount" -> r.replyCount,
            "profile" -> r.profile,
            "user" -> (if(r.user==user)user else 0));
    }
}

@WebServlet(name = "/res/write", urlPatterns = Array( "/res/write" ))
class Write extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List("name", "text"), List("topic", "reply", "profile")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val tr = new TopicRepository(con);
        val ur = new UserRepository(con);
        val rr = new ResRepository(con);
        val pr = new ProfileRepository(con);

        val t = tr.read(ip("topic"));
        val u = ur.read(token.get.user);
        val p =
            if (ip("profile") == 0) {
                None;
            } else {
                Some(pr.read(ip("profile")));
            }
        val reply =
            if (ip("reply") == 0) {
                None;
            } else {
                Some(rr.read(ip("reply")));
            }

        val result = Res(t, u, token.get, sp("name"),None, sp("text"), reply, p);
        val r2=rr.add(result._1);
        ur.save(result._2);
        tr.save(result._3);

        if(reply.isDefined){
            rr.save(reply.get.replyAdd());
        }
        return ResAPI.toJson(r2,token.get.user);
    }
}

@WebServlet(name = "/res/read/one", urlPatterns = Array( "/res/read/one" ))
class Read extends HttpApiBase(HttpMethod.GET, TokenFlag.ANY, List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val rr = new ResRepository(con);
        val r = rr.read(ip("id"));

        return ResAPI.toJson(r, if (token.isDefined) token.get.user else 0);
    }
}

@WebServlet(name = "/res/read/after", urlPatterns = Array( "/res/read/after" ))
class ReadAfter extends HttpApiBase(HttpMethod.GET, TokenFlag.ANY, List(), List("topic", "min", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val t = new TopicRepository(con).read(ip("topic"));
        val rr = new ResRepository(con);
        val r = rr.readAfter(t, ip("min"), ip("limit"));

        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/read/before", urlPatterns = Array( "/res/read/before" ))
class ReadBefore extends HttpApiBase(HttpMethod.GET, TokenFlag.ANY,List(), List("topic", "max", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val t = new TopicRepository(con).read(ip("topic"));
        val rr = new ResRepository(con);
        val r = rr.readBefore(t, ip("max"), ip("limit"));

        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/read/new", urlPatterns = Array( "/res/read/new" ))
class ReadNew extends HttpApiBase(HttpMethod.GET, TokenFlag.ANY,List(), List("topic", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val t = new TopicRepository(con).read(ip("topic"));
        val rr = new ResRepository(con);
        val r = rr.readNew(t, ip("limit"));

        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/read/hash", urlPatterns = Array( "/res/read/hash" ))
class ReadHash extends HttpApiBase(HttpMethod.GET, TokenFlag.ANY, List("hash"), List("topic")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val t = new TopicRepository(con).read(ip("topic"));
        val rr = new ResRepository(con);
        val r = rr.readHash(t, sp("hash"));

        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/read/reply", urlPatterns = Array( "/res/read/reply" ))
class ReadReply extends HttpApiBase(HttpMethod.GET, TokenFlag.ANY, List(), List("res")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val rr = new ResRepository(con);
        val res = rr.read(ip("res"));

        val r = rr.readReply(res);
        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/read/notice/after", urlPatterns = Array( "/res/read/notice/after" ))
class ReadNoticeAfter extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List("min", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val rr = new ResRepository(con);

        val r = rr.readNoticeAfter(token.get, ip("min"), ip("limit"));
        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/read/notice/before", urlPatterns = Array( "/res/read/notice/before" ))
class ReadNoticeBefore extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List("max", "limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val rr = new ResRepository(con);

        val r = rr.readNoticeBefore(token.get, ip("max"), ip("limit"));
        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/read/notice/new", urlPatterns = Array( "/res/read/notice/new" ))
class ReadNoticeNew extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List("limit")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val rr = new ResRepository(con);

        val r = rr.readNoticeNew(token.get, ip("limit"));
        return arr(r.map { x => ResAPI.toJson(x, if (token.isDefined) token.get.user else 0) }: _*);
    }
}

@WebServlet(name = "/res/uv", urlPatterns = Array( "/res/uv" ))
class UV extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List(), List("res")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val rr = new ResRepository(con);
        val vr = new VoteRepository(con);

        val u = ur.read(token.get.user);
        val r = rr.read(ip("res"));

        val result = r.uv(ur.read(r.user),u, token.get);
        rr.save(result._1);
        vr.add(result._2);
        ur.save(result._3);

        return ResAPI.toJson(result._1, u.id);
    }
}

@WebServlet(name = "/res/dv", urlPatterns = Array( "/res/dv" ))
class DV extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List(), List("res")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val rr = new ResRepository(con);
        val vr = new VoteRepository(con);
        val mr=new MsgRepository(con);

        val u = ur.read(token.get.user);
        val r = rr.read(ip("res"));

        val result = r.dv(ur.read(r.user),u, token.get);
        rr.save(result._1);
        vr.add(result._2);
        ur.save(result._3);
        result._4.foreach(mr.add);

        return ResAPI.toJson(result._1, u.id);
    }
}

@WebServlet(name = "/res/delete", urlPatterns = Array( "/res/delete" ))
class Delete extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List(), List("res")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val rr = new ResRepository(con);
        val ur=new UserRepository(con);
        val r = rr.read(ip("res"));
        val r2 = r.delete(ur.read(r.user),token.get);

        rr.save(r2._1);
        ur.save(r2._2);

        return ResAPI.toJson(r2._1, token.get.user);
    }
}

