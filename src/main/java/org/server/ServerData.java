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
    public void createVoting(String topicName, String voteName, String description, Map<String, Integer> options, String creator) {
        Map<String, Vote> votes = topics.get(topicName);
        if (votes != null) {
            votes.put(voteName, new Vote(description, options, creator));
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
                throw new Exception("There is no vote " + vote + " in topic " + topic);
            } else {
                return topics.get(topic).get(vote).options.keySet().toArray(new String[0]);
            }
        }
    }

    public String viewTopics() {
        StringBuilder res = new StringBuilder();
        if (topics.keySet().isEmpty())
            return "";

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
                answer.append(i + 1).append(". ").append(vote.options.keySet().toArray()[i]).append(" : ").append(vote.options.values().toArray()[i]).append("\n");
            }
        }
        return answer.toString();
    }

    public String deleteVote(String topicName, String voteName, String tryingUser) {
        StringBuilder answer = new StringBuilder();

        if (!topics.containsKey(topicName)) {
            answer.append("There is no topic with name ").append(topicName);
        } else if (!topics.get(topicName).containsKey(voteName)) {
            answer.append("There is no vote with name ").append(voteName).append(" in topic ").append(topicName);
        } else {

            if (topics.get(topicName).get(voteName).createdBy.equals(tryingUser)) {
                topics.get(topicName).remove(voteName);
                answer.append("Vote ").append(voteName).append(" deleted!");
            } else {
                answer.append("You can not delete this vote!");
            }

        }
        return answer.toString();
    }

    public Map<String, Object> getDataForSave() {
        // Создаем структуру для сохранения
        Map<String, Object> saveData = new HashMap<>(); // LinkedHashMap сохраняет порядок

        // Сохраняем все темы с голосованиями
        Map<String, Object> topicsData = new HashMap<>();

        for (Map.Entry<String, Map<String, Vote>> topicEntry : topics.entrySet()) {
            String topicName = topicEntry.getKey();
            Map<String, Object> votingsData = new HashMap<>();

            for (Map.Entry<String, Vote> votingEntry : topicEntry.getValue().entrySet()) {
                Vote voting = votingEntry.getValue();

                // Формируем данные голосования
                Map<String, Object> votingData = new HashMap<>();
                votingData.put("description", voting.getDescription());
                votingData.put("createdBy", voting.getCreatedBy());
                //votingData.put("options", new ArrayList<>(voting.getOptions().keySet()));
                votingData.put("results", new HashMap<>(voting.getOptions()));
                votingData.put("voted users", new ArrayList<>(voting.getVotedUsers()));

                votingsData.put(votingEntry.getKey(), votingData);
            }

            topicsData.put(topicName, votingsData);
        }

        // Добавляем мета-информацию

        saveData.put("topics", topicsData);

        return saveData;
    }

    public void restoreFromLoadedData(Map<String, Object> loadedData) {
        topics.clear();
        // Получаем данные по темам
        Map<String, Map<String, Object>> topicsData =
                (Map<String, Map<String, Object>>) loadedData.get("topics");

        // Восстанавливаем каждую тему
        for (Map.Entry<String, Map<String, Object>> topicEntry : topicsData.entrySet()) {
            String topicName = topicEntry.getKey();
            Map<String, Vote> votings = new ConcurrentHashMap<>();

            // Восстанавливаем голосования
            for (Map.Entry<String, Object> votingEntry : topicEntry.getValue().entrySet()) {
                String votingName = votingEntry.getKey();
                Map<String, Object> votingData = (Map<String, Object>) votingEntry.getValue();

                Vote voting = new Vote();
                voting.setDescription((String) votingData.get("description"));
                voting.setCreatedBy((String) votingData.get("createdBy"));
                // Восстанавливаем варианты и результаты
                Map<String, Integer> options = new ConcurrentHashMap<>();
                Map<String, Integer> results = (Map<String, Integer>) votingData.get("results");
                options.putAll(results);
                voting.setOptions(options);

                // Восстанавливаем проголосовавших пользователей (если есть)
                if (votingData.containsKey("votedUsers")) {
                    List<String> votedUsers = (List<String>) votingData.get("votedUsers");
                    votedUsers.forEach(voting::restoreVotedUser);
                }

                votings.put(votingName, voting);
            }

            this.topics.put(topicName, votings);
        }
    }

    private static class Vote {

        private String description;
        private String createdBy;
        private Map<String, Integer> options = new ConcurrentHashMap<>();
        private List<String> votedUsers;

        public Vote() { }

        public Vote(String description, Map<String, Integer> options, String createdBy) {
            this.description = description;
            this.options = options;
            this.votedUsers = Collections.synchronizedList(new ArrayList<>());
            this.createdBy = createdBy;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Integer> getOptions() {
            return options;
        }

        public void setOptions(Map<String, Integer> options) {
            this.options = options;
        }

        public List<String> getVotedUsers() {
            return votedUsers;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void restoreVotedUser(String username) {
            this.votedUsers.add(username);
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
    }
}
