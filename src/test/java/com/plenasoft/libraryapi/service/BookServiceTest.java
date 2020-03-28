package com.plenasoft.libraryapi.service;

import com.plenasoft.libraryapi.exception.BusinessException;
import com.plenasoft.libraryapi.model.entity.Book;
import com.plenasoft.libraryapi.model.repository.BookRepository;
import com.plenasoft.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("Salvar um livro")
    public void saveBookTest() {
        //cenario
        Book book = createValidBook();
        Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(false);
        Mockito.when( repository.save(book) ).thenReturn(
                Book.builder()
                        .id(11L)
                        .isbn("1234")
                        .author("Fulano")
                        .title("As aventuras").build()
        );
        //execucao
        Book saveBook = service.save(book);

        //verificacao
        assertThat(saveBook.getId()).isNotNull();
        assertThat(saveBook.getIsbn()).isEqualTo("1234");
        assertThat(saveBook.getTitle()).isEqualTo("As aventuras");
        assertThat(saveBook.getAuthor()).isEqualTo("Fulano");


    }



    @Test
    @DisplayName("Erro de negocio ao salvar livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicateISBN() {
        //Cenario
        Book book = createValidBook();
        Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(true);


        //Execucao
        Throwable exception = Assertions.catchThrowable( () -> service.save(book) );

        //Verificacoes
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);

    }

    private Book createValidBook() {
        return Book.builder().isbn("1234").author("Fulano").title("As aventuras").build();
    }


}
