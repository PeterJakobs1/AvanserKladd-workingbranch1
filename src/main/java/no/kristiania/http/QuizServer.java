package no.kristiania.http;

import no.kristiania.questions.AnswerDao;
import no.kristiania.questions.QuestionDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class QuizServer {
    private static  final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1984);
        new AnswerDao(createDataSource());
        new QuestionDao(createDataSource());
//        httpServer.addController(" /api/newAnswer", new categoryOptionsController(answerDao));
//        httpServer.addController(" /api/newQuestion", new categoryOptionsController(answerDao));
        logger.info(" Starting http://localhost:{}/index.html", httpServer.getPort());
        // http://localhost:1984/index.html

    }

    private static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")) {
            properties.load(fileReader);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty(
                "dataSource.url",
                "jdbc:postgresql://localhost:5432/person_db"));
        dataSource.setUser(properties.getProperty("dataSource.user","person_dbuser"));
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
