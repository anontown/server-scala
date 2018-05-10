package anontown.model.topic

import java.sql.Connection
import java.sql.ResultSet
import anontown.model.topic.history.TopicHistory
import anontown.model.SQL
import anontown.model.topic.history.TopicHistoryRepository
import anontown.model.exceptions.NotFoundException
import anontown.model.user.User
import anontown.model.exceptions.MisdirectedRequestException
import java.sql.Statement
import java.sql.Timestamp
import anontown.model.user.token.Token
import anontown.model.res.Res

object TopicRepository {
    def toTopic(rs: ResultSet): List[Topic] = {
        val t= SQL.toObjList(rs, { rs =>
            new Topic(rs.getInt(1),
                rs.getString(2),
                Topic.toCategoryArray(rs.getString(3)),
                rs.getString(4),
                rs.getString(5),
                rs.getTimestamp(6).toLocalDateTime(),
                rs.getInt(7),
                rs.getTimestamp(8).toLocalDateTime());
        });
        rs.close();
        return t;
    }
}

class TopicRepository(private val con: Connection) {
    def add(topic: Topic, user: User,token:Token):(TopicHistory,Res,User,Topic) = {
        val ps = this.con.prepareStatement("INSERT INTO topic VALUES(NULL,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, topic.title);
        ps.setString(2, Topic.toCategoryString(topic.category));
        ps.setString(3, topic.text);
        ps.setString(4, topic.mdtext);
        ps.setTimestamp(5, Timestamp.valueOf(topic.updatetime));
        ps.setInt(6, topic.user);
        ps.setTimestamp(7, Timestamp.valueOf(topic.date));

        ps.executeUpdate();

        val t = topic.withId(SQL.getAutoIncrement(ps),user,token);
        ps.close();
        return t;
    }

    def save(topic: Topic) {
        val ps = this.con.prepareStatement("""UPDATE topic
SET title=?,category=?,text=?,mdtext=?,updatetime=?
WHERE id=?""");
        ps.setString(1, topic.title);
        ps.setString(2, Topic.toCategoryString(topic.category));
        ps.setString(3, topic.text);
        ps.setString(4, topic.mdtext);
        ps.setTimestamp(5, Timestamp.valueOf(topic.updatetime));
        ps.setInt(6, topic.id);

        ps.executeUpdate();
        ps.close();
    }

    def read(id: Int): Topic = {
        if (id < 1) {
            throw new MisdirectedRequestException("IDが1未満です");
        }

        val ps = this.con.prepareStatement("SELECT * FROM topic WHERE id=?");
        ps.setInt(1, id);

        val list = TopicRepository.toTopic(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("");
        }
        ps.close();
        return list(0);

    }

    //ページは1から
    def search(category: List[String], title: String, page: Int, limit: Int): List[Topic] = {
        Topic.checkCategory(category);
        if (page < 1) {
            throw new MisdirectedRequestException("pageが1未満です");
        }
        if (limit < 1) {
            throw new MisdirectedRequestException("limitが1未満です");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM topic
WHERE (category=? OR category LIKE ?) AND title LIKE ?
ESCAPE '#'
ORDER BY updatetime DESC
LIMIT ?,?""");

        val cs = Topic.toCategoryString(category);
        val cLike = if (cs.length == 0) "%" else SQL.likeEsc(cs) + "/_%";
        val tLike = if (title.length == 0) "%" else "%" + SQL.likeEsc(title) + "%";

        ps.setString(1, cs);
        ps.setString(2, cLike);
        ps.setString(3, tLike);
        ps.setInt(4, (page - 1) * limit);
        ps.setInt(5, limit);

        val t= TopicRepository.toTopic(ps.executeQuery());
        ps.close();
        return t;
    }
}