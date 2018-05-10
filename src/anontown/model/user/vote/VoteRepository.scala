package anontown.model.user.vote

import java.sql.ResultSet
import anontown.model.SQL
import java.sql.Connection
import java.sql.SQLException
import anontown.model.exceptions.ConflictException
import java.sql.Statement

object VoteRepository {
    private def toVote(rs: ResultSet): List[Vote] = {
        val v= SQL.toObjList(rs, { rs =>
            Vote(rs.getInt(1),
                rs.getInt(2),
                rs.getInt(3));
        });
        rs.close();
        return v;
    }
}

class VoteRepository(private val con: Connection) {
    def add(vote: Vote): Vote = {
        val ps = this.con.prepareStatement("INSERT INTO vote VALUES(NULL,?,?)",Statement.RETURN_GENERATED_KEYS);
        try {
            ps.setInt(1, vote.user);
            ps.setInt(2, vote.res);

            ps.executeUpdate();

            return vote.withId(SQL.getAutoIncrement(ps));
        } catch {
            case e: SQLException => {
                if (e.getErrorCode == 1062) {
                    throw new ConflictException("既に投票が存在します");
                } else {
                    throw e;
                }
            }
        }finally {
            ps.close();
        }
    }

    def readAll(user: Int): List[Vote] = {
        val ps = this.con.prepareStatement("SELECT * FROM vote WHERE user=?");
        ps.setInt(1, user);

        val v= VoteRepository.toVote(ps.executeQuery());
        ps.close();
        return v;
    }
}