package com.example.project;

import javax.swing.*;
import java.io.*;
import java.util.SortedMap;

import static java.lang.Math.log;
import static java.lang.Math.round;

public class Command {
    protected static void createUser(String[] args, DataBase dataBase) {
        if (args.length == 1) {
            System.out.println("{ 'status' : 'error', 'message' : 'Please provide username'}");
            return;
        }
        if (args.length == 2) {
            System.out.println("{ 'status' : 'error', 'message' : 'Please provide password'}");
            return;
        }

        // take username
        String str = args[1];
        String[] arrOfStr = str.split("'", 0);
        String username = arrOfStr[1];

        // check if user already exists
        if (!User.usernameAlreadyExists(dataBase, username)) {
            System.out.println("{'status':'error','message':'User already exists'}");
            return;
        }

        // take password
        str = args[2];
        arrOfStr = str.split("'", 0);
        String userPassword = arrOfStr[1];

        User user = new User(username, userPassword);
        dataBase.users.add(user);
        System.out.println("{'status':'ok','message':'User created successfully'}");
        FileOp.writeUsersToFile(dataBase);
    }

    protected static void createQuestion(String[] args, DataBase dataBase) {

        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
            return;

        // check if the answers are provided accordingly
        if(!User.answersProvidedError(args))
            return;

        // creates a question with the given parameters
        Question question = Question.createQuestion(args, dataBase);
        if (question == null)
            return;

        // adds the answers to a question from the given parameters
        if (!question.addAnswersToQuestion(args))
            return;

        // provided answers do not meet the desired conditions
        if (!question.answerError())
            return;

        // add question to the questions array
        Question.population++;
        question.setId(Question.population);
        question.setPointsPerAnswer(); // sets every answer its corresponding points
        dataBase.questions.add(question); // adding question to database
        System.out.println("{ 'status' : 'ok', 'message' : 'Question added successfully'}");
        FileOp.writeQuestionsToFile(dataBase);
    }

    protected static void getQuestionIdByText(String[] args, DataBase dataBase) {

        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
            return;

        String[] arrOfStr = args[3].split("'",0);
        // create a Question instance with the given text in order to search for it in the question arrayList
        // question type doesnt matter so we are going to set it as "single"
        Question ref = new Question(arrOfStr[1], "single");

        int getQId = ref.returnQuestionId(dataBase);
        if (getQId < 0) {
            System.out.println("{ 'status' : 'error', 'message' : 'Question does not exist'}");
            return;
        }

        System.out.println("{ 'status' : 'ok', 'message' : '" + getQId + "'}");
    }

    protected static void getAllQuestions(String[] args, DataBase dataBase) {
        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
            return;

        System.out.print("{ 'status' : 'ok', 'message' : '[");
        for (int i = 0; i  < dataBase.questions.size(); i ++) {
            System.out.print(dataBase.questions.get(i).getQustion());
            if (i+1 < dataBase.questions.size())
                System.out.print(", ");
        }
        System.out.println("]'}");
    }

    public static void createQuizz(String[] args, DataBase dataBase) {
        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
            return;

        User currentUser = User.currentUser(args, dataBase);

         Quizz quizz = Quizz.createQuiz(args, dataBase);
         if (quizz == null)
            return;

         // add the desired questions to the quiz
         if (!quizz.addQuestionsToQuizz(args, dataBase))
             return;

         quizz.setId(dataBase);
         quizz.setOwnerID(dataBase, currentUser);
         dataBase.quizz.add(quizz);
         FileOp.writeQuizzToFile(dataBase);
         System.out.println("{ 'status' : 'ok', 'message' : 'Quizz added succesfully'}");
    }

    public static void getQuizzByName(String[] args, DataBase dataBase) {
        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
           return;

        // get quiz name
        String[] arrOfStrings = args[3].split("'", 0);
        String quizzName = arrOfStrings[1];

        Quizz quizz = new Quizz(quizzName);
        quizz.returnQuizzID(dataBase); // prints quizz id if found and error if not
    }

    public static void getAllQuizzes(String[] args, DataBase dataBase) {
        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
            return;

        User currentUser = User.currentUser(args, dataBase);

        Quizz.returnAllQuizzes(dataBase, currentUser);
    }

    public static void getQuizzDetailsByID(String[] args, DataBase dataBase) {
        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
            return;

        // get quiz ID
        String str = args[3];
        String[] arrOfStrings = str.split("'",0);
        int quizzID = Integer.parseInt(arrOfStrings[1]);
        Quizz quizz = Quizz.returnQuizzByID(quizzID, dataBase);

        if (quizz != null)
            quizz.returnQuizzDetails();

    }

    public static void submitQuizz(String[] args, DataBase dataBase) {

        // check if the username and password provided are correct
        if (!User.loginFailed(args, dataBase))
            return;

        Quizz quizz = Quizz.checkSubmittedQuizConditions(args, dataBase);
        if (quizz == null)
            return;

        User currentUser = User.currentUser(args, dataBase);

        double score = quizz.getQuizzResults(args, dataBase);
        if (score < 0) {
            System.out.println("{ 'status' : 'ok', 'message' : '0 points'}");
            return;
        }

        score *= 100;
        int finalScore = (int)round(score);
        System.out.println("{ 'status' : 'ok', 'message' : '" + finalScore + " points'}");

        // add this quiz to user's answered quizes
        currentUser.answeredQuizz.add(quizz);
        currentUser.quizzResults.add(finalScore);
        quizz.userHasAnswered.add(currentUser);
        FileOp.writeSolutionToFile(dataBase);
    }

     public static void deleteQuizzByID(String[] args, DataBase dataBase) {
         // check if the username and password provided are correct
         if (!User.loginFailed(args, dataBase))
             return;

         if (args.length < 4) {
             System.out.println("{ 'status' : 'error', 'message' : 'No quizz identifier was provided'}");
             return;
         }

         // take the quizz ID
         String[] arrOfStrings = args[3].split("'", 0);
         int quizzID = Integer.parseInt(arrOfStrings[1]);
         // search for the quiz using its ID and perform the "delete" operation if found
         Quizz.deleteQuizz(quizzID, dataBase);
     }

     public static void getMySolution(String[] args, DataBase dataBase) {
         // check if the username and password provided are correct
         if (!User.loginFailed(args, dataBase))
             return;

         User currentUser = User.currentUser(args, dataBase);
         currentUser.getSolutions();
     }

    public static void cleanup(DataBase dataBase) {
        dataBase.users.clear();
        for (Question question : dataBase.questions)
            question.answers.clear();
        dataBase.questions.clear();
        dataBase.quizz.clear();
        dataBase.answers.clear();

        FileOp.writeUsersToFile(dataBase);
        FileOp.writeQuestionsToFile(dataBase);
        FileOp.writeQuizzToFile(dataBase);
        FileOp.writeSolutionToFile(dataBase);

        Question.population = 0;
        User.population = 0;
        Answer.noAnswers = 0;

    }
}
