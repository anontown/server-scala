package anontown.model.user.token

import java.sql.ResultSet
import anontown.model.SQL
import anontown.model.exceptions.NotFoundException
import java.sql.Connection
import anontown.model.user.storage.Storage
import anontown.model.exceptions.UnauthorizedException
import anontown.model.user.User
import java.sql.Statement

object TokenRepository {
    private def toToken(rs: ResultSet): List[Token] = {
        val t= SQL.toObjList(rs, { rs =>
            Token(rs.getInt(1),
                rs.getString(2),
                rs.getInt(3),
                rs.getInt(4),
                rs.getBoolean(5));
        });
        rs.close;
        return t;
    }
}

class TokenRepository(val con: Connection) {
    def add(token: Token): (Token, Storage) = {
        val ps = this.con.prepareStatement("INSERT INTO token VALUES(NULL,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, token.token);
        ps.setInt(2, token.client);
        ps.setInt(3, token.user);
        ps.setBoolean(4, token.active);

        ps.executeUpdate();

        val s = token.withId(SQL.getAutoIncrement(ps));
        ps.close;
        return (s, Storage(s));
    }

    def read(id: Int): Token = {
        val ps = this.con.prepareStatement("SELECT * FROM token WHERE id=?");
        ps.setInt(1, id);

        val list= TokenRepository.toToken(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("トークンが見つかりません");
        }
        ps.close();
        return list(0);
    }


    def readAll(user:User,pass:String):List[Token]={
        if (!user.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }
        val ps = this.con.prepareStatement("SELECT * FROM token WHERE user=?");
        ps.setInt(1, user.id);

        val list= TokenRepository.toToken(ps.executeQuery());
        ps.close();
        return list;
    }

    def save(token: Token) {
        val ps = this.con.prepareStatement("""UPDATE token
SET active=?
WHERE id=?""");
        ps.setBoolean(1, token.active);
        ps.setInt(2, token.id);
        ps.executeUpdate();
        ps.close;
    }
}