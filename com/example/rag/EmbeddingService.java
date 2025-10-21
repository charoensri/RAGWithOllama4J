package com.example.rag;

/*
import com.github.dreamhead.ollama4j.OllamaClient;
import com.github.dreamhead.ollama4j.model.EmbeddingRequest;
import com.github.dreamhead.ollama4j.model.EmbeddingResponse;
import com.github.dreamhead.ollama4j.model.PromptRequest;
import com.github.dreamhead.ollama4j.model.PromptResponse;
*/

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.embeddings.OllamaEmbedRequestBuilder;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;



import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Wraps Ollama4j for two purposes:
 *   * generate embeddings (double[])
 *   * generate completions (prompt + streaming optional)
 *
 * The default model is `llama3:8b`, but you can override it
 * via the constructor or by passing a custom model name to the methods.
 */
public class EmbeddingService {
	private String ollamaUrl;
	private String modelName;
	private String content;
	OllamaAPI ollamaAPI = null;
	
	public static void main(String[] args) throws IOException, OllamaBaseException, InterruptedException {
		/*
		 * EmbeddingService ESvc = new EmbeddingService(); List<List<Double>> listEmd =
		 * ESvc.getEmbeddingService("Seri is learning embedding");
		 * System.out.println("\r\n ----------------- "+listEmd);
		 */
		String ollamaUrl = "http://localhost:11434/";
		String modelName = "llama3.1:latest";
		String content = "this is my text123 by Seri learning embedding ABC";
		
		EmbeddingService EmbSvc = new EmbeddingService(ollamaUrl, modelName);
		List<List<Double>> vector = EmbSvc.getEmbeddingService(content);
		System.out.println("\r\n ----------------- "+vector);
	}

	public EmbeddingService(OllamaAPI ollamaAPI, String modelName) {
		setOllamaAPI(ollamaAPI);
		setModelName(modelName);
	}



	public EmbeddingService(String ollamaUrl, String modelName) {
		setOllamaUrl(ollamaUrl);
		setModelName(modelName);
	    ollamaAPI = new OllamaAPI(getOllamaUrl());
	}
	
	 public List<List<Double>> getEmbeddingService() {
           OllamaEmbedResponseModel embeddings = null;
			try {
				embeddings = ollamaAPI.embed(getModelName(), Arrays.asList(getContent()));
			} catch (IOException | InterruptedException | OllamaBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

         //System.out.println(embeddings.getEmbeddings());
         return embeddings.getEmbeddings();
     }	 
	 
    public  List<List<Double>> getEmbeddingService(String ollamaUrl, String modelName, String content) {
  
            OllamaAPI ollamaAPI = new OllamaAPI(ollamaUrl);

            OllamaEmbedResponseModel embeddings = null;
			try {
				embeddings = ollamaAPI.embed(modelName, Arrays.asList(content));
			} catch (IOException | InterruptedException | OllamaBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            //System.out.println(embeddings.getEmbeddings());
            return embeddings.getEmbeddings();
        }
    
    
    public  List<List<Double>> getEmbeddingService(String content) {
         OllamaEmbedResponseModel embeddings = null;
			try {
				embeddings = ollamaAPI.embed(getModelName(), Arrays.asList(content));
			} catch (IOException | InterruptedException | OllamaBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

         //System.out.println(embeddings.getEmbeddings());
         return embeddings.getEmbeddings();
     }
    
	public String getOllamaUrl() {
		return ollamaUrl;
	}


	public void setOllamaUrl(String ollamaUrl) {
		this.ollamaUrl = ollamaUrl;
	}


	public String getModelName() {
		return modelName;
	}


	public void setModelName(String modelName) {
		this.modelName = modelName;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}

	public OllamaAPI getOllamaAPI() {
		return ollamaAPI;
	}

	public void setOllamaAPI(OllamaAPI ollamaAPI) {
		this.ollamaAPI = ollamaAPI;
	}
}
