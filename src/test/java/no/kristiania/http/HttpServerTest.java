package no.kristiania.http;

import no.kristiania.questions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private final HttpServer server = new HttpServer(0);

    HttpServerTest() throws IOException {

    }
    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldRespondWithRequestTargetIn404() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals("File not found: /non-existing", client.getMessageBody());
    }


    @Test
    void shouldCreateAndServeFile() throws IOException {
        HttpServer server = new HttpServer(10004);
        Paths.get("target/test-classes");

        String fileContent = "A file was created by @TEST shouldCreateAndServeFile";
        Files.write(Paths.get("target/test-classes/test-file.txt"),fileContent.getBytes());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/test-file.txt");
        assertEquals(fileContent, client.getMessageBody());
    }

    @Test
    void shouldReturnCategoryFromServer() throws IOException, SQLException {
        AnswerDao answerDao = new AnswerDao((TestData.testDataSource()));
        answerDao.save("Fish");
        answerDao.save("Bird");

        server.addController ("/api/categoryOptions", new categoryOptionsController(answerDao));

        HttpClient client = new HttpClient("localhost", server.getPort(),"/api/categoryOptions");
        assertEquals("<option value=1>Fish</option><option value=2>Bird</option>", client.getMessageBody());
    }

    @Test
    void shouldCreateNewQuestion() throws IOException, SQLException {
        QuestionDao questionDao = new QuestionDao(TestData.testDataSource());
        server.addController("/api/newQuestion", new addQuestionController(questionDao));

        HttpServer server = new HttpServer(0);
        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/newQuestion",
                "questionInput=goldfish"
        );
        assertEquals(200, postClient.getStatusCode());
        assertThat(questionDao.listAll())
                .anySatisfy(p -> assertThat(p.getQuestion()).isEqualTo("goldfish"));
    }

    @Test
    void shouldListQuestionFromDatabase() throws SQLException, IOException {
        QuestionDao questionDao = new QuestionDao(TestData.testDataSource());

        Question question1 = QuestionDaoTest.exampleQuestion();
        questionDao.saveQuestion(question1);
        Question question2 = QuestionDaoTest.exampleQuestion();
        questionDao.saveQuestion(question2);

        server.addController("/api/question", new listQuestionController(questionDao));

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/question");
        assertThat(client.getMessageBody())
                .contains(question1.getQuestion())
                .contains(question2.getQuestion());


    }
}