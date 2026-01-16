package kr.co.application_code.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kr.co.application_code.domain.PullRequestMetadata;
import kr.co.application_code.domain.PullRequestMetadata.RepoRef;
import kr.co.application_code.domain.PullRequestMetadata.UserRef;
import org.springframework.stereotype.Component;

/**
 * GitHub "pull_request" webhook payload에서 원천 메타데이터만 추출한다.
 *
 * - 입력: raw JSON (webhook request body 그대로)
 * - 출력: 내부 표준 모델(PullRequestMeta 클래스)
 *
 * 배포 전에는 테스트에서 JSON fixture를 넣고,
 * 배포 후에는 Controller가 받은 request body를 그대로 넣는다.
 */
@Component
public class GitHubPullRequestWebhookExtractor {

    private final ObjectMapper objectMapper;

    public GitHubPullRequestWebhookExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PullRequestMetadata extract(String payloadJson) {
        try {
            JsonNode root = objectMapper.readTree(payloadJson);

            // pull_request 이벤트에서 PR 정보는 보통 root.pull_request 에 있다.
            JsonNode pr = root.path("pull_request");

            RepoRef repo = extractRepo(root);
            long prId = asLong(pr, "id");
            int prNumber = asInt(root, "number"); // event root에 number 존재
            String title = asText(pr, "title");
            String state = asText(pr, "state");
            boolean merged = asBoolean(pr, "merged");

            UserRef author = new UserRef(asText(pr.path("user"), "login"));

            Instant createdAt = asInstant(pr, "created_at");
            Instant updatedAt = asInstant(pr, "updated_at");
            Instant closedAt  = asInstantOrNull(pr, "closed_at");
            Instant mergedAt  = asInstantOrNull(pr, "merged_at");

            String baseBranch = asText(pr.path("base"), "ref");
            String headBranch = asText(pr.path("head"), "ref");

            List<String> labels = extractLabels(pr.path("labels"));
            List<UserRef> assignees = extractUsers(pr.path("assignees"));
            List<UserRef> requestedReviewers = extractUsers(pr.path("requested_reviewers"));

            return new PullRequestMetadata(
                    repo,
                    prId,
                    prNumber,
                    title,
                    state,
                    merged,
                    author,
                    createdAt,
                    updatedAt,
                    closedAt,
                    mergedAt,
                    baseBranch,
                    headBranch,
                    labels,
                    assignees,
                    requestedReviewers
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid GitHub webhook JSON payload", e);
        }
    }

    private RepoRef extractRepo(JsonNode root) {
        // root.repository.owner.login, root.repository.name
        JsonNode repo = root.path("repository");
        String name = asText(repo, "name");
        String owner = asText(repo.path("owner"), "login");
        return new RepoRef(owner, name);
    }

    private List<String> extractLabels(JsonNode labelsNode) {
        List<String> labels = new ArrayList<>();
        if (labelsNode == null || labelsNode.isMissingNode() || !labelsNode.isArray()) return labels;

        for (JsonNode n : labelsNode) {
            String labelName = n.path("name").asText(null);
            if (labelName != null && !labelName.isBlank()) labels.add(labelName);
        }
        return labels;
    }

    private List<UserRef> extractUsers(JsonNode arrNode) {
        List<UserRef> users = new ArrayList<>();
        if (arrNode == null || arrNode.isMissingNode() || !arrNode.isArray()) return users;

        Iterator<JsonNode> it = arrNode.elements();
        while (it.hasNext()) {
            JsonNode n = it.next();
            String login = n.path("login").asText(null);
            if (login != null && !login.isBlank()) users.add(new UserRef(login));
        }
        return users;
    }

    private String asText(JsonNode node, String field) {
        JsonNode v = node.path(field);
        String s = v.asText(null);
        if (s == null) return "";
        return s;
    }

    private long asLong(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNumber() ? v.asLong() : parseLongSafe(v.asText(null));
    }

    private int asInt(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNumber() ? v.asInt() : parseIntSafe(v.asText(null));
    }

    private boolean asBoolean(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isBoolean() ? v.asBoolean() : Boolean.parseBoolean(v.asText("false"));
    }

    private Instant asInstant(JsonNode node, String field) {
        String s = node.path(field).asText(null);
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Missing required timestamp field: " + field);
        }
        return Instant.parse(s);
    }

    private Instant asInstantOrNull(JsonNode node, String field) {
        String s = node.path(field).asText(null);
        if (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) return null;
        return Instant.parse(s);
    }

    private long parseLongSafe(String s) {
        if (s == null || s.isBlank()) return 0L;
        try { return Long.parseLong(s); } catch (Exception ignored) { return 0L; }
    }

    private int parseIntSafe(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s); } catch (Exception ignored) { return 0; }
    }
}
