package no.kristiania.http;

import no.kristiania.questions.Question;
import no.kristiania.questions.QuestionDao;

import java.sql.SQLException;

public class listQuestionController implements HttpController {
    private QuestionDao questionDao;

    public listQuestionController(QuestionDao questionDao) {

        this.questionDao = questionDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        String response = "";

        for (Question question : questionDao.listAll()) {
            response += "<div>" + question.getQuestion();
        }

        return new HttpMessage("HTTP/1.1 200 OK", response);
    }
}
