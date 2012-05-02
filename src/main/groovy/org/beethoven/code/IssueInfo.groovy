package org.beethoven.code

/**
 * Ticket info
 *
 * @author Alexey Sergeev
 */
class IssueInfo {
    public static final String NO_ID = "none"

    String id;
    String type;

    @Override
    int hashCode() {
        return id.hashCode()
    }
}
