package com.example.project;

import java.util.ArrayList;

import java.text.DecimalFormat;

public class Quizz {

    private int id;
    private int ownerID;
    private final String name;
    protected int numberOfQuestions;
    protected ArrayList<Question> questions = new ArrayList<>();
    protected ArrayList<User> userHasAnswered = new ArrayList<>();

    protected Quizz(String name) {
        this.name = name;
    }

    protected Quizz(int id, String name, int numberOfQuestions, int ownerID) {
        this.id = id;
        this.name = name;
        this.numberOfQuestions = numberOfQuestions;
        this.ownerID = ownerID;
    }

    protected int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public int getOwnerID() {
        return ownerID;
    }

    // search for the lowest free natural number
    public void setId(DataBase dataBase) {
        for (this.id = 1; ; this.id++) {
            int alreadyExists = 0;
            for (Quizz quizz : dataBase.quizz) {
                if (quizz.id == this.id) {
                    alreadyExists = 1;
                    break;
                }
            }
            if (alreadyExists == 0)
                return;
        }
    }

    public void setOwnerID(DataBase dataBase, User user) {
        for (User ref : dataBase.users) {
            if (ref.getUsername().equals(user.getUsername()) && ref.getPassword().equals(user.getPassword())) {
                this.ownerID = ref.getId();
                return;
            }
        }
    }

    // check whether quizz name already exists in the database
    public static boolean checkQuizName(String quizName, DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (quizz.name.equals(quizName))
                return false;
        }
        return true;
    }

    // add the desired questions to the quiz
    public boolean addQuestionsToQuizz(String[] args, DataBase dataBase) {
        for (int i = 4; i < args.length; i++) {
            String[] arrOfStrings = args[i].split("'", 0);
            int questionId = Integer.parseInt(arrOfStrings[1]);
            int found = 0;
            for (Question question : dataBase.questions) {
                if (question.getId() == questionId) {
                    this.questions.add(question);
                    this.numberOfQuestions++;
                    found = 1;
                    break;
                }
            }
            if (found == 0) {
                System.out.println("{ 'status' : 'error', 'message' : 'Question ID for question " + (i - 3) + " does not exist'}");
                return false;
            }
        }
        return true;
    }

    public static Quizz createQuiz(String[] args, DataBase dataBase) {
        if (args.length > 14) {
            System.out.println("{ 'status' : 'error', 'message' : 'Quizz has more than 10 questions'}");
            return null;
        }

        // get quizz name
        String[] arrOfStrings = args[3].split("'", 0);
        String quizzName = arrOfStrings[1];
        boolean check = Quizz.checkQuizName(quizzName, dataBase);
        if (!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Quizz name already exists'}");
            return null;
        }

        return new Quizz(quizzName);
    }

    // checks if the quiz that is trying to be submitted meets all the desired conditions
    // if all conditions are met, then return the reference of the quizz
    public static Quizz checkSubmittedQuizConditions(String[] args, DataBase dataBase) {
        if (args.length < 4) {
            System.out.println("{ 'status' : 'error', 'message' : 'No quizz identifier was provided'}");
            return null;
        }

        // take quizz id
        String str = args[3];
        String[] arrOfStrings = str.split("'", 0);
        int quizzID = Integer.parseInt(arrOfStrings[1]);
        // check existence of quizz
        Quizz quizz = Quizz.returnQuizzByID(quizzID, dataBase);
        // quiz id is not correct
        if (quizz == null) {
            System.out.println("{ 'status' : 'error', 'message' : 'No quiz was found'}");
            return null;
        }

        // the user that created a quiz cannot answer it
        // search for the id of the currently logged-in user
        User currentUser = User.currentUser(args, dataBase);
        if (currentUser.getId() == quizz.getOwnerID()) {
            System.out.println("{ 'status' : 'error', 'message' : 'You cannot answer your own quizz'}");
            return null;
        }

        // check if this user has already submitted this quiz
        boolean check = quizz.checkIfUserCompletedThisQuizz(currentUser);
        if (check) {
            // user has already answered this quiz
            System.out.println("{ 'status' : 'error', 'message' : 'You already submitted this quizz'}");
            return null;
        }
        return quizz;
    }

    // used by readQuizzFromFile function
    public void addQuestionToQuizzFromFile(DataBase dataBase, int questionID) {
        for (Question question : dataBase.questions) {
            if (question.getId() == questionID)
                this.questions.add(question);
        }
    }

    // gets the id of a quizz and searches for it in the quizz ArrayList
    public static Quizz returnQuizzByID(int quizzID, DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (quizz.id == quizzID)
                return quizz;
        }
        return null;
    }

    // search for a specific quizz when given its name
    public void returnQuizzID(DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (this.name.equals(quizz.name)) {
                System.out.println("{ 'status' : 'ok', 'message' : '" + quizz.id +"'}");
                return;
            }
        }
        System.out.println("{ 'status' : 'error', 'message' : 'Quizz does not exist'}");
    }

    // prints all quizzes in the database
    // auxiliary function for getAllQuizzes
    public static void returnAllQuizzes(DataBase dataBase, User user) {
        System.out.print("{ 'status' : 'ok', 'message' : '[");
        for (int i = 0 ; i  < dataBase.quizz.size() ; i++) {
            Quizz quizz = dataBase.quizz.get(i);
            String isCompleted = "False";
            if (quizz.checkIfUserCompletedThisQuizz(user))
                isCompleted = "True";
            System.out.print("{\"quizz_id\" : \"" + quizz.id + "\", \"quizz_name\" : \"" + quizz.name + "\", \"is_completed\" : \"" + isCompleted + "\"}");
            if (i+1 < dataBase.quizz.size()) {
                System.out.print(", ");
            }
        }
        System.out.println("]'}");
    }

    public boolean checkIfUserCompletedThisQuizz(User user) {
        for (User ref : this.userHasAnswered) {
            if (ref.getId() == user.getId())
                return true;
        }
        return false;
    }

    public void userHasAnsweredThisQuizz(int userID, DataBase dataBase) {
        for (User user : dataBase.users) {
            if (user.getId() == userID) {
                this.userHasAnswered.add(user);
                return;
            }
        }
    }

    public void returnQuizzDetails() {
        System.out.print("{'status' : 'ok', 'message' : '[");
        for (int i = 0; i < this.questions.size(); i++) {
            Question question = this.questions.get(i);
            System.out.print("{\"question-name\":\"" + question.getText() + "\", \"question_index\":\""+(i+1)+"\", \"question_type\":\""+question.getType()+ "\", \"answers\":\"[");
            for (int j = 0; j < question.answers.size(); j++) {
                Answer answer = question.answers.get(j);
                System.out.print("{\"answer_name\":\"" + answer.getText() + "\", \"answer_id\":\"" + answer.getId() + "\"}");
                if (j+1 < question.answers.size())
                    System.out.print(", ");
            }
            System.out.print("]\"}");
            if (i+1 < this.questions.size())
                System.out.print(", ");
        }
        System.out.println("]'}");
    }

    public double getQuizzResults(String[] args, DataBase dataBase) {
        double sum = 0;
        for (int i = 4; i < args.length; i++) {
            String[] arrOfStr = args[i].split("'", 0);
            int answerID = Integer.parseInt(arrOfStr[1]);
            // search for the answer reference using its ID by going through the answers ArrayList from the database
            int found = 0; // states whether the answer ID exists or not
            for (Answer answer : dataBase.answers) {
                if (answer.getId() == answerID) {
                    sum += answer.points;
                    found = 1;
                    break;
                }
            }
            if (found == 0) {
                System.out.println("{ 'status' : 'error', 'message' : 'Answer ID for answer " + (i-3) + " does not exist'}");
                return -1;
            }
        }
        if (sum < 0)
            return 0;

        final DecimalFormat df = new DecimalFormat("0.00");
        df.format(sum); // round the score to two decimals
        return sum / this.numberOfQuestions;
    }

    public static void deleteQuizz(int quizzID, DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (quizz.id == quizzID) {
                dataBase.quizz.remove(quizz);
                System.out.println("{ 'status' : 'ok', 'message' : 'Quizz deleted successfully'}");
                return;
            }
        }
        System.out.println("{ 'status' : 'error', 'message' : 'No quiz was found'}");
    }

}
