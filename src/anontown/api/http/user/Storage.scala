package anontown.api.http.user.storage

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
import anontown.model.user.storage.StorageRepository
import anontown.model.user.storage.Storage
import anontown.api.http.HttpMethod
import javax.servlet.annotation.WebServlet

@WebServlet(name = "/user/storage/write", urlPatterns = Array( "/user/storage/write" ))
class Write extends HttpApiBase(HttpMethod.POST, TokenFlag.REQUIRED, List("value"), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val sr = new StorageRepository(con);
        val ur = new UserRepository(con);

        val u = ur.read(token.get.user);
        val s = sr.read(token.get);
        val s2 = s.withValue(sp("value"));

        sr.save(s2);

        return null;
    }
}

@WebServlet(name = "/user/storage/read", urlPatterns = Array( "/user/storage/read" ))
class Read extends HttpApiBase(HttpMethod.GET, TokenFlag.REQUIRED, List(), List()) {
    override def makeJson(token: Option[Token], con: Connection, sp: Map[String, String], ip: Map[String, Int]): Json = {
        val sr = new StorageRepository(con);

        val s = sr.read(token.get);

        return s.value;
    }
}