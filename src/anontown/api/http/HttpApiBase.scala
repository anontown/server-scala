package anontown.api.http

import java.sql.Connection

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import anontown.json.Json
import anontown.json.Json.obj
import anontown.json.Json.str
import anontown.model.SQL
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.exceptions.NchException
import anontown.model.exceptions.UnauthorizedException
import anontown.model.user.token.Token
import anontown.model.user.token.TokenRepository

object ReqParam {
    def getParamStr(req: HttpServletRequest, name: String): String = {
        val v = req.getParameter(name);
        if (v == null) throw new MisdirectedRequestException("パラメーターが足りません");
        return v;
    }

    def getParamInt(req: HttpServletRequest, name: String): Int = {
        val v = req.getParameter(name);
        if (v == null) throw new MisdirectedRequestException("パラメーターが足りません");
        val i = try {
            v.toInt;
        } catch {
            case e: NumberFormatException =>
                throw new MisdirectedRequestException("パラメーターが不正です");
        }
        return i;
    }
}

abstract class HttpApiBase(private val httpMethod: HttpMethod, private val tokenFlag: TokenFlag, private val strParams: List[String], private val intParams: List[String]) extends HttpServlet {
    override def doOptions(req: HttpServletRequest, res: HttpServletResponse){
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", httpMethod.name);
        res.setHeader("Access-Control-Allow-Headers", "X-Token");
        res.setHeader("Access-Control-Max-Age", (60*60*24*30*3).toString());
    }

    override def doPost(req: HttpServletRequest, res: HttpServletResponse) {
        if (this.httpMethod == HttpMethod.POST) {
            this.doProcess(req, res);
        }
    }

    override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
        if (this.httpMethod == HttpMethod.GET) {
            this.doProcess(req, res);
        }
    }

    private def doProcess(req: HttpServletRequest, res: HttpServletResponse) {
        req.setCharacterEncoding("UTF-8");

        res.setCharacterEncoding("UTF-8")
        res.setContentType("application/json")
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Headers", "X-Token");

        var con:Connection=null;
        try {
            con = SQL.getConnection();

            val sp = Map() ++ this.strParams.map { x => (x, ReqParam.getParamStr(req, x)) }
            val ip = Map() ++ this.intParams.map { x => (x, ReqParam.getParamInt(req, x)) }

            val token =
                if (this.tokenFlag == TokenFlag.REQUIRED ||
                    (this.tokenFlag == TokenFlag.ANY && req.getHeader("X-Token") != null)) {
                    if (req.getHeader("X-Token") == null) {
                        throw new UnauthorizedException("認証が必要です");
                    }
                    val author = req.getHeader("X-Token").split(",", 2).map { x => x.trim() };
                    if (author.length != 2) {
                        throw new MisdirectedRequestException("認証ヘッダが不正です");
                    }
                    val tokenId =
                        try {
                            author(0).toInt;
                        } catch {
                            case e: NumberFormatException => throw new MisdirectedRequestException("認証ヘッダが不正です");
                        }
                    Some(new TokenRepository(con).read(tokenId).auth(author(1)));
                } else {
                    None;
                }
            val jsonData = this.makeJson(token, con, sp, ip);
            con.commit();
            res.setStatus(200);
            res.getWriter().print(if(jsonData==null)"null"else jsonData.jString);
        } catch {
            case e: NchException =>
                con.rollback();
                res.setStatus(e.status);
                res.getWriter().print(obj("msg" -> e.getMessage()).jString);
            case e: Exception =>
                if(con!=null)con.rollback();
                res.setStatus(500);
                res.getWriter().print(obj("msg" -> "サーバー内部でエラーが発生しました").jString);
                e.printStackTrace()
                throw e;
        }finally {
            if(con!=null){
                con.close();
            }
        }
    }

    protected def makeJson(token: Option[Token],
        con: Connection,
        sp: Map[String, String],
        ip: Map[String, Int]): Json;
}


//認証の必要性
object TokenFlag {
    //任意
    case object ANY extends TokenFlag(0)

    //必須
    case object REQUIRED extends TokenFlag(1)

    //不要
    case object UNNECESSARY extends TokenFlag(2)

    val values = List(ANY, REQUIRED, UNNECESSARY)
}

sealed abstract class TokenFlag(val id: Int) {
    val name = toString
}

//認証の必要性
object HttpMethod {
    //任意
    case object GET extends HttpMethod(0)

    //必須
    case object POST extends HttpMethod(1)

    val values = List(GET, POST)
}

sealed abstract class HttpMethod(val id: Int) {
    val name = toString
}