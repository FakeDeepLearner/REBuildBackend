package com.rebuild.backend.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Component
@SuppressWarnings("UnstableApiUsage")
@Data
public class UserAgentBloomFilter {

    private BloomFilter<String> bloomFilter;

    private int numAgentsAdded;

    private int expectedInsertions = 1000;

    private double falsePositiveChance = 0.01;

    public UserAgentBloomFilter() {

        this.bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions, falsePositiveChance);
        this.numAgentsAdded = 0;
    }

    public void addAgent(String agent) {
        bloomFilter.put(agent);
        numAgentsAdded += 1;
    }

    public boolean mightContain(String agent) {
        return bloomFilter.mightContain(agent);
    }

    public boolean reachedTheoreticalMax(){
        return numAgentsAdded == expectedInsertions;
    }

    public void change(int newExpectedInsertions, double newFalsePositiveProbability,
                                             Collection<String> allNewItems) {
        BloomFilter<String> newFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                newExpectedInsertions, newFalsePositiveProbability
        );
        setExpectedInsertions(newExpectedInsertions);
        setFalsePositiveChance(newFalsePositiveProbability);
        allNewItems.forEach(newFilter::put);
        setNumAgentsAdded(allNewItems.size());
        setBloomFilter(newFilter);
    }
}
