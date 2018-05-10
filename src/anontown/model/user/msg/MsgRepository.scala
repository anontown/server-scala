package anontown.model.user.msg

import java.sql.ResultSet
import anontown.model.SQL
import java.sql.Connection
import anontown.model.exceptions.NotFoundException
import anontown.model.user.User
import anontown.model.user.Attestation
import anontown.model.user.token.Token
import anontown.model.exceptions.MisdirectedRequestException
import java.sql.Statement
import java.sql.Types
import java.sql.Timestamp

object MsgRepository {
    private def toMsg(rs: ResultSet): List[Msg] = {
        val m= SQL.toObjList(rs, { rs =>
            Msg(rs.getInt(1),
                rs.getInt(2),
                rs.getString(3),
                rs.getString(4),
                rs.getTimestamp(5).toLocalDateTime())
        });
        rs.close();
        return m;
    }
}

class MsgRepository(private val con: Connection) {
    def add(msg: Msg): Msg = {
        val ps = this.con.prepareStatement("INSERT INTO msg VALUES(NULL,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        if (msg.receiver != 0){
            ps.setInt(1, msg.receiver);
        }else{
            ps.setNull(1, Types.INTEGER);
        }
        ps.setString(2, msg.text);
        ps.setString(3, msg.mdtext);
        ps.setTimestamp(4, Timestamp.valueOf(msg.date));

        ps.executeUpdate();

        val m= msg.withId(SQL.getAutoIncrement(ps));
        ps.close();
        return m;
    }

    def read(token: Token, id: Int): Msg = {
        if (id < 1) {
            throw new MisdirectedRequestException("idが1未満です");
        }

        val ps = this.con.prepareStatement("SELECT * FROM msg WHERE id=? AND (user=? OR user IS NULL)");
        ps.setInt(1, id);
        ps.setInt(2, token.user);

        val list = MsgRepository.toMsg(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("");
        }
        ps.close();
        return list(0);
    }

    def readAfter(token: Token, min: Int, limit: Int): List[Msg] = {
        if (min < 1) {
            throw new MisdirectedRequestException("minが1未満です");
        }
        if (limit < 1) {
            throw new MisdirectedRequestException("limitが1未満です");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM msg
WHERE (receiver=? OR receiver IS NULL) AND id>=?
ORDER BY id DESC LIMIT ?""");
        ps.setInt(1, token.user);
        ps.setInt(2, min);
        ps.setInt(3, limit);

        val m= MsgRepository.toMsg(ps.executeQuery());
        ps.close();
        return m;
    }

    def readBefore(token: Token, max: Int, limit: Int): List[Msg] = {
        if (max < 1) {
            throw new MisdirectedRequestException("maxが1未満です");
        }
        if (limit < 1) {
            throw new MisdirectedRequestException("limitが1未満です");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM msg
WHERE (receiver=? OR receiver IS NULL) AND id<=?
ORDER BY id DESC LIMIT ?""");
        ps.setInt(1, token.user);
        ps.setInt(2, max);
        ps.setInt(3, limit);

        val m= MsgRepository.toMsg(ps.executeQuery());
        ps.close();
        return m;
    }

    def readNew(token: Token,limit: Int): List[Msg] = {
        if (limit < 1) {
            throw new MisdirectedRequestException("limitが1未満です");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM msg
WHERE receiver=? OR receiver IS NULL
ORDER BY id DESC LIMIT ?""");
        ps.setInt(1, token.user);
        ps.setInt(2, limit);

        val m= MsgRepository.toMsg(ps.executeQuery());
        ps.close();
        return m;
    }
}