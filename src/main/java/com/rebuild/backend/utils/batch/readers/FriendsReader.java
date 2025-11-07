package com.rebuild.backend.utils.batch.readers;

import com.rebuild.backend.model.forms.dtos.forum_dtos.FriendRequestDTO;
import lombok.NonNull;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.amqp.AmqpItemReader;
import org.springframework.stereotype.Component;

@Component
public class FriendsReader extends AmqpItemReader<FriendRequestDTO> implements ItemStream {

    private long currentIndex = 0L;

    private static final String CURRENT = "current.index";

    public FriendsReader(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.containsKey(CURRENT)) {
            currentIndex = executionContext.getLong(CURRENT);
        } else {
            currentIndex = 0;
        }
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        currentIndex += 1;
        executionContext.putLong(CURRENT, currentIndex);
    }
}
