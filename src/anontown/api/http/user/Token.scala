package anontown.api.http.user.token

import anontown.api.http.HttpApiBase
import anontown.api.http.TokenFlag
import anontown.model.user.token.Token
import java.sql.Connection
import anontown.json.Json
import anontown.json.Json._
import anontown.model.user.UserRepository
import anontown.model.user.token.TokenRepository
import anontown.api.http.HttpMethod
import anontown.model.user.storage.StorageRepository
import javax.servlet.annotation.WebServlet
import anontown.model.user.client.ClientRepository

object TokenAPI{
    def toJson(token:Token):Json={
        return obj("id" -> token.id, "key" -> token.token,"client"->token.client, "active"->token.active);
    }
}

@WebServlet(name = "/user/token/login", urlPatterns = Array( "/user/token/login" ))
class Login extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("pass"), List("user","client")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val tr = new TokenRepository(con);
        val sr = new StorageRepository(con);
        val ar=new ClientRepository(con);

        val u = ur.read(ip("user"));
        val t = Token(u, sp("pass"), ar.read(ip("client")));

        val t2 = tr.add(t);
        sr.add(t2._2);

        return TokenAPI.toJson(t2._1);
    }
}

@WebServlet(name = "/user/token/read/all", urlPatterns = Array( "/user/token/read/all" ))
class ReadAll extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("pass"), List("user")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val tr = new TokenRepository(con);

        val u = ur.read(ip("user"));
        val t=tr.readAll(u,sp("pass"));

        return arr(t.map { x => TokenAPI.toJson(x) }:_*);
    }
}

@WebServlet(name = "/user/token/disable", urlPatterns = Array( "/user/token/disable" ))
class Disable extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("pass"), List("user","id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val tr = new TokenRepository(con);

        val u = ur.read(ip("user"));
        val t=tr.read(ip("id"));
        val t2=t.disable(u,sp("pass"));

        tr.save(t2);

        return TokenAPI.toJson(t2);
    }
}

@WebServlet(name = "/user/token/enable", urlPatterns = Array( "/user/token/enable" ))
class Enable extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("pass"), List("user","id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val tr = new TokenRepository(con);

        val u = ur.read(ip("user"));
        val t=tr.read(ip("id"));
        val t2=t.enable(u,sp("pass"));

        tr.save(t2);

        return TokenAPI.toJson(t2);
    }
}

@WebServlet(name = "/user/token/change", urlPatterns = Array( "/user/token/change" ))
class Change extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("pass"), List("user","id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val tr = new TokenRepository(con);

        val u = ur.read(ip("user"));
        val t=tr.read(ip("id"));
        val t2=t.tokenChange(u,sp("pass"));

        tr.save(t2);

        return TokenAPI.toJson(t2);
    }
}

@WebServlet(name = "/user/token/data", urlPatterns = Array( "/user/token/data" ))
class Data extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val u = ur.read(token.get.user);

        return obj("id"->u.id,"sn"->u.sn);
    }
}