package no.kristiania.http;

import no.kristiania.questions.AnswerDao;

import java.sql.SQLException;

public class categoryOptionsController implements HttpController  {
    private AnswerDao answerDao;

    public categoryOptionsController(AnswerDao answerDao) {
        this.answerDao = answerDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        String responseText = "";

        int value = 1;
        for (String answer : answerDao.listAll()) {
            responseText += "<option value=" + (value++) + ">" + answer + "</option>";
        }
        return new HttpMessage("HTTP/1.1 200 OK", responseText);
    }
}
