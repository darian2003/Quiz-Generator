package com.example.project;

public class Answer {

    protected static int noAnswers = 0;
    protected double points; // between -1 and 1
    protected int id;
    protected String text;

    enum AnswerType {
        CORRECT,
        INCORRECT
    }
    AnswerType type;

    public Answer() {
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setType(AnswerType type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public AnswerType getType() {
        return type;
    }

    public Answer(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "points=" + points +
                ", id=" + id +
                ", text='" + text + '\'' +
                ", type=" + type +
                '}';
    }

    public static void addAnswer() {
        noAnswers++;
    }

}
