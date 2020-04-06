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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.ModelExtensionsKt;

import java.util.Optional;

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

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getIdBookTest() {
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        //execucao
        Optional<Book> foundBook = service.getById(id);

        //Verificacoes
        assertThat( foundBook.isPresent() ).isTrue();
        assertThat( foundBook.get().getId()).isEqualTo(id);
        assertThat( foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat( foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat( foundBook.get().getTitle()).isEqualTo(book.getTitle());

    }

    @Test
    @DisplayName("Deve retornar vazio ao obter livro com Id inexistente na base.")
    public void bookNotFoundByIdTest() {
        Long id = 1L;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        //execucao
        Optional<Book> foundBook = service.getById(id);

        //Verificacoes
        assertThat( foundBook.isPresent() ).isFalse();

    }

    @Test
    @DisplayName("Deletar um livro.")
    public void deleteBookTeste() {
        Book book = Book.builder().id(1L).build();

        //execucao
        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () ->  service.delete(book) );

        //verificacoes
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        //execucao
        //execucao
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->  service.delete(book) );

        //verificacoes
        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
    public void updateInvalidBookTest() {
        Book book = new Book();

        //execucao
        //execucao
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->  service.update(book) );

        //verificacoes
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public  void updateBookTest() {
        long id = 1L;

        //livro a atualizar
        Book updatingBook = Book.builder().id(id).build();

        //simulacao
        Book updateBook = createValidBook();
        updateBook.setId(id);
        Mockito.when(repository.save(updatingBook)).thenReturn(updateBook);

        //execucao
        Book book = service.update(updatingBook);

        //verificacoes
        assertThat(book.getId()).isEqualTo(updateBook.getId());
        assertThat(book.getTitle()).isEqualTo(updateBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updateBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updateBook.getAuthor());


    }

    private Book createValidBook() {
        return Book.builder().isbn("1234").author("Fulano").title("As aventuras").build();
    }


}
