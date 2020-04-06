package com.plenasoft.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plenasoft.libraryapi.api.dto.BookDTO;
import com.plenasoft.libraryapi.exception.BusinessException;
import com.plenasoft.libraryapi.model.entity.Book;
import com.plenasoft.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    //Criar instancia
    @MockBean
    BookService service;



    @Test
    @DisplayName("Criar um livro com sucesso.")
    public void createBookTeste() throws Exception {

        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(10L).author("Artur").title("Aventuras").isbn("001").build();
        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
           .perform(request)
           .andExpect( status().isCreated() )
           .andExpect( jsonPath("id").isNotEmpty() )
           .andExpect( jsonPath("title").value(dto.getTitle()) )
           .andExpect( jsonPath("author").value(dto.getAuthor()) )
           .andExpect( jsonPath("isbn").value(dto.getIsbn()) )
        ;
    }



    @Test
    @DisplayName("Erro de validação quando não houver dados suficientes para criação do livro.")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest())
                .andExpect( jsonPath( "errors", hasSize(3)));
    }

    @Test
    @DisplayName("Lançar erro ao cadastrar livro com isbn já utilizado por outro.")
    public void createBookWithDuplicatedIsbn() throws  Exception {

        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);
        String mensagemErro = "Isbn já cadastrado.";

        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException("Isbn já cadastrado."));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));


    }

    @Test
    @DisplayName("Deve obter informacoes de um livro")
    public void getBookDetailsTest() throws Exception {

        //Cenario
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/"+id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isOk())
            .andExpect( jsonPath("id").value(id) )
            .andExpect( jsonPath("title").value(createNewBook().getTitle()) )
            .andExpect( jsonPath("author").value(createNewBook().getAuthor()) )
            .andExpect( jsonPath("isbn").value(createNewBook().getIsbn()) )
        ;


    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro")
    public void bookNotFoundTeste() throws Exception {

        //Cenario
       BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/"+1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTeste() throws Exception {

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/"+1));

        mvc.perform(request)
                .andExpect( status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar livro para deletar.")
    public void notBookFoundForDelete() throws Exception {

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/"+1));

        mvc.perform(request)
                .andExpect( status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar livro.")
    public void updateBookTeste() throws Exception {
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updateBook = Book.builder()
                .id(1L)
                .title("some titel")
                .author("some author")
                .isbn("80890")
                .build();

        BDDMockito.given( service.getById(id) )
                .willReturn( Optional.of(updateBook));

        Book updatedBook = Book.builder().id(id).author("Artur").title("Aventuras").isbn("80890").build();

        BDDMockito.given(service.update(updateBook)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/"+1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
            .andExpect( status().isOk())
            .andExpect( jsonPath("id").value(id) )
            .andExpect( jsonPath("title").value(createNewBook().getTitle()) )
            .andExpect( jsonPath("author").value(createNewBook().getAuthor()) )
            .andExpect( jsonPath("isbn").value("80890") );

    }

    @Test
    @DisplayName("Deve retornar 404 ao tenttar atualizar livro inexistente.")
    public void notBookFoundForUpdate() throws Exception {


        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given( service.getById(Mockito.anyLong()) )
                .willReturn( Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/"+1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect( status().isNotFound() );
    }

    private BookDTO createNewBook() {
        return BookDTO.builder().author("Artur").title("Aventuras").isbn("001").build();
    }
}
