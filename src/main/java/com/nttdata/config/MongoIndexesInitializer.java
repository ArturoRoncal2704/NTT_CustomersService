package com.nttdata.config;


import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoIndexesInitializer {

    private final ReactiveMongoTemplate template;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Index idx = new Index()
                .on("documentType", Sort.Direction.ASC)
                .on("documentNumber", Sort.Direction.ASC)
                .unique()
                .partial(PartialIndexFilter.of(Criteria.where("active").is(true)))
                .named("ux_doc_active_true");

        template.indexOps(com.nttdata.domain.Customer.class)
                .ensureIndex(idx)
                .subscribe();
    }
}
