# Spring AI code samples
This repository contains a set of running code samples using Spring AI libraries, sourced from a few blogs, official docs, and a various conference videos and Spring CLI project catalogs. As much as possible some of the original sources are linked. The code has been updated or restructured to work together in this repo for the most part.

Following code examples are present:
1. Simple prompting REST controller
2. Externalized prompts
3. Document summarizer prompt example
4. Game Rules Q & A  - Text file reading RAG example with SimpleVectorStore
5. Simple PGVector RAG example
6. Local Ollama chat model integration with `gemma2:2b` model
7. Multiple ChatModel and VectorStore implementations used in same application
8. Spring AI actuator monitoring and tracing support
9. PDF reader PGVector Medicaid FAQs RAG example from the Spring AI team
10. Multimodal prompt Image Q&A example
11. Image generation prompt example
12. Evaluator example for prompt response verification
13. Simple Function call example
14. Function call example with external APIs


## PG Vector store setup in Docker
Following along with https://www.youtube.com/watch?v=aNKDoiOUo9M
Original code at https://github.com/spring-tips/llm-rag-with-spring-ai/

Using colima for Docker
```bash
brew install docker
brew install docker-compose
cd ai
docker compose up
brew install postico --cask 
brew install libpq
export PATH="/opt/homebrew/opt/libpq/bin:$PATH"
PGPASSWORD=postgres psql -U postgres -h localhost postgres
```
Inside psql shell
```
postgres=# \c vector_store
You are now connected to database "vector_store" as user "postgres".
vector_store=# \d
Did not find any relations.
vector_store=# \d
            List of relations
 Schema |     Name     | Type  |  Owner   
--------+--------------+-------+----------
 public | vector_store | table | postgres
(1 row)
vector_store=# select count(*) from vector_store;
 count 
-------
    13
(1 row)

vector_store=# \d vector_store
                     Table "public.vector_store"
  Column   |     Type     | Collation | Nullable |      Default       
-----------+--------------+-----------+----------+--------------------
 id        | uuid         |           | not null | uuid_generate_v4()
 content   | text         |           |          | 
 metadata  | json         |           |          | 
 embedding | vector(1536) |           |          | 
Indexes:
    "vector_store_pkey" PRIMARY KEY, btree (id)
    "vector_store_embedding_idx" hnsw (embedding vector_cosine_ops)


```
Add the Spring CLI `ai` project code
```shell
spring boot add ai
```
This created a new [AIController](src/main/java/dev/axymthr/springai/AIController.java) and the [README-ai.md](README-ai.md) file

Add the `ai-rag` project
```shell
spring boot add ai-rag
Getting project with URL https://github.com/rd-1-2022/ai-openai-rag
Refactoring code base that is to be merged to package name bootiful.ai

Done!
```
This added code and created a new [README-ai-rag.md](README-ai-rag.md) file
