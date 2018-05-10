package anontown.model.user.storage

import java.sql.Connection
import java.sql.ResultSet
import anontown.model.SQL
import anontown.model.exceptions.NotFoundException
import java.sql.SQLException
import anontown.model.user.token.Token
import anontown.model.exceptions.ConflictException
import java.sql.Statement

object StorageRepository {
    private def toStorage(rs: ResultSet): List[Storage] = {
        val s= SQL.toObjList(rs, { rs =>
            Storage(rs.getInt(1),
                rs.getInt(2),
                rs.getInt(3),
                rs.getString(4));
        });
        rs.close();
        return s;
    }
}

class StorageRepository(private val con: Connection) {
    def add(storage: Storage): Storage = {
        val ps = this.con.prepareStatement("INSERT INTO storage VALUES(NULL,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, storage.user);
        ps.setInt(2, storage.token);
        ps.setString(3, storage.value);

        ps.executeUpdate();

        val s= storage.withId(SQL.getAutoIncrement(ps));
        ps.close();
        return s;
    }

    def save(storage: Storage) {
        val ps = this.con.prepareStatement("""UPDATE storage
SET value=?
WHERE id=?""");
        ps.setString(1, storage.value);
        ps.setInt(2, storage.id);

        ps.executeUpdate();
        ps.close();
    }

    def read(token: Token): Storage = {
        val ps = this.con.prepareStatement("SELECT * FROM storage WHERE token=?");
        ps.setInt(1, token.id);

        val list = StorageRepository.toStorage(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("");
        }
        ps.close();
        return list(0);
    }
}