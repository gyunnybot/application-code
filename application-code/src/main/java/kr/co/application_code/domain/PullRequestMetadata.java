package kr.co.application_code.domain;

import java.time.Instant;
import java.util.List;

/**
 * PR 분석/통계를 위해 쓰는 "내부 표준 메타데이터".
 * GitHub payload 구조가 바뀌어도 이 모델은 가능한 안정적으로 유지한다.
 */
// 내부 표준 메타데이터
public record PullRequestMetadata(
        RepoRef repo,
        long prId,
        int prNumber,
        String title,
        String state, // types: [opened, reopened, synchronize, ready_for_review]
        boolean merged,
        UserRef author,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt,
        Instant mergedAt,
        String baseBranch,
        String headBranch,
        List<String> labels,
        List<UserRef> assignees,
        List<UserRef> requestedReviewers
) {
    public record RepoRef(String owner, String name) {}
    public record UserRef(String login) {}
}
