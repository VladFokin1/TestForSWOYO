package org.server;

import java.util.*;
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
            votes.put(voteName, new Vote(description, options ));
        }
    }

    public String[] getTopicsArray() {
        return topics.keySet().toArray(new String[0]);
    }

    public String[] getVoteOptionsNames(String topic, String vote) throws Exception{
        if (topics.keySet().stream().noneMatch(x -> x.equals(topic))) {
            throw new Exception("There is no topic with name" + topic);
        } else {
            if (topics.values().stream().noneMatch(x -> x.keySet().stream().anyMatch(y -> y.equals(vote)))) {
                throw new Exception("There is no vote" + vote + " in topic " + topic);
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
        if (!topics.containsKey(topicName)) {
            return "There is no topic with name " + topicName;
        }
        Map<String, Vote> votes = topics.get(topicName);
        if (votes != null)
            return "Votes in " + topicName + " topic:\n"  + votes.keySet();
        else
            return "There is no votes in topic " + topicName;
    }

    public String vote(String topicName, String voteName, String optionName, String userName) {
        StringBuilder answer = new StringBuilder();
        if (topics.get(topicName).get(voteName).votedUsers.contains(userName)) {
            answer.append("Have you already voted here!");
        } else {
            Vote vote = topics.get(topicName).get(voteName);
            vote.votedUsers.add(userName);
            vote.options.put(optionName, vote.options.get(optionName) + 1);
            answer.append("You voted for ").append(optionName).append(" in vote ").append(voteName);
        }
        return answer.toString();
    }

    public String viewVote(String topicName, String voteName) {

        StringBuilder answer = new StringBuilder();

        if (!topics.containsKey(topicName)) {
            answer.append("There is no topic with name ").append(topicName);
        } else if (!topics.get(topicName).containsKey(voteName)) {
            answer.append("There is no vote with name ").append(voteName).append(" in topic ").append(topicName);
        } else {
            Vote vote = topics.get(topicName).get(voteName);
            answer.append(vote.description).append("\n");

            for (int i = 0; i < vote.options.keySet().size(); i++) {
                answer.append(i).append(". ").append(vote.options.keySet().toArray()[i]).append(" : ").append(vote.options.values().toArray()[i]);
            }
        }
        return answer.toString();
    }

    private static class Vote {

        private String description;
        private Map<String, Integer> options = new ConcurrentHashMap<>();
        private List<String> votedUsers;

        public Vote(String description, Map<String, Integer> options) {
            this.description = description;
            this.options = options;
            this.votedUsers = Collections.synchronizedList(new ArrayList<>());
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Integer> getOptions() {
            return options;
        }
    }
}
