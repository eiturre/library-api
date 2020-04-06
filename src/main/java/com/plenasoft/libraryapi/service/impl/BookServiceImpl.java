package com.plenasoft.libraryapi.service.impl;

import com.plenasoft.libraryapi.exception.BusinessException;
import com.plenasoft.libraryapi.model.entity.Book;
import com.plenasoft.libraryapi.model.repository.BookRepository;
import com.plenasoft.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if( repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("Isbn já cadastrado.");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {

        if(book == null || book.getId() == null) {
            throw new IllegalArgumentException("Livro não pode retornar nulo.");
        }
        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book == null || book.getId() == null) {
            throw new IllegalArgumentException("Livro não pode retornar nulo.");
        }
        return this.repository.save(book);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return null;
    }
}
