package com.nttdata.customersService.config;

import com.nttdata.config.MongoIndexesInitializer;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoIndexesInitializerTest {

  @Test
  void init_creaIndiceParcialUnico() {
    ReactiveMongoTemplate template = mock(ReactiveMongoTemplate.class);
    ReactiveIndexOperations ops = mock(ReactiveIndexOperations.class);

    when(template.indexOps(any(Class.class))).thenReturn(ops);
    when(ops.ensureIndex(any())).thenReturn(Mono.just("ux_doc_active_true"));

    MongoIndexesInitializer init = new MongoIndexesInitializer(template);
    init.init();

    ArgumentCaptor<org.springframework.data.mongodb.core.index.Index> cap =
        ArgumentCaptor.forClass(org.springframework.data.mongodb.core.index.Index.class);

    verify(ops, times(1)).ensureIndex(cap.capture());
    assertEquals("ux_doc_active_true", cap.getValue().getIndexOptions().get("name"));
  }
}
