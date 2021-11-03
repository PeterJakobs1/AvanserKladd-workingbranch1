package no.kristiania.http;

import no.kristiania.questions.AnswerDao;
import no.kristiania.questions.Question;
import no.kristiania.questions.QuestionDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;


public class HttpServer {

    private static  final Logger logger = LoggerFactory.getLogger(HttpServer.class);


    private final ServerSocket serverSocket;
    private List<Question> questionList = new ArrayList<>();
    private AnswerDao answerDao;
    private QuestionDao questionDao;

    public HttpServer(int serverPort) throws IOException {
        serverSocket = new ServerSocket(serverPort);

        new Thread(this::serveClients).start();
    }
    // -- Error handling --  //
    private void serveClients() {
        try {
            while (true) {
                serveClient();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void serveClient() throws IOException, SQLException {
        Socket clientSocket = serverSocket.accept();

        HttpMessage httpMessage = new HttpMessage(clientSocket);
        String[] requestLine = httpMessage.startLine.split(" ");
        String requestTarget = requestLine[1];

        int questionPos = requestTarget.indexOf('?');
        String targetFile;
        String query = null;
        if (questionPos != -1) {
            targetFile = requestTarget.substring(0, questionPos);
            query = requestTarget.substring(questionPos+1);
        } else {
            targetFile = requestTarget;
        }

        if (targetFile.equals("/test")) {
            String testText = " ";
            if (query != null) {
                Map<String, String> queryMap = parseRequestParameters(query);
                testText = queryMap.get("productName");
            }
            String responseText = "test test";
            Response(clientSocket, responseText, "text/html");

        } else if (targetFile.equals("/api/newProduct")) {
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            Question aQuestion = new Question();
            aQuestion.setQuestion(queryMap.get("questionInput"));
            questionDao.saveQuestion(aQuestion);
            Response(clientSocket, "Question Added!", "text/html");

        } else if (targetFile.equals("/api/products")) {

            String responseText = "";
            for (Question i : questionDao.listAll()) {
                responseText += "<li>" + i.toString() + "</li>";
            }
            String responseList = "<ul>" + responseText +"</ul>";
            Response(clientSocket, responseList, "text/html");

        } else if (targetFile.equals("/api/categoryOptions")) {
            String responseText = "";

            int value = 1;
            for (String option : answerDao.listAll()) {
                responseText += "<option value=" + (value++) + ">" + option + "</option>";
            }

            Response(clientSocket, responseText, "text/html");

        } else {
            InputStream resourceFile = getClass().getResourceAsStream(targetFile);
            if (resourceFile != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                resourceFile.transferTo(buffer);
                String responseText = buffer.toString();


                String contentType = "text/plain";
                if (requestTarget.endsWith(".html")) {
                    contentType = "text/html";
                }
                Response(clientSocket, responseText, contentType);
                return;

            }

            String responseText = "File not found: " + requestTarget;

            String response = "HTTP/1.1 404 Not found\r\n" +
                    "Content-Length: " + responseText.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseText;
            clientSocket.getOutputStream().write(response.getBytes());
        }
    }

    private Map<String, String> parseRequestParameters(String query) {
        Map<String, String> queryMap = new HashMap<>();
        for (String queryParameter : query.split("&")) {
            int equalsPos = queryParameter.indexOf('=');
            String parameterName = queryParameter.substring(0, equalsPos);
            String parameterValue = queryParameter.substring(equalsPos+1);
            queryMap.put(parameterName, parameterValue);
        }
        return queryMap;
    }

    private void Response(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseText.length() + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseText;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1984);
        httpServer.setAnswerDao(new AnswerDao(createDataSource()));
        httpServer.setQuestionDao(new QuestionDao(createDataSource()));
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

    public int getPort() {
        return serverSocket.getLocalPort();
    }

   public void setAnswerDao(AnswerDao answerDao){
        this.answerDao = answerDao;
   }

   public void setQuestionDao(QuestionDao questionDao) {
        this.questionDao = questionDao;
   }

    public List<Question> getItem() {
        return questionList;
    }

}