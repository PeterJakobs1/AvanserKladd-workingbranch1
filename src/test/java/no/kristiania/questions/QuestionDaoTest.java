package no.kristiania.questions;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDaoTest {

    private final QuestionDao dao = new QuestionDao(TestData.testDataSource());


    @Test
    void shouldRetrieveSavedQuestionFromDatabase() throws SQLException {
        Question question = exampleQuestion();
        dao.saveQuestion(question);

        assertThat(dao.retrieve(question.getId()))
                .usingRecursiveComparison()
                .isEqualTo(question)
        ;
    }

    @Test
    void shouldListAllQuestions() throws SQLException {
        Question question = exampleQuestion();
        dao.saveQuestion(question);
        Question anotherQuestion = exampleQuestion();
        dao.saveQuestion(anotherQuestion);

        assertThat(dao.listAll())
                .extracting(Question::getId)
                .contains(question.getId(), anotherQuestion.getId());
    }

    @Test
    void shouldListByQuestionName() throws SQLException {
        Question question = exampleQuestion();
        dao.saveQuestion(question);

        Question anotherMatchingQuestion = exampleQuestion();
        anotherMatchingQuestion.setQuestion(question.getQuestion());
        dao.saveQuestion(anotherMatchingQuestion);

        Question nonMatchingQuestion = exampleQuestion();
        dao.saveQuestion(nonMatchingQuestion);

        assertThat(dao.listByQuestionName(question.getQuestion()))
                .extracting(Question::getId)
                .contains(question.getId(), anotherMatchingQuestion.getId());


    }

    public static Question exampleQuestion() {
        Question question = new Question();
        question.setQuestion(TestData.pickOne("Fish?","Bread?","Hat?"));
        return question;
    }
}
