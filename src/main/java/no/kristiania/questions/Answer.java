package no.kristiania.questions;

public class Answer {
    private String answerName;
    private String answerId;

    public void setAnswerName(String answerName) {
        this.answerName = answerName;
    }

    public String getAnswerName() {
        return answerName;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getAnswerId() {
        return answerId;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "answerName='" + answerName + '\'' +
                ", answerId='" + answerId + '\'' +
                '}';
    }
}
