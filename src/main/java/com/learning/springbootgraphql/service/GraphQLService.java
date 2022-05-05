package com.learning.springbootgraphql.service;

import com.learning.springbootgraphql.model.Book;
import com.learning.springbootgraphql.repository.BookRepository;
import com.learning.springbootgraphql.service.datafetcher.AllBookDataFetcher;
import com.learning.springbootgraphql.service.datafetcher.BookDataFetcher;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

@Service
public class GraphQLService {

    private GraphQL graphQL;

    @Value("classpath:book.graphql")
    Resource resource;

    @Autowired
    private AllBookDataFetcher allBookDataFetcher;

    @Autowired
    private BookDataFetcher bookDataFetcher;

    @Autowired
    private BookRepository bookRepository;

    @PostConstruct
    public void loadSchema() throws IOException {
        loadDataInDB();

        File schemaFile = resource.getFile();
        TypeDefinitionRegistry typeDefinitionRegistory = new SchemaParser().parse(schemaFile);
        RuntimeWiring wiring = buildRuntimeWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistory, wiring);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    private RuntimeWiring buildRuntimeWiring () {
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring.dataFetcher("allBook", allBookDataFetcher)
                        .dataFetcher("book", bookDataFetcher))
                .build();
    }

    public GraphQL getGraphQL() {
        return graphQL;
    }

    private void loadDataInDB() {
        Stream.of(
                new Book("123", "Book of Clouds", "Kindle Edition",
                        new String[] {
                                "Chloe Aridjis"
                        }, "Nov 2017"),
                new Book("124", "Cloud Arch & Engineering", "Orielly",
                        new String[] {
                                "Peter", "Sam"
                        }, "Jan 2015"),
                new Book("125", "Java 9 Programming", "Orielly",
                        new String[] {
                                "Venkat", "Ram"
                        }, "Dec 2016")
        ).forEach(book -> {
            bookRepository.save(book);
        });
    }
}
