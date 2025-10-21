package com.example.rag;


import java.io.IOException;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.exceptions.ToolInvocationException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;

public class Chat {

	
    public static void main(String[] args) throws Exception {
        String host = "http://localhost:11434/";

        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setVerbose(true);

        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance("gpt-oss:20b");

        // create first user question
        OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.USER, "What is the capital of France?")
                .build();

        // start conversation with model
        OllamaChatResult chatResult = ollamaAPI.chat(requestModel);

        System.out.println("First answer: " + chatResult.getResponseModel().getMessage().getContent());

        // create next userQuestion
        requestModel = builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, "And what is the second largest city?").build();

        // "continue" conversation with model
        chatResult = ollamaAPI.chat(requestModel);

        System.out.println("Second answer: " + chatResult.getResponseModel().getMessage().getContent());

        System.out.println("Chat History: " + chatResult.getChatHistory());
    }
    
    public static String prompt(OllamaAPI ollamaAPI, String modelName, String prompt ) {
    	   OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(modelName);

           // create first user question
           OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.USER, prompt)
                   .build();

           // start conversation with model
           OllamaChatResult chatResult = null;
			try {
				chatResult = ollamaAPI.chat(requestModel);
			} catch (OllamaBaseException | IOException | InterruptedException | ToolInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

           System.out.println("First answer: " + chatResult.getResponseModel().getMessage().getContent());

           // create next userQuestion
           //requestModel = builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, "And what about Kubernetes?").build();
           requestModel = builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, "And what about LlamaSeri that eats cats or dogs?").build();

           // "continue" conversation with model
           try {
			chatResult = ollamaAPI.chat(requestModel);
		   } catch (OllamaBaseException | IOException | InterruptedException | ToolInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		   }

           System.out.println("Second answer: " + chatResult.getResponseModel().getMessage().getContent());

           System.out.println("Chat History: " + chatResult.getChatHistory());
           
           return chatResult.getResponseModel().getMessage().getContent();
    }
}
