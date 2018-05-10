package anontown.model.topic.history

import java.sql.Connection;
import java.sql.ResultSet;

import anontown.model.SQL;
import anontown.model.exceptions.NotFoundException
import anontown.model.topic.Topic
import java.sql.Statement
import java.sql.Timestamp

object TopicHistoryRepository {
    def toTopicHistory(rs: ResultSet): List[TopicHistory] = {
        val th= SQL.toObjList(rs, { rs =>
            TopicHistory(rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getTimestamp(6).toLocalDateTime(),
                rs.getString(7),
                rs.getInt(8),
                rs.getInt(9));
        });
        rs.close();
        return th;
    }
}

class TopicHistoryRepository(private val con: Connection) {

    def add(th: TopicHistory): TopicHistory = {
        val ps = this.con.prepareStatement("INSERT INTO topic_history VALUES(NULL,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, th.title);
        ps.setString(2, th.category);
        ps.setString(3, th.text);
        ps.setString(4, th.mdtext);
        ps.setTimestamp(5, Timestamp.valueOf(th.date));
        ps.setString(6, th.hash);
        ps.setInt(7, th.user);
        ps.setInt(8, th.topic);

        ps.executeUpdate();

        val th2= th.withId(SQL.getAutoIncrement(ps));
        ps.close();
        return th2;
    }

    def read(id: Int): TopicHistory = {
        val ps = this.con.prepareStatement("SELECT * FROM topic_history WHERE id=?");
        ps.setInt(1, id);

        val list = TopicHistoryRepository.toTopicHistory(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("");
        }
        ps.close();
        return list(0);
    }

    def readAll(topic: Int): List[TopicHistory] = {
        val ps = this.con.prepareStatement("SELECT * FROM topic_history WHERE topic=?");
        ps.setInt(1, topic);

        val th= TopicHistoryRepository.toTopicHistory(ps.executeQuery());
        ps.close();
        return th;
    }
}