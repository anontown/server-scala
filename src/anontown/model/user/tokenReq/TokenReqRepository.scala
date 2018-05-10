package anontown.model.user.tokenReq

import java.sql.ResultSet
import anontown.model.SQL
import anontown.model.user.token.Token
import java.sql.Statement
import java.sql.Connection
import java.sql.Timestamp
import anontown.model.exceptions.NotFoundException

object TokenReqRepository {
    private def toTokenReq(rs: ResultSet): List[TokenReq] = {
        val t= SQL.toObjList(rs, { rs =>
            TokenReq(rs.getInt(1),
                rs.getString(2),
                rs.getInt(3),
                rs.getTimestamp(4).toLocalDateTime());
        });
        rs.close;
        return t;
    }
}

class TokenReqRepository(val con: Connection) {
    def add(tr:TokenReq):TokenReq={
        val ps = this.con.prepareStatement("INSERT INTO token_req VALUES(NULL,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, tr.key);
        ps.setInt(2, tr.token);
        ps.setTimestamp(3, Timestamp.valueOf(tr.expireDate));

        ps.executeUpdate();

        val t = tr.withId(SQL.getAutoIncrement(ps));
        ps.close;
        return t;
    }

    def read(id: Int,key:String): TokenReq = {
        val ps = this.con.prepareStatement("SELECT * FROM token_req WHERE id=? AND `key`=?");
        ps.setInt(1, id);
        ps.setString(2, key)

        val list= TokenReqRepository.toTokenReq(ps.executeQuery());
        if (list.length == 0||(!list(0).checkDate())) {
            throw new NotFoundException("トークンが見つかりません");
        }
        ps.close();
        return list(0);
    }

    def delete(tr:TokenReq){
        val ps = this.con.prepareStatement("DELETE FROM token_req WHERE id=?");
        ps.setInt(1, tr.id);

        ps.executeUpdate();
        ps.close();
    }
}