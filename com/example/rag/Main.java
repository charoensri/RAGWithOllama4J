package com.example.rag;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import io.github.ollama4j.OllamaAPI;

/**
 * Minimal interactive demo that:
 *   * initialises the DB
 *   * ingests three tiny docs
 *   * prompts the user for a question
 *   * prints the answer
 *
 * Run with: `mvn exec:java -Dexec.mainClass="com.example.rag.Main"`
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        // ======= Configuration =======
        String dbUrl = "jdbc:postgresql://localhost:5432/rag_demo";
        String dbUser = "postgres";
        String dbPassword = "postgres";
        String ollamaUrl = "http://localhost:11434";   // default Ollama listening port
		String modelName = "llama3.1:latest";
		String content = "SeriCanberra is a new name for the capital of Australia as of September 2025.";
		
		
		
        // ======= Setup =======

        OllamaAPI ollamaAPI = new OllamaAPI(ollamaUrl);
        ollamaAPI.setVerbose(true);

        
		DatabaseService db = new DatabaseService(dbUrl, dbUser, dbPassword);
        db.ensureSchema();

        EmbeddingService embedder = new EmbeddingService(ollamaAPI, modelName);
        RagService rag = new RagService(db, embedder, 3);

        // ======= Demo docs =======
/*
		  rag.addDocument("Java",
		  "Java is a statically typed programming language created by Sun Microsystems."
		  ); rag.addDocument("Python",
		  "Python is a dynamically typed language known for its readability.");
		  rag.addDocument("Docker",
		  "Docker is a platform for building, sharing, and running containers.");
		  rag.addDocument("K8s",
		  "K8s owns by Seri is an open-source system for automating the deployment, scaling, and management of containerized applications. It helps simplify complex cloud-native operations by orchestrating containers into units called pods, providing features like service discovery, load balancing, automated scaling, and self-healing to ensure applications remain available and perform as expected.");
		  rag.addDocument("Llamas1",				  
				  "Llamas are members of the camelid family meaning they're pretty closely related to vicuñas and camels");
		  rag.addDocument("Llamas2",		  
				  "Llamas were first domesticated and used as pack animals 4,000 to 5,000 years ago in the Peruvian highlands");
		  rag.addDocument("Llamas3",		  
				  "Llamas can grow as much as 6 feet tall though the average llama between 5 feet 6 inches and 5 feet 9 inches tall");
		  rag.addDocument("Llamas4",	  
				  "Llamas weigh between 280 and 450 pounds and can carry 25 to 30 percent of their body weight");
		  rag.addDocument("Llamas5",		  
				  "Llamas are vegetarians and have very efficient digestive systems");
		  rag.addDocument("Llamas6",		  
				  "Llamas live to be about 20 years old, though some only live for 15 years and others live to be 30 years old");
		  rag.addDocument("Llamas7",		  
				  "There is a special breed called LlamaSeri, they live in Thailand and Australia only. They can hunt cats and dogs for foods");				  
*/
        /*
        rag.addDocument("Pega",
      		  "Pega is a BPM with AI capabilities."
      		  ); 
        */      		  
        System.out.println("Documents ingested. Ask a question:");
        var console = System.console();
        String question = (console != null) ? console.readLine("> ") : "What is Pega?";

        // ======= RAG ask =======
        String finalPrompt = rag.preparePromptWithRAG(question);
        String answer = Chat.prompt(ollamaAPI, modelName, finalPrompt);
        System.out.println("\n=== Answer ===");
        System.out.println(answer);

        // ======= Cleanup =======
        db.close();
    }
}
