package org.beethoven.code

import org.tmatesoft.hg.core.HgChangeset
import org.tmatesoft.hg.core.HgChangesetHandler
import org.tmatesoft.hg.core.HgLogCommand
import org.tmatesoft.hg.core.HgRepoFacade
import org.tmatesoft.hg.util.Path

import java.util.regex.Matcher

/**
 * Analyses the repository within the specific date range
 *
 * @author Alexey Sergeev
 */
public class AnalysisTool {
    private static final Integer MAX_COMMITS_DEFAULT = 10000
    private static final String issueRetrievalExpression = /\(([a-zA-Z]+\-\d+)\).*/

    private static final String ISSUE_TYPE_BUG = 'Bug'
    private static final String ISSUE_TYPE_NEWFEATURE = 'New Feature'

    public static void main(String[] args) {
        // path to configuration file should be passed
        if (!args || args.size() != 1) {
            println "Path to configuration file is not passed."
            println "Configuration file should define the following parameters:"
            println "repository.path = <absolute path to Mercurial source repository>"
            println "jira.url = <Jira URL>"
            println "jira.username = <Jira user login name>"
            println "jira.password =  <Jira user password>"
            return
        }
        // check whether configuration file exists
        File configurationFile = new File(args[0])
        if (!configurationFile.exists()) {
            println "Configuration file '${configurationFile}' is not found"
            return
        }
        // read configuration
        ConfigObject configuration = new ConfigSlurper().parse(configurationFile.toURL());
        // read repository path
        String repositoryPath = configuration.repository.path
        // creating repository facade and checking whether underlying repository exists
        HgRepoFacade hgRepo = new HgRepoFacade();
        if (!hgRepo.initFrom(new File(repositoryPath))) {
            System.err.printf("Can't find repository in '${hgRepo.getRepository().getLocation()}'");
            return;
        }

        Integer maxCommits = configuration.repository.log.maxchagesets?:MAX_COMMITS_DEFAULT
        println "Reading last '${maxCommits}' commits.."
        HgLogCommand cmd = hgRepo.createLogCommand();
        final AnalysisResult analysisResult = new AnalysisResult();
        cmd.branch('default').limit(maxCommits).execute(new HgChangesetHandler() {
            @Override
            void cset(HgChangeset changeset) {
                changeset.getAffectedFiles().each {Path path ->
                    // searching for ticket number within the comment
                    Matcher matcher = (changeset.comment =~ issueRetrievalExpression)
                    String issueKey = IssueInfo.NO_ID
                    if (matcher.matches()) {
                        issueKey = matcher[0][1]
                    }
                    analysisResult.sourceInfo[path.toString()].addIssueCommit(issueKey)
                }
            }
        })
        // fetching issue info
        println "Fetching issues info from Jira..."
        Set<String> issueKeys = [] as Set<String>
        analysisResult.sourceInfo.values().each {SourceFileInfo sourceFileInfo ->
            issueKeys.addAll(sourceFileInfo.issueCommits.keySet())
        }
        issueKeys.remove(IssueInfo.NO_ID)
        Map<String, IssueInfo> issueInfo = new JiraIssueInfoFetcher(configuration.jira.url, configuration.jira.username, configuration.jira.password).fetch(issueKeys);
        println "'${issueInfo.size()}' issues are found"

        // report
        println "Preparing report.."
        println "Report shows how many commits were done in specified file basing on 'Bug' and 'New Feature' issues"
        println "${'Bug commits'} | ${'Feature commits'} | ${'Path'.padRight(5)}"
        analysisResult.sourceInfo.values().sort {SourceFileInfo sourceFileInfo -> sourceFileInfo.countCommitsByIssueType(ISSUE_TYPE_BUG, issueInfo)}.reverse().each {SourceFileInfo sourceFileInfo ->
            Integer bugCount = sourceFileInfo.countCommitsByIssueType(ISSUE_TYPE_BUG, issueInfo);
            Integer featureCount = sourceFileInfo.countCommitsByIssueType(ISSUE_TYPE_NEWFEATURE, issueInfo);
            if (bugCount > 0) {
                println "${bugCount.toString().padLeft(11)} | ${featureCount.toString().padLeft(15)} | '${sourceFileInfo.path}'"
            }
        }
    }
}