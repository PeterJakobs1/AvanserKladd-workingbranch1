package no.kristiania.http;

import no.kristiania.questions.QuestionDao;

import java.sql.SQLException;

public class listQuestionController implements HttpController {
    public listQuestionController(QuestionDao questionDao) {

    }

    @Override
    public HttpMessage handle(HttpMessage request)  {
        return new HttpMessage("HTTP/1.1 200 OK", "OK");
    }
}
