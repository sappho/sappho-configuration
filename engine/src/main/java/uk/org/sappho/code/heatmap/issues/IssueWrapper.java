package uk.org.sappho.code.heatmap.issues;

import java.util.List;

public class IssueWrapper {

    private final String issueKey;
    private final String summary;
    private final String subTaskKey;
    private final List<String> releases;
    private final int weight;

    public IssueWrapper(String issueKey, String summary, String subTaskKey, List<String> releases, int weight) {

        this.issueKey = issueKey;
        this.summary = summary;
        this.subTaskKey = subTaskKey;
        this.releases = releases;
        this.weight = weight;
    }

    public String getKey() {

        return issueKey;
    }

    public String getSubTaskKey() {

        return subTaskKey;
    }

    public String getSummary() {

        return summary;
    }

    public List<String> getReleases() {

        return releases;
    }

    public int getWeight() {

        return weight;
    }
}
