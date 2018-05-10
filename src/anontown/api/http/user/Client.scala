package anontown.api.http.user

import anontown.model.user.client.Client
import anontown.json.Json
import anontown.json.Json._
import anontown.api.http.HttpApiBase
import anontown.api.http.HttpMethod
import anontown.api.http.TokenFlag
import javax.servlet.annotation.WebServlet
import java.sql.Connection
import anontown.model.user.UserRepository
import anontown.model.user.client.ClientRepository
import anontown.model.user.token.Token

object ClientAPI{
    def toJson(client:Client):Json={
        return obj("id" -> client.id, "name" -> client.name, "url"->client.url);
    }
}

@WebServlet(name = "/user/client/create", urlPatterns = Array( "/user/client/create" ))
class Create extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("name","url","pass"), List("user")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val ar = new ClientRepository(con);

        val u = ur.read(ip("user"));
        val a=ar.add(Client(sp("name"),sp("url"),u,sp("pass")));

        return ClientAPI.toJson(a);
    }
}

@WebServlet(name = "/user/client/data", urlPatterns = Array( "/user/client/data" ))
class Name extends HttpApiBase(HttpMethod.POST, TokenFlag.UNNECESSARY, List("name","pass","url"), List("id","user")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur = new UserRepository(con);
        val ar = new ClientRepository(con);

        val u = ur.read(ip("user"));
        val a=ar.read(ip("id")).withData(sp("name"),sp("url"), u, sp("pass"));
        ar.save(a);

        return ClientAPI.toJson(a);
    }
}

@WebServlet(name = "/user/client/read/one", urlPatterns = Array( "/user/client/read/one" ))
class Read extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY, List(), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ar = new ClientRepository(con);

        val a=ar.read(ip("id"));

        return ClientAPI.toJson(a);
    }
}

@WebServlet(name = "/user/client/read/all", urlPatterns = Array( "/user/client/read/all" ))
class ReadAll extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY, List("pass"), List("user")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val ur=new UserRepository(con);
        val ar = new ClientRepository(con);

        val a=ar.readAll(ur.read(ip("user")), sp("pass"))

        return arr(a.map { x => ClientAPI.toJson(x) }:_*);
    }
}