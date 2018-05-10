package anontown.api.http.user

import anontown.api.http.HttpApiBase
import anontown.api.http.TokenFlag
import anontown.model.user.token.Token
import java.sql.Connection
import anontown.json.Json
import anontown.json.Json._
import anontown.model.user.UserRepository
import anontown.model.user.User
import anontown.api.http.HttpMethod
import javax.servlet.annotation.WebServlet

@WebServlet(name = "/user/signin", urlPatterns = Array( "/user/signin" ))
class Signin extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("sn", "pass"), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val u = ur.add(User(sp("sn"), sp("pass")));

        return u.id;
    }
}

@WebServlet(name = "/user/id", urlPatterns = Array( "/user/id" ))
class Id extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY, List("sn"), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        return ur.getId(sp("sn"));
    }
}

@WebServlet(name = "/user/pass", urlPatterns = Array( "/user/pass" ))
class Pass extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("pass", "new-pass"), List("user")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val u = ur.read(ip("user"));
        val u2 = u.withPass(sp("pass"), sp("new-pass"));
        ur.save(u2);

        return null;
    }
}

@WebServlet(name = "/user/sn", urlPatterns = Array( "/user/sn" ))
class Sn extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("pass", "sn"), List("user")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val u = ur.read(ip("user"));
        val u2 = u.withSn(sp("pass"), sp("sn"));
        ur.save(u2);

        return null;
    }
}

