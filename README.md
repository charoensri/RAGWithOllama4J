
**1. Prerequisites**

Java 17 (or any 11‑+ LTS), ollama‑4j targets Java 17+

Maven 3.6+ Build tool

PostgreSQL **15.0+ (or 14+ with pgvector)** Need pgvector for vector similarity

Ollama 0.2+ (or newer) Provides embedding & generation endpoints

Ollama‑4j 0.5+ Java wrapper for Ollama HTTP API


**1.1 Install PostgreSQL & pgvector**
** Debian/Ubuntu**
sudo apt-get install postgresql postgresql-contrib
sudo -u postgres psql -c "CREATE EXTENSION IF NOT EXISTS vector;"

**PostgreSQL 15+ ships pgvector by default
If you’re on 14, install pgvector:
   CREATE EXTENSION vector;**
   
Create a database and user (adjust as needed):
sudo -u postgres psql
CREATE DATABASE rag_demo;
CREATE USER rag_user WITH PASSWORD 'strong!Pass';
GRANT ALL PRIVILEGES ON DATABASE rag_demo TO rag_user;
\q


1.2 Install Ollama
curl -fsSL https://ollama.com/install.sh | sh
ollama run llama3.1  # pull a model (or any model you prefer)


-- Table: public.documents

-- DROP TABLE IF EXISTS public.documents;

CREATE TABLE IF NOT EXISTS public.documents
(
    id bigint NOT NULL DEFAULT nextval('documents_id_seq'::regclass),
    title text COLLATE pg_catalog."default",
    content text COLLATE pg_catalog."default" NOT NULL,
   ** embedding vector(4096),**
    CONSTRAINT documents_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.documents
    OWNER to postgres;

//==============


chars1@L47178WAP:/$ sudo -u postgres psql -d rag_demo
psql (17.5 (Ubuntu 17.5-1.pgdg22.04+1))
Type "help" for help.

rag_demo=# \dx
                             List of installed extensions
  Name   | Version |   Schema   |                     Description
---------+---------+------------+------------------------------------------------------
 plpgsql | 1.0     | pg_catalog | PL/pgSQL procedural language
 vector  | 0.8.1   | public     | vector data type and ivfflat and hnsw access methods
(2 rows)

rag_demo=#

rag_demo=# select id from documents;
 id
----
 32
 33
 34
 35
(4 rows)

rag_demo=# select id, title, content from documents;
rag_demo=# select id, title, content from documents;
rag_demo=#
rag_demo=# select id, title from documents;
 id | title
----+--------
 32 | Java
 33 | Python
 34 | Docker
 35 | K8s
(4 rows)

rag_demo=# select id, title, content from documents;
rag_demo=#

**POM:**

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
           http://maven.apache.org/POM/4.0.0
           https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- JitPack is where ollama4j lives -->
  <repositories>    
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
  
    <groupId>com.example</groupId>
    <artifactId>rag-postgres-ollama</artifactId>
    <version>0.1.0</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- JDBC driver for Postgres -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.3</version>
        </dependency>
        
        <!-- ollama4j: Java client for Ollama -->
        <dependency>
            <groupId>io.github.ollama4j</groupId>
            <artifactId>ollama4j</artifactId>
             <version>1.1.0</version>
        </dependency>
             
<!--
<dependency>
  <groupId>com.github.dreamhead</groupId>
  <artifactId>ollama4j</artifactId>
  <version>1.0.0</version>
  <scope>compile</scope>
</dependency>
-->        
        <!-- Jackson for JSON processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.18.0</version>
        </dependency>

        <!-- SLF4J simple for logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.16</version>
        </dependency>
    </dependencies>
</project>
