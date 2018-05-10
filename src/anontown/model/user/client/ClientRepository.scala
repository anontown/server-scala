package anontown.model.user.client

import java.sql.ResultSet
import anontown.model.SQL
import java.sql.Connection
import java.sql.Statement
import anontown.model.user.User
import anontown.model.exceptions.UnauthorizedException
import anontown.model.exceptions.NotFoundException

object ClientRepository {
    private def toApp(rs: ResultSet): List[Client] = {
        val t= SQL.toObjList(rs, { rs =>
            Client(rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4))
        });
        rs.close;
        return t;
    }
}

class ClientRepository(val con: Connection) {
    def add(client: Client): Client = {
        val ps = this.con.prepareStatement("INSERT INTO client VALUES(NULL,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, client.name);
        ps.setString(2, client.url);
        ps.setInt(3, client.user);

        ps.executeUpdate();

        val a = client.withId(SQL.getAutoIncrement(ps));
        ps.close;
        return a;
    }

    def read(id: Int): Client = {
        val ps = this.con.prepareStatement("SELECT * FROM client WHERE id=?");
        ps.setInt(1, id);

        val list= ClientRepository.toApp(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("アプリが見つかりません");
        }
        ps.close();
        return list(0);
    }

    def readAll(user:User,pass:String):List[Client]={
        if (!user.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }
        val ps = this.con.prepareStatement("SELECT * FROM client WHERE user=?");
        ps.setInt(1, user.id);

        val list= ClientRepository.toApp(ps.executeQuery());
        ps.close();
        return list;
    }

    def save(client: Client) {
        val ps = this.con.prepareStatement("""UPDATE client
SET name=?,url=?
WHERE id=?""");
        ps.setString(1, client.name);
        ps.setString(2, client.url);
        ps.setInt(3, client.id);
        ps.executeUpdate();
        ps.close;
    }
}