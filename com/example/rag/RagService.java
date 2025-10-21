package com.example.rag;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;

/**
 * Orchestrates the RAG flow:
 *   1. Store documents + embeddings.
 *   2. For a user query:
 *      a) embed the query,
 *      b) find the top‑k similar docs,
 *      c) feed those docs + query into Ollama as a prompt,
 *      d) return the final answer.
 */
public class RagService {

    private final DatabaseService db;
    private final EmbeddingService embedder;
    private final int topK;          // number of retrieved docs

    public RagService(DatabaseService db, EmbeddingService embedder, int topK) {
        this.db = db;
        this.embedder = embedder;
        this.topK = topK;
    }

    /** Store a single document. */
    public void addDocument(String title, String content) throws IOException, InterruptedException, SQLException {
    	List<List<Double>> docVec = embedder.getEmbeddingService(content);
        db.upsertDocument(title, content, docVec);
    }

    /** Ask a question and get an answer that references the closest docs. */
    public String preparePromptWithRAG(String question) throws IOException, InterruptedException, SQLException {
        // 1️⃣ Embed the question
    	List<List<Double>> queryVec = embedder.getEmbeddingService(question);

        // 2️⃣ Retrieve the nearest docs
        List<Document> relevant = db.retrieveBySimilarity(queryVec, topK);

        // 3️⃣ Build the system prompt
        String context = relevant.stream()
                .map(d -> String.format("\n<doc>%s\n%s", d.getTitle(), d.getContent()))
                .collect(Collectors.joining());

        // 4️⃣ Compose the final prompt – here we simply append the question
        String finalPrompt = """
                You are a helpful assistant. The user asked:
                %s

                Context from the knowledge base:
                %s

                Provide an answer that cites the relevant documents in addtion to your trained data.
                Pls provide document name and where you find it.
                """.formatted(question, context);

        // 5️⃣ Run Ollama
        //return embedder.prompt(finalPrompt);
        return finalPrompt;

    }
}
