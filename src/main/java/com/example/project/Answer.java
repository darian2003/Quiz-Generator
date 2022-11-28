package com.example.project;

public class Answer {

    public static int noAnswers = 0;
    public double points; // between -1 and 1
    private int id;
    private String text;

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
}
