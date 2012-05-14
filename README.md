# ranged-review

Performs source files commits analysis basing on Mercurial repository &amp; Jira issues

## Idea

There are a lot of tools that performs source code analysis within the project:
- static code analysis: checkstyle, pmd, codenarc, findbugs, jslint and etc.
- code coverage: cobertura, clover, emma, jscoverage and etc.

These ones provide good overview of the code quality basing on the configured rules for certain programming language.

Yet I think it is not enough. And if you would like to make an overview of how development process goes on inside the project you can form your view on the activity that took place in the source code for period of time.
How? Hmm... Lets assume you use VCS (version control system) and bug tracking system in you daily development. Doesn't ring a bell? :)
So developers commit the changes basing on the posted issues over and over again, day by day. All commits could be found in the history. Usually each commit contains comment that describes what has been done and reason for it, i.e. issue id.
F.e.

(PRJ-1234) Some commit that fixes the bug
or
(MODULE-8765) Implemented great feature

Each commit has a MESSAGE and CHANGED FILES set. Each message could contain a reference to an ISSUE in bug tracking system. Most natural development activity is adding something new and fixing bugs, that corresponds to issue types "NEW FEATURE" and "BUG".
Analysis is done basing one described assumptions and tool generate report that shows how much times each source file was changed because of the BUG or NEW FEATURE issue.

## Prerequisite to analyzed sources

I focused this tool on *Mercurial* and *Jira*. These two I use every day, so that's why I selected them, but it does not mean that for other VCS-s and bug tracking systems the same analysis can't be done.
So Mercurial repository sources should be cloned onto your local FS. 
Jira have to be available via http and be either publicly available otherwise you need to know user name & password to log in into it.
In Jira bug ticket has type *Bug*, and feature ticket has type *New Feature*.
Tool assumes that each commit message should look like:
<pre><code>(ABC-123) This is my commit</code></pre>
i.e. at the beginning of the message issue key should be defined in the round braces that is followed by commit commentary.

## How it works
Initial idea was to analyse sources history within specified period of time (that's why I named tool *ranged-view*). But unfortunately hg4j library that I've used does not support such a feature yet. So tool analyses repository commits from the latest change set on out to the configured maximum number of commits back in history.
Each commit message is checked to issue id presence and then issue data is fetched from Jira. Amount of commits per issue is gathered per source file and basing on this info final report is generated. It contains grouped info on how many "bug" and "feature" commits were done in the file.

## Development tools
* Maven 2 or 3 (M2_HOME is configured)
* Java 6 (JAVA_HOME is configured)

**P.S.** Project uses [hg4j](http://code.google.com/p/hg4j). At the moment of initial project creation version 0.9.0 was not available in any public Maven repository. You need to download and put it into your local repository.

## How to use
Here are Windows based scripts:
* build.bat - compiles sources and packages them
* runtool.bat - runs the tool, takes one required parameter - configuration file path

## Configuration file
<pre><code>
// Mercurial repository absolute path
repository.path='&lt;path to mercurial repo>'
// maximum amount of change sets that need to be analyzed
repository.log.maxchagesets=1000
// jira parameters
jira.url='&lt;jira url>'
jira.username='&lt;jira user name>'
jira.password='&lt;jira password>'
</code></pre>

## Report

Here how it looks like (sample Grails project sources):
<pre><code>
Bug commits | Feature commits | Path
         56 |              74 | 'grails-app/controllers/CartController.groovy'
         54 |              40 | 'grails-app/controllers/SearchController.groovy'
         50 |              58 | 'grails-app/controllers/DetailsController.groovy'
          4 |               3 | 'web-app/js/engine.js'
          4 |              19 | 'application.properties'
...
</code></pre>

All the files are sorted by amount of bug commits. Sources with 0 bugs are skipped.
Basing on such a report you can see what sources where "touched" the most because of the bugs, still you can see the amount of features that are applied to the same source file.
Further conclusions you could do on your own. F.e. review specific source file more precisely, check whether it needs to be refactored or covered by tests. So it gives you possibility to reveal the problems of your source files.

I hope that you'll find this tool useful for YOU.

