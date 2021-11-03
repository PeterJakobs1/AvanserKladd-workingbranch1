package no.kristiania.http;

import no.kristiania.questions.AnswerDao;
import no.kristiania.questions.Question;
import no.kristiania.questions.QuestionDao;
import no.kristiania.questions.TestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private final HttpServer server = new HttpServer(0);


    HttpServerTest() throws IOException {

    }


    @Test
    void shouldRespondWith200RequestTarget() throws IOException {
        HttpServer server = new HttpServer(10003);
        HttpClient client =new HttpClient("localhost",server.getPort(),"/test");
        assertEquals(200,client.getStatusCode());

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
    void shouldCreateNewItem() throws IOException, SQLException {
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
        Question questionItem = questionDao.listAll().get(0);
        assertEquals("goldfish", questionItem.getQuestion());
    }

}