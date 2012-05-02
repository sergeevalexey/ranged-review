package org.beethoven.code

import java.util.concurrent.atomic.AtomicInteger

/**
 * Contains source file info
 *
 * @author Alexey Sergeev
 */
class SourceFileInfo {
    // path inside the repository to the file
    String path
    // key - jira issue key, value - amount of commits per ticket, AtomicInteger is used just because of convenience, it provide possibility to increment value cia special methods (f.e. incrementAndGet)
    Map<String, AtomicInteger> issueCommits = [:].withDefault {String issueKey -> new AtomicInteger(0)}

    void addIssueCommit(String key) {
        issueCommits.get(key).incrementAndGet()
    }

    Integer countCommitsByIssueType(String issueType, Map<String, IssueInfo> issueInfo) {
        return (Integer) issueCommits.findAll {String issueKey, Number amount -> issueInfo[issueKey]?.type == issueType}.values().sum(0) {Number amount -> amount}
    }
}
