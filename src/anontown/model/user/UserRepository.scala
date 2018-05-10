package anontown.model.user

import java.sql.ResultSet
import anontown.model.SQL
import anontown.model.user.storage.Storage
import anontown.model.user.vote.Vote
import anontown.model.user.profile.Profile
import anontown.model.exceptions.NotFoundException
import java.sql.Connection
import anontown.model.user.msg.MsgRepository
import anontown.model.user.profile.ProfileRepository
import anontown.model.user.storage.StorageRepository
import anontown.model.user.vote.VoteRepository
import anontown.model.exceptions.MisdirectedRequestException
import java.sql.Statement
import java.sql.Timestamp

object UserRepository {
    def toUser(rs: ResultSet): User = {
        val list = SQL.toObjList(rs, { rs =>
            User(rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4),
                rs.getTimestamp(5).toLocalDateTime(),
                rs.getTimestamp(6).toLocalDateTime());
        });
        if (list.length == 0) {
            throw new NotFoundException("ユーザーが見つかりません");
        }
        rs.close();
        return list(0);
    }
}

class UserRepository(private val con: Connection) {
    def add(user: User): User = {
        val ps = this.con.prepareStatement("INSERT INTO user VALUES(NULL,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, user.sn);
        ps.setString(2, user.pass);
        ps.setInt(3, user.lv);
        ps.setTimestamp(4, Timestamp.valueOf(user.lastRes));
        ps.setTimestamp(5, Timestamp.valueOf(user.lastTopic));

        ps.executeUpdate();

        val u= user.withId(SQL.getAutoIncrement(ps));
        ps.close();
        return u;
    }

    def save(user: User) {
        val ps = this.con.prepareStatement("""UPDATE user
SET sn=?,pass=?,lv=?,lastRes=?,lastTopic=?
WHERE id=?""");
        ps.setString(1, user.sn);
        ps.setString(2, user.pass);
        ps.setInt(3, user.lv);
        ps.setTimestamp(4, Timestamp.valueOf(user.lastRes));
        ps.setTimestamp(5, Timestamp.valueOf(user.lastTopic));
        ps.setInt(6, user.id);

        ps.executeUpdate();
        ps.close();

    }

    def read(id: Int): User = {
        if (id < 1) {
            throw new MisdirectedRequestException("idが1未満です");
        }

        val ps = this.con.prepareStatement("SELECT * FROM user WHERE id=?");
        ps.setInt(1, id);

        val u= UserRepository.toUser(ps.executeQuery());
        ps.close();
        return u;
    }

    def getId(sn: String): Int = {
        val ps = this.con.prepareStatement("SELECT id FROM user WHERE sn=?");
        ps.setString(1, sn);
        val rs = ps.executeQuery();

        if (!rs.next()) {
            throw new NotFoundException("ユーザーが見つかりません");
        }

        val id= rs.getInt(1);
        ps.close;
        rs.close;
        return id;
    }
}