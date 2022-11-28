package com.example.project;

import javax.swing.*;
import java.io.*;
import java.util.SortedMap;

import static java.lang.Math.log;
import static java.lang.Math.round;

public class Command {
    public static void createUser(String[] args, DataBase dataBase) {
        if(args.length == 1) {
            System.out.println("{ 'status' : 'error', 'message' : 'Please provide username'}");
            return;
        }
        if(args.length == 2) {
            System.out.println("{ 'status' : 'error', 'message' : 'Please provide password'}");
            return;
        }

        String str = args[1];
        String[] arrOfStr = str.split("'",0);
        String user_name = arrOfStr[1];

        // check if user already exists
        for(int i = 0; i < dataBase.users.size(); i++) {
            User ref = dataBase.users.get(i);
            if(ref.getUsername().equals(user_name)) {
                System.out.println("{'status':'error','message':'User already exists'}");
                return;
            }
        }

        str = args[2];
        arrOfStr = str.split("'", 0);
        String user_password = arrOfStr[1];

        User user = new User(user_name, user_password, true);
        dataBase.users.add(user);
        System.out.println("{'status':'ok','message':'User created successfully'}");
        FileOp.writeUsersToFile(dataBase);
    }

    public static void createQuestion(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

        if(args.length < 6) {
            System.out.println("{ 'status' : 'error', 'message' : 'No answer provided'}");
            return;
        }
        if(args.length < 8) {
            System.out.println("{ 'status' : 'error', 'message' : 'Only one answer provided'}");
            return;
        }
        if(args.length > 15) {

            System.out.println("{ 'status' : 'error', 'message' : 'More than 5 answers were submitted'}");
        }
        // take question's text
        str = args[3];
        arrOfStrings = str.split("'", 0);
        String parameter = arrOfStrings[0];
        String[] questionText = parameter.split(" ",0);
        if (!questionText[0].equals("-text")) {
            System.out.println("{ 'status' : 'error', 'message' : 'No question text provided'}");
            return;
        }
        String text = arrOfStrings[1];
        // take question's type
        str = args[4];
        arrOfStrings = str.split("'", 0);
        if(!arrOfStrings[1].equals("single") && !arrOfStrings[1].equals("multiple")) {
            System.err.println("Question type is not valid");
            return;
        }

        Question question = new Question(text, arrOfStrings[1]);
        check = question.checkIfQuestionExists(dataBase);
        if(check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Question already exists'}");
            return;
        }

        int answer_id = 1;
        // take each answer with its correct flag two by two until we reach end of args
        for (int i = 5; i < args.length; i++) {
         // first check whether the correct parameters are provided for answer description
            String ref = args[i];
            String[] arrOfStr = ref.split(" ",0);
            if (!arrOfStr[0].equals("-answer-"+answer_id)) {
                System.out.println("{ 'status' : 'error', 'message' : 'Answer "+ answer_id +" has no answer description'}");
                return;
            }
            Answer answer = new Answer();
            String[] answer_text = arrOfStr[1].split("'",0);
            answer.setText(answer_text[1]);

            // then check whether the correct parameters are provided for answer correct flag
            i++;
            if (i == args.length) {
                System.out.println("{'status':'error','message':'Answer " + answer_id +" has no answer correct flag'}");
                return;
            }
            ref = args[i];
            arrOfStr = ref.split(" ", 0);
            if (!arrOfStr[0].equals("-answer-"+answer_id+"-is-correct")) {
                System.out.println("{'status':'error','message':'Answer " + answer_id +" has no answer correct flag'}");
                return;
            }
            String[] takeCorrectFlag = arrOfStr[1].split("'",0);
            int correct_flag = Integer.parseInt(takeCorrectFlag[1]);
            if (correct_flag == 1)
                answer.setType(Answer.AnswerType.CORRECT);
            else if (correct_flag == 0)
                answer.setType(Answer.AnswerType.INCORRECT);
            else System.err.println("Provided correct flag is not acceptable!");

            Answer.noAnswers++;
            answer.setId(Answer.noAnswers);
            answer_id++;
            question.answers.add(answer);
        }
        // check whether single question has multiple correct answers
        check = true;
        if (question.getType().equals("single"))
            check = question.singleTypeQuestionHasMultipleCorrectAnswers();

        if (!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Single correct answer question has more than one correct answer'}");
            return;
        }

        // check whether question has duplicate answers
        check = question.duplicateAnswers();
        if (!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Same answer provided more than once'}");
            return;
        }

        question.population++;
        question.setId(question.population);
        question.setPointsPerAnswer(); // sets every answer its corresponding points
        dataBase.questions.add(question); // adding question to database
        System.out.println("{ 'status' : 'ok', 'message' : 'Question added successfully'}");
        FileOp.writeQuestionsToFile(dataBase);
    }

    public static void getQuestionIdByText(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }

        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        // take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

        String[] arrOfStr = args[3].split("'",0);
        // create a Question instance with the given text in order to seach for it in the question arrayList
        // question type doesnt matter so we are going to set it as "single"
        Question ref = new Question(arrOfStr[1], "single");

        int getQId = 0;
        getQId = ref.returnQuestionId(dataBase);
        if (getQId < 0) {
            System.out.println("{ 'status' : 'error', 'message' : 'Question does not exist'}");
            return;
        }

        System.out.println("{ 'status' : 'ok', 'message' : '" + getQId + "'}");
    }

    public static void getAllQuestions(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if (!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

        System.out.print("{ 'status' : 'ok', 'message' : '[");
        for (int i = 0; i  < dataBase.questions.size(); i ++) {
            System.out.print(dataBase.questions.get(i).getQustion());
            if (i+1 < dataBase.questions.size())
                System.out.print(", ");
        }
        System.out.println("]'}");
    }

    public static void createQuizz(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

        if (args.length > 14) {
            System.out.println("{ 'status' : 'error', 'message' : 'Quizz has more than 10 questions'}");
            return;
        }

        arrOfStrings = args[3].split("'", 0);
        String quizz_name = arrOfStrings[1];
        check = Quizz.checkQuizName(quizz_name, dataBase);
        if (!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Quizz name already exists'}");
            return;
        }

        Quizz quizz = new Quizz(quizz_name);

        // add the desired questions to the quiz
        for (int i = 4; i < args.length; i++) {
            arrOfStrings = args[i].split("'", 0);
            int questionId = Integer.parseInt(arrOfStrings[1]);
            int found = 0;
            for (Question question : dataBase.questions) {
                if (question.getId() == questionId) {
                    quizz.questions.add(question);
                    quizz.numberOfQuestions++;
                    found = 1;
                    break;
                }
            }
            if (found == 0) {
                System.out.println("{ 'status' : 'error', 'message' : 'Question ID for question " + (i - 3) + " does not exist'}");
                return;
            }
        }

        quizz.setId(dataBase);
        quizz.setOwnerID(dataBase, checkIfUserExists);
        dataBase.quizz.add(quizz);
        FileOp.writeQuizzToFile(dataBase);
        System.out.println("{ 'status' : 'ok', 'message' : 'Quizz added succesfully'}");
    }

    public static void getQuizzByName(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

        arrOfStrings = args[3].split("'", 0);
        String quizz_name = arrOfStrings[1];

        Quizz quizz = new Quizz(quizz_name);
        quizz.returnQuizzID(dataBase); // prints quizz id if found and error if not
    }

    public static void getAllQuizzes(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

       Quizz.returnAllQuizzes(dataBase, checkIfUserExists);
    }

    public static void getQuizzDetailsByID(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

        str = args[3];
        arrOfStrings = str.split("'",0);
        int quizz_id = Integer.parseInt(arrOfStrings[1]);
        Quizz quizz = Quizz.returnQuizzByID(quizz_id, dataBase);
        quizz.returnQuizzDetails();

    }

    public static void submitQuizz(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String user_name = arrOfStrings[1];
        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String user_password = arrOfStrings[1];

        User checkIfUserExists = new User(user_name, user_password, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return;
        }

        if (args.length < 4) {
            System.out.println("{ 'status' : 'error', 'message' : 'No quizz identifier was provided'}");
            return;
        }

        // take quizz id
        str = args[3];
        arrOfStrings = str.split("'", 0);
        int quizzID = Integer.parseInt(arrOfStrings[1]);
        // check existence of quizz
        Quizz quizz = Quizz.returnQuizzByID(quizzID, dataBase);
        // quiz id is not correct
        if (quizz == null) {
            System.out.println("{ 'status' : 'error', 'message' : 'No quiz was found'}");
            return;
        }

        // the user that created a quiz cannot answer it
        // search for the id of the currently logged in user
        int userLoggedInID = User.getUserID(user_name, user_password, dataBase);
        if (userLoggedInID == quizz.getOwnerID()) {
            System.out.println("{ 'status' : 'error', 'message' : 'You cannot answer your own quizz'}");
        }

        // check if this user has already submitted this quiz
        // first get the reference to the logged user
        User loggedUser = User.getUserByID(userLoggedInID, dataBase);
        check = quizz.checkIfUserCompletedThisQuizz(loggedUser);
        if (check) {
            // user has already answered this quiz
            System.out.println("{ 'status' : 'error', 'message' : 'You already submitted this quizz'}");
            return;
        }

        double score = quizz.getQuizzResults(args, dataBase);
        if (score < 0) {
            System.out.println("{ 'status' : 'ok', 'message' : '0 points'}");
            return;
        }

        score *= 100;
        int finalScore = (int)round(score);
        System.out.println("{ 'status' : 'ok', 'message' : '" + finalScore + " points'}");
        // add this quiz to user's answered quizes
        loggedUser.answeredQuizz.add(quizz);
        loggedUser.quizzResults.add(finalScore);
        quizz.userHasAnswered.add(loggedUser);
        FileOp.writeSolutionToFile(dataBase);
    }

     public static void deleteQuizzByID(String[] args, DataBase dataBase) {
         // first check if -u and -p are given
         if (args.length < 3) {
             System.out.println("{'status':'error','message':'You need to be authenticated'}");
             return;
         }
         // take username
         String str = args[1];
         String[] arrOfStrings = str.split("'", 0);
         String user_name = arrOfStrings[1];
         //take password
         str = args[2];
         arrOfStrings = str.split("'", 0);
         String user_password = arrOfStrings[1];

         User checkIfUserExists = new User(user_name, user_password, 0);
         boolean check = checkIfUserExists.loginAttempt(dataBase);
         if(!check) {
             System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
             return;
         }

         if (args.length < 4) {
             System.out.println("{ 'status' : 'error', 'message' : 'No quizz identifier was provided'}");
             return;
         }

         // take the quizz ID
         arrOfStrings = args[3].split("'", 0);
         int quizzID = Integer.parseInt(arrOfStrings[1]);
         // search for the quiz using its ID and perform the "delete" operation if found
         Quizz.deleteQuizz(quizzID, dataBase);
     }

     public static void getMySolution(String[] args, DataBase dataBase) {
         // first check if -u and -p are given
         if (args.length < 3) {
             System.out.println("{'status':'error','message':'You need to be authenticated'}");
             return;
         }
         // take username
         String str = args[1];
         String[] arrOfStrings = str.split("'", 0);
         String user_name = arrOfStrings[1];
         //take password
         str = args[2];
         arrOfStrings = str.split("'", 0);
         String user_password = arrOfStrings[1];

         User checkIfUserExists = new User(user_name, user_password, 0);
         boolean check = checkIfUserExists.loginAttempt(dataBase);
         if(!check) {
             System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
             return;
         }

         int userID = User.getUserID(user_name, user_password, dataBase);
         User userLoggedIn = User.getUserByID(userID, dataBase);
         userLoggedIn.getSolutions();
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
