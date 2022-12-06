package com.example.project;

import java.util.ArrayList;

public class Question {

    protected static int population = 0;

    private int id;
    private final String text;
    private final String type;

    protected ArrayList<Answer> answers = new ArrayList<>();

    protected String getType() {
        return type;
    }

    protected String getText() { return this.text; }

    protected int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    // constructor with no id -> used for reference
    protected Question(String text, String type) {
        this.id = 0;
        this.text = text;
        this.type = type;
    }

    // used to read  a question from file
    protected Question(String text, String type, int id) {
        this.text = text;
        this.type = type;
        this.id = id;
    }


    protected static Question createQuestion(String[] args, DataBase dataBase) {
        // take question's text
        String str = args[3];
        String[] arrOfStrings = str.split("'", 0);
        String parameter = arrOfStrings[0];
        String[] questionText = parameter.split(" ",0);
        if (!questionText[0].equals("-text")) {
            System.out.println("{ 'status' : 'error', 'message' : 'No question text provided'}");
            return null;
        }
        String text = arrOfStrings[1];
        // take question's type
        str = args[4];
        arrOfStrings = str.split("'", 0);
        if(!arrOfStrings[1].equals("single") && !arrOfStrings[1].equals("multiple")) {
            System.err.println("Question type is not valid");
            return null;
        }

        Question question = new Question(text, arrOfStrings[1]);
        boolean check = question.checkIfQuestionExists(dataBase);
        if(check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Question already exists'}");
            return null;
        }
        return question;
    }

    protected boolean addAnswersToQuestion(String[] args) {
        int answerID = 1;
        for (int i = 5; i < args.length; i++) {
            // first check whether the correct parameters are provided for answer description
            String ref = args[i];
            String[] arrOfStr = ref.split(" ",0);
            if (!arrOfStr[0].equals("-answer-"+answerID)) {
                System.out.println("{ 'status' : 'error', 'message' : 'Answer "+ answerID +" has no answer description'}");
                return false;
            }
            Answer answer = new Answer();
            String[] answerText = arrOfStr[1].split("'",0);
            answer.setText(answerText[1]);

            // then check whether the correct parameters are provided for answer correct flag
            i++;
            if (i == args.length) {
                System.out.println("{'status':'error','message':'Answer " + answerID +" has no answer correct flag'}");
                return false;
            }
            ref = args[i];
            arrOfStr = ref.split(" ", 0);
            if (!arrOfStr[0].equals("-answer-"+answerID+"-is-correct")) {
                System.out.println("{'status':'error','message':'Answer " + answerID +" has no answer correct flag'}");
                return false;
            }
            String[] takeCorrectFlag = arrOfStr[1].split("'",0);
            int correctFlag = Integer.parseInt(takeCorrectFlag[1]);
            if (correctFlag == 1)
                answer.setType(Answer.AnswerType.CORRECT);
            else if (correctFlag == 0)
                answer.setType(Answer.AnswerType.INCORRECT);
            else System.err.println("Provided correct flag is not acceptable!");

            Answer.addAnswer();
            answer.setId(Answer.noAnswers);
            answerID++;
            this.answers.add(answer);
        }
        return true;
    }

    // checks whether the answers of this question meet the required conditions
    protected boolean answerError() {
        // check whether single question has multiple correct answers
        boolean check = true;
        if (this.getType().equals("single"))
            check = this.singleTypeQuestionHasMultipleCorrectAnswers();

        if (!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Single correct answer question has more than one correct answer'}");
            return false;
        }

        // check whether question has duplicate answers
        check = this.duplicateAnswers();
        if (!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Same answer provided more than once'}");
            return false;
        }
        return true;
    }

    // looks into the database for this question
    protected boolean checkIfQuestionExists(DataBase dataBase) {
        for (int i = 0; i < dataBase.questions.size(); i++) {
            Question ref = dataBase.questions.get(i);
            if (ref.text.equals(this.text))
                return true;
        }
        return false;
    }

    // returns a question id when knowing its text
    protected int returnQuestionId(DataBase dataBase) {
        for (int i = 0; i < dataBase.questions.size(); i++) {
            Question ref = dataBase.questions.get(i);
            if (ref.text.equals(this.text))
                return ref.id;
        }
        return -1;
    }

    public boolean singleTypeQuestionHasMultipleCorrectAnswers() {
        int count = 0; // if count != 0 => false
        for (Answer ref : this.answers) {
            if (ref.type.equals(Answer.AnswerType.CORRECT))
                count++;
        }
        return count == 1;
    }

    public boolean duplicateAnswers() {
        for(int i = 0; i < this.answers.size() - 1 ; i++) {
            Answer ref1 = this.answers.get(i);
            for(int j = i+1; j < this.answers.size(); j++) {
                Answer ref2 = this.answers.get(j);
                if (ref1.getText().equals(ref2.getText()))
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", type='" + type + '\'' +
                ", answers=" + answers +
                '}';
    }

    public String getQustion() {
        return "{\"question_id\" : \"" + this.id + "\", \"question_name\" : \"" + this.text + "\"}";
    }

    // treats single and multiple type questions accordingly
    public void setPointsPerAnswer() {
        if (this.type.equals("single")) {
            // => any wrong answer has -1 points and the right on has +1 points
            for (Answer answer : this.answers) {
                if (answer.type.equals(Answer.AnswerType.CORRECT)) {
                    answer.points = 1;
                } else {
                    answer.points = -1;
                }
            }
        } else {
            // first we count the number of right and wrong answers to calculate the weight of each answer
            int right = 0;
            int wrong = 0;
            for (Answer answer : this.answers) {
                if (answer.type.equals(Answer.AnswerType.CORRECT)) {
                    right++;
                } else {
                    wrong++;
                }
            }
            if (right == 0 || wrong == 0) {
                System.err.println("Impartire la 0");
                return;
            }
            // now start assigning the right number of points per answer
            for (Answer answer : this.answers) {
                if (answer.type.equals(Answer.AnswerType.CORRECT)) {
                    answer.points = (double)(1) / right;
                } else {
                    answer.points = (double)(-1) / wrong;
                }
            }
        }
    }

}
