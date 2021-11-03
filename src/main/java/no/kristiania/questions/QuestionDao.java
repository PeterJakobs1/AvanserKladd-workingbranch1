package no.kristiania.questions;

import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class QuestionDao {

    private final DataSource dataSource;

    public QuestionDao(DataSource dataSource) {
        this.dataSource = dataSource;

    }

    public static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")) {
            properties.load(fileReader);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty(
                "dataSource.url",
                "jdbc:postgresql://localhost:5432/product_db"));
        dataSource.setUser(properties.getProperty("dataSource.user","person_dbuser"));
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;

    }

    public void saveQuestion(Question question) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into questions(question) values (?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setString(1, question.getQuestion());

                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    resultSet.next();
                    question.setId(resultSet.getLong("question_id"));
                }

            }
        }

    }

    public Question retrieve(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(
                    "select * from questions where question_id  = ?"
            )) {
                statement.setLong(1,id);

                try (ResultSet resultSet = statement.executeQuery()) {
                   resultSet.next();

                    return fromResultSet(resultSet);
                }
            }
        }
    }



    public List<Question> listByQuestionName(String lastName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(
                    // ------------- add ilike  -------- //
                    "select * from questions where question = ?"
            )) {
                statement.setString(1, lastName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    ArrayList<Question> question = new ArrayList<>();

                    while (resultSet.next()) {

                        question.add(fromResultSet(resultSet));
                    }


                    return question;
                }
            }
        }
    }

    public List<Question> listAll() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(
                    "select * from questions")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    ArrayList<Question> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(fromResultSet(resultSet));
                    }
                    return result;
                }
            }
        }
    }

    private Question fromResultSet(ResultSet resultSet) throws SQLException {
        Question question = new Question();
        question.setId(resultSet.getLong("question_id"));
        question.setQuestion((resultSet.getString("question")));
        return question;
    }

    public static void main(String[] args) {

    }
}
