package com.example.rag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.postgresql.util.PGobject;

/**
 * Thin wrapper around JDBC that knows how to
 *   * create the schema (pgvector + documents table)
 *   * upsert a document + embedding
 *   * retrieve top‑k nearest documents
 */
public class DatabaseService {

    private final Connection conn;

    public DatabaseService(String url, String user, String password) throws SQLException {
        this.conn = DriverManager.getConnection(url, user, password);
        this.conn.setAutoCommit(false);
    }

    /** Create extension + table if they don't exist. */
    public void ensureSchema() throws SQLException {
        String sql = """
            CREATE EXTENSION IF NOT EXISTS vector;
            CREATE TABLE IF NOT EXISTS documents (
                id        SERIAL PRIMARY KEY,
                title     TEXT NOT NULL,
                content   TEXT NOT NULL,
                embedding vector(1536)
            );
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            conn.commit();
        }
    }

    /** Insert or replace a document together with its embedding. */
    public void upsertDocument(String title, String content, List<List<Double>> embedding) throws SQLException {
        String sql = """
            INSERT INTO documents (title, content, embedding)
            VALUES (?, ?, ?)
            ON CONFLICT (id) DO UPDATE
                SET title = EXCLUDED.title,
                    content = EXCLUDED.content,
                    embedding = EXCLUDED.embedding
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, content);
            // JDBC array of float8 for pgvector
            //Array pgArray = conn.createArrayOf("float8", toObjectArray(embedding));
            
			/*
			 * Array array = conn.createArrayOf("vector", embedding.stream() .map(v -> {
			 * StringBuilder sb = new StringBuilder("["); for (int i = 0; i < v.length; i++)
			 * { sb.append(v[i]).append(i + 1 == v.length ? "]" : ","); } return
			 * sb.toString(); }) .toArray(String[]::new)); 
			 * ps.setArray(3, pgArray);
			 */
            
            PGobject pgVector = new PGobject();
            pgVector.setType("vector");
            //pgVector.setValue("[1.0,2.0,3.0]");
            String embeddingsString = embedding.toString();
            pgVector.setValue(embeddingsString.substring(1, embeddingsString.length() - 1));  
            ps.setObject(3, pgVector);
            ps.executeUpdate();
            conn.commit();
        }
    }

    /** Return top‑k documents closest to the query vector. */
    public List<Document> retrieveBySimilarity(List<List<Double>> queryVector, int k) throws SQLException {
        String sql = """
            SELECT id, title, content
            FROM documents
            ORDER BY embedding <-> ?
            LIMIT ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            //Array pgArray = conn.createArrayOf("float8", toObjectArray(queryVector));
            //ps.setArray(1, pgArray);
            PGobject pgVector = new PGobject();
            pgVector.setType("vector");
            //pgVector.setValue("[1.0,2.0,3.0]");
            String queryVectorString = queryVector.toString();
            pgVector.setValue(queryVectorString.substring(1, queryVectorString.length() - 1));
            ps.setObject(1, pgVector);
            ps.setInt(2, k);

            ResultSet rs = ps.executeQuery();
            List<Document> docs = new ArrayList<>();
            while (rs.next()) {
                docs.add(new Document(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content")
                ));
            }
            return docs;
        }
    }

    /** Convert double[] to Double[] (required by JDBC). */
    private static List<double[]> toObjectArray(List<List<Double>> vec) {
    	List<double[]> vectors = vec.stream()
    	        .map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
    	        .collect(Collectors.toList());
        return vectors;
    }

    public void close() throws SQLException {
        conn.close();
    }
}
