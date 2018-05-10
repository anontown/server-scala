package anontown.model.user.profile

import java.sql.ResultSet
import anontown.model.SQL
import java.sql.Connection
import anontown.model.exceptions.NotFoundException
import anontown.model.user.User
import anontown.model.user.Attestation
import java.sql.SQLException
import anontown.model.exceptions.ConflictException
import java.sql.Statement

object ProfileRepository {
    private def toProfile(rs: ResultSet): List[Profile] = {
        val p= SQL.toObjList(rs, { rs =>
            Profile(rs.getInt(1),
                rs.getString(2),
                rs.getInt(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getBoolean(7))
        });
        rs.close();
        return p;
    }
}

class ProfileRepository(private val con: Connection) {
    def add(profile: Profile): Profile = {
        val ps = this.con.prepareStatement("INSERT INTO profile VALUES(NULL,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        try {
            ps.setString(1, profile.sid);
            ps.setInt(2, profile.user);
            ps.setString(3, profile.name);
            ps.setString(4, profile.text);
            ps.setString(5, profile.mdtext);
            ps.setBoolean(6, profile.active);
            ps.executeUpdate();

            return profile.withId(SQL.getAutoIncrement(ps));
        } catch {
            case e: SQLException => {
                if (e.getErrorCode == 1062) {
                    throw new ConflictException("既にsidが存在します");
                } else {
                    throw e;
                }
            }
        }finally {
            ps.close();
        }
    }

    def save(profile: Profile) {
        val ps = this.con.prepareStatement("""UPDATE profile
SET name=?,text=?,mdtext=?,active=?
WHERE id=?""");
        ps.setString(1, profile.name);
        ps.setString(2, profile.text);
        ps.setString(3, profile.mdtext);
        ps.setBoolean(4, profile.active);
        ps.setInt(5, profile.id);

        ps.executeUpdate();
        ps.close();
    }

    def read(sid: String): Profile = {
        val ps = this.con.prepareStatement("SELECT * FROM profile WHERE sid=?");
        ps.setString(1, sid);

        val list = ProfileRepository.toProfile(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("");
        }
        ps.close();
        return list(0);
    }

    def read(id: Int): Profile = {
        val ps = this.con.prepareStatement("SELECT * FROM profile WHERE id=?");
        ps.setInt(1, id);

        val list = ProfileRepository.toProfile(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("");
        }
        ps.close();
        return list(0);
    }

    def readAll(user: Int): List[Profile] = {
        val ps = this.con.prepareStatement("SELECT * FROM profile WHERE user=?");
        ps.setInt(1, user);

        val p= ProfileRepository.toProfile(ps.executeQuery());
        ps.close();
        return p;
    }
}