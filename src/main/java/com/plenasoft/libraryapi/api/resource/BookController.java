package com.plenasoft.libraryapi.api.resource;

import com.plenasoft.libraryapi.api.dto.BookDTO;
import com.plenasoft.libraryapi.api.exception.ApiErros;
import com.plenasoft.libraryapi.exception.BusinessException;
import com.plenasoft.libraryapi.model.entity.Book;
import com.plenasoft.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper mapper;

    public BookController(BookService service, ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        Book entity = mapper.map( dto, Book.class );
        entity = service.save(entity);
       return mapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    public BookDTO get( @PathVariable Long id) {
        return service
                .getById(id)
                .map ( book -> mapper.map(book, BookDTO.class) )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
        service.delete(book);

    }

    @PutMapping("{id}")
    public BookDTO update (@PathVariable Long id, BookDTO dto) {
        return service.getById(id).map( book -> {

            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            book  = service.update(book);
            return mapper.map(book, BookDTO.class);

        }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );


    }


}
