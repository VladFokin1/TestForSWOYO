package org.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData {
    private final Map<String, Map<String, Vote>> topics = new ConcurrentHashMap<>();


    public boolean createTopic(String topicName) {
        if (topicName.isEmpty()) return false;
        return topics.putIfAbsent(topicName, new ConcurrentHashMap<String, Vote>()) == null;
    }

    // Создать новое голосование в разделе
    public void createVoting(String topicName, String voteName, String description, Map<String, Integer> options) {
        Map<String, Vote> votes = topics.get(topicName);
        if (votes != null) {
            votes.put(voteName, new Vote(description, options));
        }
    }

    public String[] getTopicsArray() {
        return topics.keySet().toArray(new String[0]);
    }

    public String[] getVoteOptionsNames(String topic, String vote) {
        if (topics.keySet().stream().noneMatch(x -> x.equals(topic))) {
            return new String[] {"There is no topic with name" + topic};
        } else {
            if (topics.values().stream().noneMatch(x -> x.keySet().stream().anyMatch(y -> y.equals(vote)))) {
                return new String[] {"There is no vote" + vote + " in topic " + topic};
            } else {
                return topics.get(topic).get(vote).options.keySet().toArray(new String[0]);
            }
        }
    }

    public String viewTopics() {
        StringBuilder res = new StringBuilder();
        if (topics.keySet().isEmpty())
            return "There is no topics created yet!";

        for (String topic : topics.keySet()) {
            res.append(topic).append(" (votes in topic=").append(topics.get(topic).keySet().toArray().length).append(")\n");
        }
        return res.toString();
    }

    public String viewVotesInTopic(String topicName) {
        Map<String, Vote> votes = topics.get(topicName);
        if (votes != null)
            return "Votes in " + topicName + " topic:\n"  + votes.keySet();
        else
            return "There is no votes in topic " + topicName;
    }

    private static class Vote {

        private String description;
        private Map<String, Integer> options = new ConcurrentHashMap<>();

        public Vote(String description, Map<String, Integer> options) {

            this.description = description;
            this.options = options;
        }



        public String getDescription() {
            return description;
        }

        public Map<String, Integer> getOptions() {
            return options;
        }
    }
}
