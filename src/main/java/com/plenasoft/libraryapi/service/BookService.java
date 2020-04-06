package com.plenasoft.libraryapi.service;

import com.plenasoft.libraryapi.model.entity.Book;

import java.util.Optional;

public interface BookService {

    Book save(Book any);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

    Optional<Book> getBookByIsbn(String isbn);
}
