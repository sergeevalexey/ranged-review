package org.beethoven.code

import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair

import java.util.regex.Matcher

/**
 * Fetch Jira issue via search interface using RSS issue view
 *
 * @author Alexey Sergeev
 */
class JiraIssueInfoFetcher {
    private HttpClient httpclient = new DefaultHttpClient()

    private static final String errorExpression = /.*An\ issue\ with\ key\ '([a-zA-Z]+\-\d+)' does not exist for field \'issueKey\'.*/

    private String jiraUrl
    private String jiraUsername
    private String jiraPassword

    private String jiraRssSearchRequestPath = "sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml"

    JiraIssueInfoFetcher(String jiraUrl, String jiraUsername, String jiraPassword) {
        this.jiraUrl = jiraUrl
        this.jiraUsername = jiraUsername
        this.jiraPassword = jiraPassword
    }

    /**
     * Retrieves ticket information from Jira by passsed issue keys
     * @param issueKeys
     * @return
     */
    Map<String, IssueInfo> fetch(Collection<String> issueKeys) {
        Map<String, IssueInfo> result = [:] as Map<String, IssueInfo>
        if (!issueKeys) {
            // no keys -> no issues
            return result
        }

        if (issueKeys.size() > 500) {
            // too much issues that we divide them and fetch each half
            int divideIndex = (Integer) issueKeys.size() / 2
            result << fetch(issueKeys.toList()[0..divideIndex]) + fetch(issueKeys.toList()[(divideIndex + 1)..(issueKeys.size() - 1)])
            return result
        }

        // issues search params
        List<BasicNameValuePair> params = [
                new BasicNameValuePair('os_username', 'sab@scand.com'),
                new BasicNameValuePair('os_password', 'qwert'),
                new BasicNameValuePair('field', 'key'),
                new BasicNameValuePair('field', 'type'),
                new BasicNameValuePair('jqlQuery', "issueKey in (${issueKeys.join(',')})")
        ] as List<BasicNameValuePair>;
        // create search uri
        String searchUri = "${jiraUrl}/${jiraRssSearchRequestPath}?${URLEncodedUtils.format(params, 'UTF-8')}"
        // run search
        HttpResponse response = httpclient.execute(new HttpGet(searchUri))
        HttpEntity entity = response.getEntity()
        InputStream is = null
        try {
            // read response body
            is = entity.content
            // get if as a text
            String responseAsText = is.getText("utf-8")
            // something is wrong processing response in a special way
            if (response.statusLine.statusCode != 200) {
                // in case of error we search for the possible not found issue
                Matcher matcher = responseAsText =~ errorExpression
                if (matcher.matches()) {
                    // if non existing issue is  found skip it and repeat fetch once again
                    String notFoundIssueKey = matcher[0][1]
                    println "warning: issue '${notFoundIssueKey}' is not found, skip it!"
                    return fetch(issueKeys.minus(matcher[0][1]))
                } else {
                    // something unexpected happened
                    println responseAsText
                    throw new RuntimeException("Can't fetch issue info from Jira")
                }
            }
            /*
             * Parsing result RSS
             * <rss ..>
             *   <channel>
             *   ...
             *   <item>
             *     <key ...>PRJ-123</key>
             *     <type ...>Bug</type>
             *   </item>
             *   ...
             * </rss>
             *
             */
            GPathResult rss = new XmlSlurper().parseText(responseAsText)
            rss.channel.item.list().each {
                result[it.key.text()] = new IssueInfo(id: it.key.text(), type: it.type.text())
            }
            return result
        } finally {
            IOUtils.closeQuietly(is)
        }
    }
}
