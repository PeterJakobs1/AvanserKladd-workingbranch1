package no.kristiania.questions;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerDao {

    private final DataSource dataSource;

    public AnswerDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(String answer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into answers (answer_name) values (?)",
                    Statement.RETURN_GENERATED_KEYS

            )) {
                statement.setString(1,answer);
                statement.executeUpdate();
            }
        }
    }

    public List<String> listAll() throws SQLException {
        try (Connection dataSourceConnection = dataSource.getConnection()) {
            try (PreparedStatement statement = dataSourceConnection.prepareStatement("select * from answers")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    ArrayList<String> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(resultSet.getString("answer_name"));

                    }
                    return result;
                }
            }
        }
    }


}
