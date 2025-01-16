package com.rebuild.backend.utils.rate_limiting_utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;

@Component
@SuppressWarnings("UnstableApiUsage")
@Data
public class UserAgentBloomFilter {

    private BloomFilter<String> bloomFilter;

    private int numAgentsAdded;

    private int expectedInsertions = 1000;

    private double falsePositiveChance = 0.01;

    private Collection<String> addedAgents;

    public UserAgentBloomFilter() {

        this.bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions, falsePositiveChance);
        this.numAgentsAdded = 0;
        addedAgents = new HashSet<>();
    }

    public void addAgent(String agent) {
        bloomFilter.put(agent);
        addedAgents.add(agent);
        numAgentsAdded += 1;
    }

    public boolean mightContain(String agent) {
        return bloomFilter.mightContain(agent);
    }

    public boolean reachedTheoreticalMax(){
        return numAgentsAdded == expectedInsertions;
    }

    public void change(int newExpectedInsertions, double newFalsePositiveProbability) {
        BloomFilter<String> newFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                newExpectedInsertions, newFalsePositiveProbability
        );
        setExpectedInsertions(newExpectedInsertions);
        setFalsePositiveChance(newFalsePositiveProbability);
        addedAgents.forEach(newFilter::put);
        setNumAgentsAdded(addedAgents.size());
        setBloomFilter(newFilter);
    }
}
