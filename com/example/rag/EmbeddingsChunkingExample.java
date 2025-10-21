package com.example.rag;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import io.github.ollama4j.utils.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmbeddingsChunkingExample {
    
    private static final String OLLAMA_HOST = "http://localhost:11434";
    private static final String MODEL = "nomic-embed-text"; // or "all-minilm" or "mxbai-embed-large"
    
    public static void main(String[] args) {
        try {
            OllamaAPI ollamaAPI = new OllamaAPI(OLLAMA_HOST);
            ollamaAPI.setRequestTimeoutSeconds(60);
            
            // Sample long text to chunk
            String longText = 
            		"Artificial intelligence is transforming industries. " + 
                    "Machine learning models can process vast amounts of data. " +
                    "Natural language processing enables computers to understand human language. " +
                    "Deep learning has revolutionized computer vision and speech recognition. " +
                    "AI ethics and responsible development are crucial considerations.";
            
            // Chunk the text
            int chunkSize = 100; // characters per chunk
            int overlap = 20; // overlap between chunks
            
            List<String> chunks = chunkText(longText, chunkSize, overlap);
            //List<String> chunks = chunkBySentences(longText, 1);
            //List<String> chunks = AdvancedChunking.chunkByTokens(longText, 10);
            //List<String> chunks  = AdvancedChunking.chunkByParagraphs(longText , 1);
            
            System.out.println("Created " + chunks.size() + " chunks:\n");
            
            // Generate embeddings for each chunk
            List<List<Double>> myChunkEmbeddings = new ArrayList<>();
            List<Double> chunkEmbeddings = null;
            
            OllamaEmbedResponseModel embeddings =  new OllamaEmbedResponseModel(); //new ArrayList<>();
            
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                System.out.println("Chunk " + (i + 1) + ": " + chunk);
                
                // Generate embedding
                //OllamaEmbedResponseModel response = ollamaAPI.generateEmbeddings(MODEL, chunk);
                //List<Double> embedding = response.getEmbeddings();
                //embeddings.add(embedding);
              
                embeddings = ollamaAPI.embed(MODEL, Arrays.asList(chunk));
                //myChunkEmbeddings  =  embeddings.getEmbeddings();
                chunkEmbeddings = embeddings.getEmbeddings().get(0); 
                System.out.println("chunkEmbeddings Dimensions: " + chunkEmbeddings.size());    //nomic-embed-text has 768 size or dimensions            
                System.out.println("embeddings Dimensions: " + embeddings.getEmbeddings().size());

                myChunkEmbeddings.add(chunkEmbeddings);
                
             
                
                System.out.println("Embedding dimensions: " + myChunkEmbeddings.size());
                System.out.println("First 5 values: " + myChunkEmbeddings.subList(0, Math.min(5, myChunkEmbeddings.size())));
                System.out.println();
            }
            
            // Example: Calculate similarity between chunks
            if (myChunkEmbeddings.size() >= 2) {
                double similarity = cosineSimilarity(myChunkEmbeddings.get(0), myChunkEmbeddings.get(1));
                System.out.println("Similarity between chunk 1 and 2: " + similarity);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Chunk text with overlapping windows
     */
    public static List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            
            // Try to break at word boundary
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }
            
            chunks.add(text.substring(start, end).trim());
            start = end - overlap;
            
            // Prevent infinite loop
            if (start <= 0 || end >= text.length()) {
                break;
            }
        }
        
        return chunks;
    }
    
    /**
     * Alternative: Chunk by sentences
     */
    public static List<String> chunkBySentences(String text, int sentencesPerChunk) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        StringBuilder chunk = new StringBuilder();
        int count = 0;
        
        for (String sentence : sentences) {
            chunk.append(sentence).append(" ");
            count++;
            
            if (count >= sentencesPerChunk) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder();
                count = 0;
            }
        }
        
        if (chunk.length() > 0) {
            chunks.add(chunk.toString().trim());
        }
        
        return chunks;
    }
    
    /**
     * Calculate cosine similarity between two embeddings
     */
    public static double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must have same dimensions");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += Math.pow(vec1.get(i), 2);
            norm2 += Math.pow(vec2.get(i), 2);
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}

// Alternative chunking strategies for different use cases
class AdvancedChunking {
    
    /**
     * Chunk by token count (approximate)
     */
    public static List<String> chunkByTokens(String text, int maxTokens) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        
        StringBuilder chunk = new StringBuilder();
        int tokenCount = 0;
        
        for (String word : words) {
            // Rough approximation: 1 word ≈ 1.3 tokens
            int wordTokens = (int) Math.ceil(word.length() / 4.0);
            
            if (tokenCount + wordTokens > maxTokens && chunk.length() > 0) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder();
                tokenCount = 0;
            }
            
            chunk.append(word).append(" ");
            tokenCount += wordTokens;
        }
        
        if (chunk.length() > 0) {
            chunks.add(chunk.toString().trim());
        }
        
        return chunks;
    }
    
    /**
     * Semantic chunking by paragraphs
     */
    public static List<String> chunkByParagraphs(String text, int maxParagraphs) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");
        
        StringBuilder chunk = new StringBuilder();
        int count = 0;
        
        for (String para : paragraphs) {
            if (para.trim().isEmpty()) continue;
            
            chunk.append(para).append("\n\n");
            count++;
            
            if (count >= maxParagraphs) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder();
                count = 0;
            }
        }
        
        if (chunk.length() > 0) {
            chunks.add(chunk.toString().trim());
        }
        
        return chunks;
    }
}