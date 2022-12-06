package com.example.project;

import java.io.File;
import java.util.ArrayList;

public class Tema1 {

	public static void main(final String[] args)
	{
		if(args == null)
		{
			System.out.print("Hello world!");
			return;
		}

		// set up database
		DataBase dataBase = new DataBase();
		FileOp.readUserFromFile(dataBase);
		FileOp.readQuestionFromFile(dataBase);
		FileOp.readQuizzFromFile(dataBase);
		FileOp.readSolutionFromFile(dataBase);

		// command line
		switch (args[0]) {
			case "-create-user":
				Command.createUser(args, dataBase);
				break;
			case "-create-question":
				Command.createQuestion(args, dataBase);
				break;
			case "-get-question-id-by-text":
				Command.getQuestionIdByText(args, dataBase);
				break;
			case "-get-all-questions":
				Command.getAllQuestions(args, dataBase);
				break;
			case "-create-quizz":
				Command.createQuizz(args, dataBase);
				break;
			case "-get-quizz-by-name":
				Command.getQuizzByName(args, dataBase);
				break;
			case "-get-all-quizzes":
				Command.getAllQuizzes(args, dataBase);
				break;
			case "-get-quizz-details-by-id":
				Command.getQuizzDetailsByID(args, dataBase);
				break;
			case "-submit-quizz":
				Command.submitQuizz(args, dataBase);
				break;
			case "-delete-quizz-by-id":
				Command.deleteQuizzByID(args,dataBase);
				break;
			case "-get-my-solutions":
				Command.getMySolution(args, dataBase);
				break;
			case "-cleanup-all":
				Command.cleanup(dataBase);
				break;
		}


	}

}
