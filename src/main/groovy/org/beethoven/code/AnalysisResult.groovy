package org.beethoven.code

/**
 * Holds the collected information about each source file under VC
 *
 * @author Alexey Sergeev
 */
public class AnalysisResult {
    // key -> source path, value -> SourceFileInfo
    Map<String, SourceFileInfo> sourceInfo = new TreeMap<String, SourceFileInfo>().withDefault {String path -> new SourceFileInfo(path: path)}
}
