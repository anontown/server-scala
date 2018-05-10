package anontown.api.http.user.tokenReq

import anontown.model.user.tokenReq.TokenReq
import anontown.json.Json
import anontown.json.Json._
import anontown.api.http.HttpApiBase
import anontown.api.http.TokenFlag
import anontown.api.http.HttpMethod
import javax.servlet.annotation.WebServlet
import anontown.model.user.token.Token
import java.sql.Connection
import anontown.model.user.tokenReq.TokenReqRepository
import anontown.api.http.user.token.TokenAPI
import anontown.model.user.token.TokenRepository


object TokenReqAPI {
    def toJson(t:TokenReq): Json = {
        return obj("id"->t.id,"key"->t.key);
    }
}

@WebServlet(name = "/user/token/req/create", urlPatterns = Array( "/user/token/req/create" ))
class Create extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List(), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val trr=new TokenReqRepository(con);
        val tr=TokenReq(token.get);

        return TokenReqAPI.toJson(trr.add(tr));
    }
}

@WebServlet(name = "/user/token/req/get", urlPatterns = Array( "/user/token/req/get" ))
class ReadAfter extends HttpApiBase(HttpMethod.GET, TokenFlag.UNNECESSARY,List("key"), List("id")) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val trr=new TokenReqRepository(con);
        val tr=trr.read(ip("id"),sp("key"));
        trr.delete(tr);
        return TokenAPI.toJson(new TokenRepository(con).read(tr.token));
    }
}