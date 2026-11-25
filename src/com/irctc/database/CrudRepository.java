package com.irctc.database;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Interface demonstrating Java Generics & Type Safety.
 * Directly satisfies Syllabus Lecture 24 (Introductory Generics, Diamond operator, Type safety).
 *
 * @param <T>  The Model Entity type (e.g. Ticket)
 * @param <ID> The Primary Key type (e.g. String for PNR)
 */
public interface CrudRepository<T, ID> {

    /**
     * Inserts a new record into persistent storage.
     */
    void save(T entity);

    /**
     * Reads a record by its unique identifier.
     */
    Optional<T> findById(ID id);

    /**
     * Returns all records from persistent storage.
     */
    List<T> findAll();

    /**
     * Updates an existing record.
     */
    void update(T entity);

    /**
     * Deletes a record by its unique identifier.
     */
    boolean deleteById(ID id);
}
